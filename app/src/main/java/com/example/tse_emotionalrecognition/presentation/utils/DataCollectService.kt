package com.example.tse_emotionalrecognition.presentation.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.UserRepository
import com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement
import com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataCollectService : Service() {
    private lateinit var userRepository: com.example.tse_emotionalrecognition.common.data.database.UserRepository
    private lateinit var healthTrackingService: HealthTrackingService
    private lateinit var heartRateTracker: HealthTracker
    private lateinit var skinTemperatureTracker: HealthTracker
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        userRepository = com.example.tse_emotionalrecognition.common.data.database.UserDataStore.getUserRepository(applicationContext)
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
        Log.v("HealthTrackingService", "Service started")
        val sessionId = intent?.getLongExtra("sessionId", 0L) ?: 0L
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
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null // Da dies ein ungebundener Service ist, wird null zurückgegeben
    }
    override fun onDestroy() {
        Log.v("HealthTrackingService", "Service stopped")
        super.onDestroy()
        CoroutineScope(Dispatchers.IO).launch {
            while (
                !this@DataCollectService::heartRateTracker.isInitialized ||
                !this@DataCollectService::skinTemperatureTracker.isInitialized
            ) {
                Log.v("HealthTrackingService", "Waiting for trackers to initialize...")
                kotlinx.coroutines.delay(100L)
            }
            heartRateTracker.flush()
            skinTemperatureTracker.flush()
            kotlinx.coroutines.delay(1000L)
            heartRateTracker.unsetEventListener()
            skinTemperatureTracker.unsetEventListener()
            kotlinx.coroutines.delay(1000L)
            healthTrackingService.disconnectService()
            kotlinx.coroutines.delay(1000L)
        }
        stopForeground(true)
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



fun buildTrackerEventListener(repository: com.example.tse_emotionalrecognition.common.data.database.UserRepository, sessionId: Long, type: HealthTrackerType, context: Context
): HealthTracker.TrackerEventListener {
    return object : HealthTracker.TrackerEventListener {
        override fun onDataReceived(list: List<DataPoint>) {
            Log.v("data","logged")

            if (type == HealthTrackerType.HEART_RATE_CONTINUOUS && list.isNotEmpty()) {
                val entries = mutableListOf<com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement>()
                for (dataPoint in list) {
                    entries.add(
                        com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement(
                            0L,
                            sessionId,
                            dataPoint.timestamp.toLong(),
                            dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE),
                            dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS)
                        )
                    )
                    Log.v("data", "Heart rate: ${dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE)}")
                }
                repository.insertHeartRateMeasurementList(
                    CoroutineScope(Dispatchers.IO),
                    entries
                )
            } else if (type == HealthTrackerType.SKIN_TEMPERATURE_CONTINUOUS && list.isNotEmpty()){
                val entries = mutableListOf<com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement>()
                for (dataPoint in list) {
                    entries.add(
                        com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement(
                            0L,
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