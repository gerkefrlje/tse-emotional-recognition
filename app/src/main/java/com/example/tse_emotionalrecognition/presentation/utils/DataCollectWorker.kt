package com.example.tse_emotionalrecognition.presentation.utils

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
        const val NOTIFICATION_ID = 1
    }


    override suspend fun doWork(): Result {
        Log.d("DataCollectWorker", "Worker started")

        createNotification()

//        if(isFirstRun()) {
//            startDataCollectionService()
//        }
//        else{
//            createNotification()
//        }
/**
        //createNotificationChannel()

//        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setContentTitle("Data Collection")
//            .setContentText("Collecting data in the background...")
//            .setSmallIcon(android.R.drawable.ic_dialog_info)
//            .build()
//
//        Log.d("DataCollectWorker", "Creating foreground info")
//        //val foregroundInfo = ForegroundInfo(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_HEALTH)
//
//        Log.d("DataCollectWorker", "Setting foreground info")
//        setForegroundAsync(createForegroundInfo())
//
//        val phase = getAppPhase(context)
//
//        Log.d("DataCollectWorker", "Creating Intent for DataCollectService")
//        val sessionId = Calendar.getInstance().timeInMillis
//        val intent = Intent(applicationContext, DataCollectService::class.java)
//
//        intent.putExtra("COLLECT_DATA", true)
//        intent.putExtra("sessionId", sessionId)
//        intent.putExtra("PHASE", phase.name)
//
//        Log.d("DataCollectWorker", "Starting DataCollectService")
//        //ContextCompat.startForegroundService(applicationContext, intent)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            applicationContext.startForegroundService(intent) // WICHTIG: `startForegroundService()` statt `startService()`
//        } else {
//            applicationContext.startService(intent)
//        }

        Log.d("DataCollectWorker", "Worker finished")
        Log.d("DataCollectWorker", "Result is " + Result.success())
**/
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

        Log.d("DataCollectWorker", "Worker finished")
        Log.d("DataCollectWorker", "Result is " + Result.success())

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