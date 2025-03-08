package com.example.tse_emotionalrecognition.presentation.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.content.ContextCompat
import androidx.work.ForegroundInfo
import com.example.tse_emotionalrecognition.presentation.AppPhase
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DataCollectWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    companion object {
        const val CHANNEL_ID = "DataCollectChannel"
        const val NOTIFICATION_ID = 1
    }


    override fun doWork(): Result {
        Log.d("DataCollectWorker", "Worker started")

        createNotificationChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Data Collection")
            .setContentText("Collecting data in the background...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        val foregroundInfo = ForegroundInfo(NOTIFICATION_ID, notification)

        setForeground(foregroundInfo)

        val phase = getAppPhase(context)

        val sessionId = Calendar.getInstance().timeInMillis
        val intent = Intent(applicationContext, DataCollectService::class.java)

        intent.putExtra("COLLECT_DATA", true)
        intent.putExtra("sessionId", sessionId)
        intent.putExtra("PHASE", phase.name)
        ContextCompat.startForegroundService(applicationContext, intent)

        return Result.success()
    }

    private fun getAppPhase(context: Context): AppPhase {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val firstLaunchTime = sharedPreferences.getLong("first_launch_time", 0L)
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - firstLaunchTime
        val daysElapsed = TimeUnit.MILLISECONDS.toDays(elapsedTime)

        return when {
            daysElapsed < 2 -> AppPhase.INITIAL_COLLECTION
            daysElapsed < 5 -> AppPhase.PREDICTION_WITH_FEEDBACK
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