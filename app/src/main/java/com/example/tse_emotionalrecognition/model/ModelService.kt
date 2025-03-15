package com.example.tse_emotionalrecognition.model

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
import android.os.IBinder
import android.util.Log
import androidx.activity.result.launch
import androidx.core.app.NotificationCompat
import androidx.work.impl.close
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.data.database.UserDataStore
import com.example.tse_emotionalrecognition.data.database.UserRepository
import com.example.tse_emotionalrecognition.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.data.database.entities.HeartRateMeasurement
import com.example.tse_emotionalrecognition.data.database.entities.SkinTemperatureMeasurement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import smile.classification.GradientTreeBoost
import smile.classification.gbm
import smile.data.DataFrame
import smile.data.Tuple
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
const val DATA_WINDOW_MS = 2 * 60 * 1000L
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

    private var prediction = 2

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        userRepository = UserDataStore.getUserRepository(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        Log.d("ModelService", "Service started")

        startForeground(
            124,
            createNotification("Model Service is running ..."),
            FOREGROUND_SERVICE_TYPE_HEALTH
        )

        when (action) {
            ACTION_TRAIN_MODEL -> {
                loadData()

                val lastTrainedTime = sharedPreferences.getLong(LAST_TRAINED_KEY, 0L)
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastTrainedTime < 24 * 60 * 60 * 1000) {
                    trainModel()
                }

                predict()

                // TODO: Link to Feedback
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
            prediction = model.predict(predictionData)[0]
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
            LongVector.of("timestamp", affectData.map { it.timeOfEngagement }.toLongArray()),
            LongVector.of("affect", affectData.map { it.affect.ordinal.toLong() }.toLongArray())
        )

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

            if (skinWindow.isEmpty()) {
                continue
            }

            val skinTemperatureNormalizedValues = skinWindow.map { it[1] as Double }
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


        val skinTemperatureNormalizedValues = skinWindow.map { it[1] as Double }
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

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, "data_collection_service")
            .setContentTitle("Data Collection Service")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }
}