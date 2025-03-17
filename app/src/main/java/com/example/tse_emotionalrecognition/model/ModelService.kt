package com.example.tse_emotionalrecognition.model

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.impl.close
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.UserRepository
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectType
import com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement
import com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement
import com.example.tse_emotionalrecognition.presentation.FeedbackActivity
import com.example.tse_emotionalrecognition.presentation.LabelActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import smile.classification.GradientTreeBoost
import smile.classification.gbm
import smile.data.DataFrame
import smile.data.formula.Formula
import smile.data.vector.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.io.path.exists
import kotlin.math.pow
import kotlin.math.sqrt

const val MODEL_FILE_NAME = "gbm_model.ser"
const val DATA_WINDOW_MS = 3 * 60 * 1000L
const val LABEL_COLUMN = "affect"
const val LAST_TRAINED_KEY = "last_trained_time"

class ModelService : Service() {

    companion object {
        const val ACTION_TRAIN_MODEL = "ACTION_TRAIN_MODEL"
        const val ACTION_PREDICT = "ACTION_PREDICT"
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userRepository: UserRepository

    private var heartRateMeasurements: List<HeartRateMeasurement> = emptyList()
    private var skinTemperatureMeasurements: List<SkinTemperatureMeasurement> = emptyList()
    private var affectData: List<AffectData> = emptyList()
    private var sessionId: Long = 0L

    private var prediction = AffectType.NONE

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        userRepository = UserDataStore.getUserRepository(applicationContext)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        Log.d("ModelService", "Service started")

        startForeground(
            124,
            createNotification("Model Service is running ..."),
            FOREGROUND_SERVICE_TYPE_HEALTH
        )

        sessionId = intent?.getLongExtra("sessionId", 0L) ?: 0L

        when (action) {
            ACTION_TRAIN_MODEL -> {
                loadData()

                val lastTrainedTime = sharedPreferences.getLong(LAST_TRAINED_KEY, 0L)
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastTrainedTime > 24 * 60 * 60 * 1000) {
                    trainModel()
                }

                predict()

                val intent = Intent(applicationContext, FeedbackActivity::class.java)
                intent.flags = FLAG_ACTIVITY_NEW_TASK // Hinzufügen des Flags
                val newAffectData = AffectData(
                    sessionId = sessionId,
                    timeOfNotification= System.currentTimeMillis(),
                    affect = AffectType.NULL)

                //TODO selbes affect Data
                userRepository.insertAffect(
                    CoroutineScope(Dispatchers.IO), newAffectData,
                ) { insertedAffectData ->
                    if (insertedAffectData != null) {
                        Log.v("ModelService", "AffectData inserted with ID: ${insertedAffectData.id}")


                        intent.putExtra("affectDataId", newAffectData.id)
                        val pendingIntent =
                            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                        Log.v("ModelService", "AffectData ID: ${insertedAffectData.id}")
                        createActivityNotification("How do you feel", pendingIntent)
                    } else {
                        Log.e("ModelService", "Failed to insert AffectData")
                    }

                }
            }
            ACTION_PREDICT -> {
                loadData ()

                predict()

                // TODO: Link to Intervention Helper
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun trainModel() {
        val trainingData = prepareTrainingData()

        val model = gbm(Formula.lhs(LABEL_COLUMN), trainingData)

        val currentTime = System.currentTimeMillis()
        sharedPreferences.edit().putLong(LAST_TRAINED_KEY, currentTime).apply()

        storeModel(model)
    }

    private fun predict() {
        var model = loadModel()

        if (model == null) {
            trainModel()

            model = loadModel()
        }

        val predictionData = preparePredictionData()

        if (model != null) {
            val predictionAsInt = model.predict(predictionData)[0]

            prediction = when (predictionAsInt) {
                0 -> AffectType.NEGATIVE
                1 -> AffectType.POSITIVE
                2 -> AffectType.NONE
                else -> AffectType.NULL
            }
        }
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                heartRateMeasurements = userRepository.getAllHeartRateMeasurements()
                skinTemperatureMeasurements = userRepository.getAllSkinTemperatureMeasurements()
                affectData = userRepository.getAllAffectData()

                Log.d("ModelService", "Loaded ${heartRateMeasurements.size} heart rate measurements")
                Log.d("ModelService", "Loaded ${skinTemperatureMeasurements.size} skin temperature measurements")
                Log.d("ModelService", "Loaded ${affectData.size} affect data entries")

                heartRateMeasurements.firstOrNull()?.let { Log.d("ModelService", "First HeartRateMeasurement: $it") }
                skinTemperatureMeasurements.firstOrNull()?.let { Log.d("ModelService", "First SkinTemperatureMeasurement: $it") }
                affectData.firstOrNull()?.let { Log.d("ModelService", "First AffectData: $it") }

            } catch (e: Exception) {
                Log.e("ModelService", "Error loading data", e)
            }
        }
    }

    private fun prepareTrainingData(): DataFrame {

        // Create the dataframe for heartrate values
        val heartRateMeasurementsFiltered = heartRateMeasurements.filter { it.hr in 40..200 }
        val hrTimestamp = heartRateMeasurementsFiltered.map { it.timestamp }
        val ibi = heartRateMeasurementsFiltered.map { 60000.0 / it.hr }
        val hrNormalized = normalize(heartRateMeasurementsFiltered.map { it.hr.toDouble() })
        val ibiNormalized = normalize(ibi)

        val hrDf = DataFrame.of(
            LongVector.of("timestamp", hrTimestamp.toLongArray()),
            DoubleVector.of("hr_normalized", hrNormalized.toDoubleArray()),
            DoubleVector.of("ibi_normalized", ibiNormalized.toDoubleArray())
        )

        val skinTemperatureMeasurementsFiltered = skinTemperatureMeasurements.filter { it.status == 1 }
        val skinTimestamp = skinTemperatureMeasurementsFiltered.map { it.timestamp }
        val skinTemperature = skinTemperatureMeasurementsFiltered.map { it.objectTemperature.toDouble() }
        val skinTemperatureNormalized = normalize(skinTemperature)

        val skinDf = DataFrame.of(
            LongVector.of("timestamp", skinTimestamp.toLongArray()),
            DoubleVector.of("skin_temperature_normalized", skinTemperatureNormalized.toDoubleArray())
        )

        val affectDf = DataFrame.of(
            LongVector.of("timestamp", affectData.map { it.timeOfNotification }.toLongArray()),
            LongVector.of("affect", affectData.map { it.affect.ordinal.toLong() }.toLongArray())
        ).filter {
            it[1] as Long != 3L
        }

        val rows = mutableListOf<DataRow>()

        for (row in affectDf) {
            val affectTimestamp = row[0] as Long
            val affectValue = row[1] as Long

            val hrWindow = hrDf.filter { it[0] as Long in (affectTimestamp - DATA_WINDOW_MS)..affectTimestamp }

            if (hrWindow.isEmpty()) {
                continue
            }

            val hrNormalizedValues = hrWindow.map { it[1] as Double }
            val meanHrNormalized = hrNormalizedValues.average()

            val ibiNormalizedValues = hrWindow.map { it[2] as Double }
            val rmssd = calculateRmssd(ibiNormalizedValues)

            val skinWindow = skinDf.filter { it[0] as Long in (affectTimestamp - DATA_WINDOW_MS)..affectTimestamp }

            var skinTemperatureNormalizedValues = skinWindow.map { it[1] as Double }

            if (skinTemperatureNormalizedValues.isEmpty()) {
                skinTemperatureNormalizedValues = List(1) { 0.0 }
            }

            val meanSkinTemperatureNormalized = skinTemperatureNormalizedValues.average()

            rows.add(DataRow(meanHrNormalized, rmssd, meanSkinTemperatureNormalized, affectValue))
        }

        val df = DataFrame.of(rows, DataRow::class.java)

        // TODO: Add dropna step

        return df
    }

    private fun preparePredictionData(): DataFrame {
        val heartRateMeasurementsFiltered = heartRateMeasurements.filter { it.hr in 40..200 }
        val hrTimestamp = heartRateMeasurementsFiltered.map { it.timestamp }
        val ibi = heartRateMeasurementsFiltered.map { 60000.0 / it.hr }
        val hrNormalized = normalize(heartRateMeasurementsFiltered.map { it.hr.toDouble() })
        val ibiNormalized = normalize(ibi)

        val hrDf = DataFrame.of(
            LongVector.of("timestamp", hrTimestamp.toLongArray()),
            DoubleVector.of("hr_normalized", hrNormalized.toDoubleArray()),
            DoubleVector.of("ibi_normalized", ibiNormalized.toDoubleArray())
        )

        val skinTemperatureMeasurementsFiltered = skinTemperatureMeasurements.filter { it.status == 1 }
        val skinTimestamp = skinTemperatureMeasurementsFiltered.map { it.timestamp }
        val skinTemperature = skinTemperatureMeasurementsFiltered.map { it.objectTemperature.toDouble() }
        val skinTemperatureNormalized = normalize(skinTemperature)

        val skinDf = DataFrame.of(
            LongVector.of("timestamp", skinTimestamp.toLongArray()),
            DoubleVector.of("skin_temperature_normalized", skinTemperatureNormalized.toDoubleArray())
        )

        val latestTimestamp = heartRateMeasurementsFiltered.lastOrNull()?.timestamp ?: 0L

        val hrWindow = hrDf.filter { it[0] as Long in (latestTimestamp - DATA_WINDOW_MS)..latestTimestamp }

        val hrNormalizedValues = hrWindow.map { it[1] as Double }
        val meanHrNormalized = hrNormalizedValues.average()

        val ibiNormalizedValues = hrWindow.map { it[2] as Double }
        val rmssd = calculateRmssd(ibiNormalizedValues)

        val skinWindow = skinDf.filter { it[0] as Long in (latestTimestamp - DATA_WINDOW_MS)..latestTimestamp }



        var skinTemperatureNormalizedValues = skinWindow.map { it[1] as Double }

        if (skinTemperatureNormalizedValues.isEmpty()) {
            skinTemperatureNormalizedValues = List(1) { 0.0 }
        }

        val meanSkinTemperatureNormalized = skinTemperatureNormalizedValues.average()

        return DataFrame.of(
            DoubleVector.of("meanHeartRateNormalized", doubleArrayOf(meanHrNormalized)),
            DoubleVector.of("rmssdNormalized", doubleArrayOf(rmssd)),
            DoubleVector.of("meanSkinTemperatureNormalized", doubleArrayOf(meanSkinTemperatureNormalized))
        )
    }

    private fun normalize(values: List<Double>): List<Double> {
        val mean = values.average()

        val std = values.map { (it - mean).pow(2) }.average().pow(0.5)

        return values.map { (it - mean) / std }
    }

    private fun calculateRmssd(ibiValues: List<Double>): Double {
        val differences = mutableListOf<Double>()
        for (i in 1 until ibiValues.size) {
            differences.add(ibiValues[i] - ibiValues[i - 1])
        }
        val squaredDifferences = differences.map { it * it }
        val meanSquaredDifference = squaredDifferences.average()
        return sqrt(meanSquaredDifference)
    }


    private fun storeModel(model: GradientTreeBoost) {
        Log.d("ModelService", "Storing model...")

        try {
            val file = File(this.filesDir, MODEL_FILE_NAME)
            val fileOutputStream = FileOutputStream(file)
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(model)
            objectOutputStream.close()
            fileOutputStream.close()
            Log.d("ModelTrainer", "Model stored successfully at ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("ModelTrainer", "Error storing model", e)
        }
    }
    private fun loadModel(): GradientTreeBoost? {
        Log.d("ModelTrainer", "Loading model...")

        try {
            val file = File(this.filesDir, MODEL_FILE_NAME)
            if (!file.exists()) {
                Log.w("ModelTrainer", "Model file does not exist.")
                return null
            }

            val fileInputStream = FileInputStream(file)
            val objectInputStream = ObjectInputStream(fileInputStream)
            val model = objectInputStream.readObject() as GradientTreeBoost
            objectInputStream.close()
            fileInputStream.close()
            Log.d("ModelTrainer", "Model loaded successfully from ${file.absolutePath}")
            return model
        } catch (e: Exception) {
            Log.e("ModelTrainer", "Error loading model", e)
            return null
        }
    }

    private fun createActivityNotification(notificationText: String, intent: PendingIntent) {
        Log.v("DataCollectService", "Creating notification: $notificationText")
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 4  // Unique ID for the notification


        val notification = NotificationCompat.Builder(this, "data_collection_service")
            .setContentTitle("Data Collection Service")
            .setContentText(notificationText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .addAction(R.mipmap.ic_launcher, "Launch Activity", intent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, "data_collection_service")
            .setContentTitle("Data Collection Service")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }
}