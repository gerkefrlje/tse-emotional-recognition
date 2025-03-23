package com.example.tse_emotionalrecognition.presentation.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.core.content.ContextCompat
import androidx.work.ForegroundInfo
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.presentation.AppPhase
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DataCollectWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    companion object {
        const val CHANNEL_ID = "DataCollectChannel"
    }


    override suspend fun doWork(): Result {
        Log.d("DataCollectWorker", "Worker started")

        createNotification()

        if(isFirstRun()) {
            startDataCollectionService()
        }
        else{
            createNotification()
        }

        return Result.success()
    }

    private fun isFirstRun() : Boolean{
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstRun = sharedPref.getBoolean("first_run", true)

        if (isFirstRun) {
            // Setze den Wert auf false, damit es nur einmal passiert
            sharedPref.edit().putBoolean("first_run", false).apply()
        }
        return isFirstRun
    }

    private fun startDataCollectionService() {

        val phase = getAppPhase(context)

        Log.d("DataCollectWorker", "Creating Intent for DataCollectService")
        val sessionId = Calendar.getInstance().timeInMillis
        val intent = Intent(applicationContext, DataCollectService::class.java)

        intent.putExtra("COLLECT_DATA", true)
        intent.putExtra("sessionId", sessionId)
        intent.putExtra("PHASE", phase.name)

        Log.d("DataCollectWorker", "Starting DataCollectService")
        ContextCompat.startForegroundService(applicationContext, intent)
    }

    private fun createNotification() {
        val sessionId = Calendar.getInstance().timeInMillis
        val phase = getAppPhase(context)

        val notificationIntent = Intent(context, DataCollectReciever::class.java).apply {
            putExtra("COLLECT_DATA", true)
            putExtra("PHASE", phase)
            putExtra("sessionId", sessionId)
        }

        val pedingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val intent = Intent(context, DataCollectReciever::class.java).apply {
            putExtra("COLLECT_DATA", true)
            putExtra("PHASE", phase)
            putExtra("sessionId", sessionId)
        }

        //context.sendBroadcast(intent)


        val pendingIntent = PendingIntent.getService(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "data_collection_request",
            "Data Collection Request",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)


        val broadcastAction = NotificationCompat.Action.Builder(
            R.drawable.splash_icon,
            "Start data collection",
            pedingIntent
        ).build()

        val broadcastNotification = NotificationCompat.Builder(context, "data_collection_request")
            .setContentTitle("Start data collection")
            .setContentText("Press to start the collection")
            .setSmallIcon(R.drawable.splash_icon)
            .addAction(broadcastAction)
            .setAutoCancel(true)
            .build()

        //notificationManager.notify(1, notification)
        notificationManager.notify(69, broadcastNotification)
    }

    private fun getAppPhase(context: Context): AppPhase {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val firstLaunchTime = sharedPreferences.getLong("first_launch_time", 0L)
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - firstLaunchTime
        val daysElapsed = TimeUnit.MILLISECONDS.toDays(elapsedTime)

        return when {
            daysElapsed < 1 -> AppPhase.INITIAL_COLLECTION
            daysElapsed < 2 -> AppPhase.PREDICTION_WITH_FEEDBACK
            else -> AppPhase.PREDICTION_ONLY
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Data Collection",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}