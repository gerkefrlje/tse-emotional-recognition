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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
                loadData {

                    val lastTrainedTime = sharedPreferences.getLong(LAST_TRAINED_KEY, 0L)
                    Log.d("ModelService", "Last trained time: $lastTrainedTime")
                    val currentTime = System.currentTimeMillis()

//                    if (currentTime - lastTrainedTime > 24 * 60 * 60 * 1000) {
//                        Log.d("ModelService", "Training model...")
//                        trainModel()
//                    }

                    trainModel()

                    predict()

                    val intent = Intent(applicationContext, FeedbackActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK // Hinzufügen des Flags
                    val newAffectData = AffectData(
                        sessionId = sessionId,
                        timeOfNotification = System.currentTimeMillis(),
                        affect = AffectType.NULL
                    )

                    //TODO selbes affect Data
                    userRepository.insertAffect(
                        CoroutineScope(Dispatchers.IO), newAffectData,
                    ) { insertedAffectData ->
                        if (insertedAffectData != null) {
                            Log.v(
                                "ModelService",
                                "AffectData inserted with ID: ${insertedAffectData.id}"
                            )


                            intent.putExtra("affectDataId", insertedAffectData.id)
                            val predictionString = Json.encodeToString(prediction)
                            intent.putExtra("prediction", predictionString)

                            val pendingIntent =
                                PendingIntent.getActivity(
                                    this,
                                    insertedAffectData.id.toInt(),
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                            Log.v("ModelService", "AffectData ID: ${insertedAffectData.id}")
                            createActivityNotification("How do you feel", pendingIntent)
                        } else {
                            Log.e("ModelService", "Failed to insert AffectData")
                        }

                    }
                }
            }
            ACTION_PREDICT -> {
                loadData {
                    predict()
                }

                // TODO: Link to Intervention Helper
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun trainModel() {
        Log.d("ModelService", "Preparing training data...")
        val trainingData = prepareTrainingData()

        val model = gbm(Formula.lhs(trainingData.column(LABEL_COLUMN).name()), trainingData)

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

        Log.d("ModelService", "Checking prediction data ${predictionData.schema().fields().map { it.name }}")

        if (model != null) {

            val result = model.predict(predictionData)
            for (i in result.indices) {
                Log.d("ModelService", "Prediction $i: ${result[i]}")
            }
            Log.d("ModelService", "Prediction result: $result")

            val predictionAsInt = model.predict(predictionData)[0]

            prediction = when (predictionAsInt) {
                0 -> AffectType.NEGATIVE
                1 -> AffectType.POSITIVE
                2 -> AffectType.NONE
                else -> AffectType.NULL
            }
        }
    }

    private fun loadData(onDataLoaded: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                heartRateMeasurements = userRepository.getAllHeartRateMeasurements()
                //skinTemperatureMeasurements = userRepository.getAllSkinTemperatureMeasurements()
                affectData = userRepository.getAllAffectData()

                Log.d("ModelService", "Loaded ${heartRateMeasurements.size} heart rate measurements")
                //Log.d("ModelService", "Loaded ${skinTemperatureMeasurements.size} skin temperature measurements")
                Log.d("ModelService", "Loaded ${affectData.size} affect data entries")

                heartRateMeasurements.firstOrNull()?.let { Log.d("ModelService", "First HeartRateMeasurement: $it") }
                //skinTemperatureMeasurements.firstOrNull()?.let { Log.d("ModelService", "First SkinTemperatureMeasurement: $it") }
                affectData.firstOrNull()?.let { Log.d("ModelService", "First AffectData: $it") }

                withContext(Dispatchers.Main) {
                    onDataLoaded()
                }

            } catch (e: Exception) {
                Log.e("ModelService", "Error loading data", e)
            }
        }
    }

    private fun prepareTrainingData(): DataFrame {

        val affects = mutableListOf<Int>()
        val meanHeartRates = mutableListOf<Double>()
        val rmssds = mutableListOf<Double>()
        val meanSkinTemperatures = mutableListOf<Double>()

        val tmpAffectData = affectData.filter { it.affect != AffectType.NULL }
        val tmpHeartRateMeasurements = heartRateMeasurements.filter { it.hr.toLong() in 40L..200L }

        Log.d("ModelService", "Filtered ${tmpHeartRateMeasurements.size} heart rate measurements")

        val meanHr = tmpHeartRateMeasurements.map { it.hr.toLong() }.average()
        val stdHr = tmpHeartRateMeasurements.map { (it.hr.toLong() - meanHr).pow(2) }.average().pow(0.5)

        Log.d("ModelService", "Mean HR: $meanHr, Std HR: $stdHr")

        val ibi = tmpHeartRateMeasurements.map { 60000.0 / it.hr }
        val meanIbi = ibi.average()
        val stdIbi = ibi.map { (it - meanIbi).pow(2) }.average().pow(0.5)

        Log.d("ModelService", "Mean IBI: $meanIbi, Std IBI: $stdIbi")

        val rows = mutableListOf<DataRow>()

        for (tmpAffectDatum in tmpAffectData) {
            Log.d("ModelService", "Processing affect row: $tmpAffectDatum")

            val tmpHeartRateMeasurementsFiltered = tmpHeartRateMeasurements.filter { it.timestamp as Long in (tmpAffectDatum.timeOfNotification - DATA_WINDOW_MS)..tmpAffectDatum.timeOfNotification }

            if (tmpHeartRateMeasurementsFiltered.isEmpty()) {
                Log.d("ModelService", "No heart rate measurements found for affect data entry: $tmpAffectDatum")
                continue
            }

            val hrNormalized = tmpHeartRateMeasurementsFiltered.map { (it.hr - meanHr) / stdHr }

            val meanHrNormalized = hrNormalized.average()

            val ibiFiltered = tmpHeartRateMeasurementsFiltered.map { 60000.0 / it.hr }

            val ibiNormalized = ibiFiltered.map { (it - meanIbi) / stdIbi }

            val rmssd = calculateRmssd(ibiNormalized)

            affects.add(tmpAffectDatum.affect.ordinal)
            meanHeartRates.add(meanHrNormalized)
            rmssds.add(rmssd)
            meanSkinTemperatures.add(0.0)
        }
        val df = DataFrame.of(
            DoubleVector.of("meanHeartRateNormalized", meanHeartRates.toDoubleArray()),
            DoubleVector.of("rmssdNormalized", rmssds.toDoubleArray()),
            DoubleVector.of("meanSkinTemperatureNormalized", meanSkinTemperatures.toDoubleArray()),
            IntVector.of(LABEL_COLUMN, affects.toIntArray())
        )

//        val df = DataFrame.of(
//            DoubleVector.of("meanHeartRateNormalized", doubleArrayOf(0.5, 1.0, -0.5)),
//            DoubleVector.of("rmssdNormalized", doubleArrayOf(0.1, 0.2, 0.3)),
//            DoubleVector.of("meanSkinTemperatureNormalized", doubleArrayOf(36.5, 37.0, 36.8)),
//            IntVector.of("affect", intArrayOf(0, 1, 2)),
//            )

        Log.d("ModelService", "Prepared DataFrame: $df")

        return df
    }

    private fun preparePredictionData(): DataFrame {

        val latestTimestamp = heartRateMeasurements.lastOrNull()?.timestamp ?: 0L

        val tmpHeartRateMeasurements = heartRateMeasurements.filter { it.hr in 40..200 }

        val meanHr = tmpHeartRateMeasurements.map { it.hr.toLong() }.average()
        val stdHr = tmpHeartRateMeasurements.map { (it.hr.toLong() - meanHr).pow(2) }.average().pow(0.5)

        Log.d("ModelService", "Mean HR: $meanHr, Std HR: $stdHr")

        val ibi = tmpHeartRateMeasurements.map { 60000.0 / it.hr }
        val meanIbi = ibi.average()
        val stdIbi = ibi.map { (it - meanIbi).pow(2) }.average().pow(0.5)

        Log.d("ModelService", "Mean IBI: $meanIbi, Std IBI: $stdIbi")

        val heartRateMeasurementsFiltered = tmpHeartRateMeasurements.filter { it.timestamp >= latestTimestamp - DATA_WINDOW_MS }

        Log.d("ModelService", "Filtered ${heartRateMeasurementsFiltered.size} heart rate measurements")

        if (heartRateMeasurementsFiltered.isEmpty()) {
            Log.d("ModelService", "No heart rate measurements found")
            return DataFrame.of()
        }

        val hrNormalized = heartRateMeasurementsFiltered.map { (it.hr - meanHr) / stdHr }
        val meanHrNormalized = hrNormalized.average()
        Log.d("ModelService", "Mean HR normalized: $meanHrNormalized")

        val ibiFiltered = heartRateMeasurementsFiltered.map { 60000.0 / it.hr }
        val ibiNormalized = ibiFiltered.map { (it - meanIbi) / stdIbi }
        val rmssd = calculateRmssd(ibiNormalized)
        Log.d("ModelService", "RMSSD normalized: $rmssd")

        val dummyAffect = IntArray(1) { 0 }

        return DataFrame.of(
            DoubleVector.of("meanHeartRateNormalized", doubleArrayOf(meanHrNormalized)),
            DoubleVector.of("rmssdNormalized", doubleArrayOf(rmssd)),
            DoubleVector.of("meanSkinTemperatureNormalized", doubleArrayOf(0.0)),
            IntVector.of("affect", dummyAffect)
        )
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