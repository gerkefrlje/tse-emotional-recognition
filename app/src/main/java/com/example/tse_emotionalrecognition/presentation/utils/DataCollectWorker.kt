package com.example.tse_emotionalrecognition.presentation.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.content.ContextCompat
import java.util.Calendar

class DataCollectWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        Log.d("DataCollectWorker", "Worker started")
        val sessionId = Calendar.getInstance().timeInMillis
        val intent = Intent(applicationContext, DataCollectService::class.java)

        intent.putExtra("COLLECT_DATA", true)
        intent.putExtra("sessionId", sessionId)
        ContextCompat.startForegroundService(applicationContext, intent)

        return Result.success()
    }
}