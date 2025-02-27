package com.example.tse_emotionalrecognition.presentation.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.CountDownTimer
import androidx.core.content.ContextCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.data.database.UserDataStore
import com.example.tse_emotionalrecognition.data.database.UserRepository
import com.example.tse_emotionalrecognition.data.database.entities.HeartRateMeasurement
import com.example.tse_emotionalrecognition.data.database.entities.SkinTemperatureMeasurement
import com.example.tse_emotionalrecognition.presentation.LabelActivity
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DataCollectService : Service() {
    private lateinit var userRepository: UserRepository
    private lateinit var healthTrackingService: HealthTrackingService
    private lateinit var heartRateTracker: HealthTracker
    private lateinit var skinTemperatureTracker: HealthTracker
    private lateinit var wearDetectionHelper: WearDetectionHelper

    private var countDownTimer: CountDownTimer? = null
    private val dataCollectionInterval: Long = 2L * 60L * 1000L // 2 Minutes
    private var isWatchWorn: Boolean = false
    private var sessionId: Long = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        userRepository = UserDataStore.getUserRepository(applicationContext)
        wearDetectionHelper = WearDetectionHelper(this)
        healthTrackingService = HealthTrackingService(
            object : ConnectionListener {
            override fun onConnectionSuccess() {
                Log.v("data", "connected")
                heartRateTracker = healthTrackingService.getHealthTracker(HealthTrackerType.HEART_RATE_CONTINUOUS)
                skinTemperatureTracker = healthTrackingService.getHealthTracker(HealthTrackerType.SKIN_TEMPERATURE_CONTINUOUS)
            }
            override fun onConnectionEnded() {
                Log.v("connect","ended")
            }
            override fun onConnectionFailed(e: HealthTrackerException) { }
        }, applicationContext)
        healthTrackingService.connectService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DataCollectService", "Service started")

        sessionId = intent?.getLongExtra("sessionId", 0L) ?: 0L
        val shouldCollectData = intent?.getBooleanExtra("COLLECT_DATA", false) ?: false

        if (shouldCollectData) {
            wearDetectionHelper.start { isWorn ->
                isWatchWorn = isWorn
                if (isWatchWorn) {
                    startDataCollection(intent)
                    startTimer()
                } else {
                    Log.d("DataCollectService", "Watch is not worn, skipping data collection")
                    stopSelf()
                }
            }
        } else {
            Log.d("DataCollectService", "Service started without data collection")
            stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Da dies ein ungebundener Service ist, wird null zurückgegeben
    }

    override fun onDestroy() {
        Log.v("HealthTrackingService", "Service stopped")
        super.onDestroy()
        stopDataCollection()
        countDownTimer?.cancel()
        wearDetectionHelper.stop()
        stopForeground(true)
    }

    private fun startDataCollection(intent: Intent?) {

        startForeground(
            123,
            createNotification("Health data collection is running...")
        )

        CoroutineScope(Dispatchers.IO).launch {
            while (
                !this@DataCollectService::heartRateTracker.isInitialized ||
                !this@DataCollectService::skinTemperatureTracker.isInitialized
            ) {
                Log.v("HealthTrackingService", "Waiting for trackers to initialize...")
                kotlinx.coroutines.delay(100L) // Warte auf Initialisierung
            }
            heartRateTracker.setEventListener(buildTrackerEventListener(userRepository,sessionId, HealthTrackerType.HEART_RATE_CONTINUOUS, applicationContext))
            skinTemperatureTracker.setEventListener(buildTrackerEventListener(userRepository,sessionId, HealthTrackerType.SKIN_TEMPERATURE_CONTINUOUS, applicationContext))
        }

        Log.d("DataCollectService", "Data collection started")
    }

    private fun stopDataCollection() {
        CoroutineScope(Dispatchers.IO).launch {
            if(this@DataCollectService::heartRateTracker.isInitialized) {
                heartRateTracker.flush()
                heartRateTracker.unsetEventListener()
            }
            if(this@DataCollectService::skinTemperatureTracker.isInitialized) {
                skinTemperatureTracker.flush()
                skinTemperatureTracker.unsetEventListener()
            }

            delay(1000L)
            healthTrackingService.disconnectService()
            delay(1000L)
        }
        Log.d("DataCollectService", "Data collection stopped")
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(dataCollectionInterval, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.v("DataCollectService", "Time remaining: ${millisUntilFinished / 1000}")
            }

            override fun onFinish() {
                Log.v("DataCollectService", "Data collection finished")
                stopDataCollection()
                launchLabelActivity()
                stopSelf()
            }
        }

        countDownTimer?.start()
    }

    private fun launchLabelActivity() {
        val intent = Intent(this, LabelActivity::class.java)
        intent.putExtra("sessionId", sessionId)
        ContextCompat.startActivity(this, intent, null)
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, "data_collection_service")
            .setContentTitle("Data Collection Service")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "data_collection_service",
                "Data Collection Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

fun buildTrackerEventListener(repository: UserRepository, sessionId: Long, type: HealthTrackerType, context: Context
): HealthTracker.TrackerEventListener {
    return object : HealthTracker.TrackerEventListener {
        override fun onDataReceived(list: List<DataPoint>) {
            Log.v("data","logged")

            if (type == HealthTrackerType.HEART_RATE_CONTINUOUS && list.isNotEmpty()) {
                val entries = mutableListOf<HeartRateMeasurement>()
                for (dataPoint in list) {
                    entries.add(
                        HeartRateMeasurement(
                            0L,
                            sessionId,
                            dataPoint.timestamp.toLong(),
                            dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE),
                            dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS))
                    )
                }
                repository.insertHeartRateMeasurementList(
                    CoroutineScope(Dispatchers.IO),
                    entries
                )
            } else if (type == HealthTrackerType.SKIN_TEMPERATURE_CONTINUOUS && list.isNotEmpty()){
                val entries = mutableListOf<SkinTemperatureMeasurement>()
                for (dataPoint in list) {
                    entries.add(
                        SkinTemperatureMeasurement(0L,
                            sessionId,
                            dataPoint.timestamp,
                            dataPoint.getValue(ValueKey.SkinTemperatureSet.OBJECT_TEMPERATURE),
                            dataPoint.getValue(ValueKey.SkinTemperatureSet.AMBIENT_TEMPERATURE),
                            dataPoint.getValue(ValueKey.SkinTemperatureSet.STATUS)
                            )
                    )
                }
                repository.insertSkinTemperatureMeasurementList(
                    CoroutineScope(Dispatchers.IO),
                    entries
                )
            }
        }

        override fun onFlushCompleted() {
            Log.v("data", "data storing finished")
        }

        override fun onError(trackerError: HealthTracker.TrackerError) {
            Log.v("data", "error Data")
        }
    }
}