package com.example.tse_emotionalrecognition.data.collection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DataCollectReciever : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val serviceIntent = Intent(it, DataCollectService::class.java).apply {
                putExtra("COLLECT_DATA", intent?.getBooleanExtra("COLLECT_DATA", false))
                putExtra("PHASE", intent?.getSerializableExtra("PHASE"))
                putExtra("sessionId", intent?.getLongExtra("sessionId", 0L))
                putExtra("debug", intent?.getBooleanExtra("debug", false))
            }
            Log.d("DataCollectReciever", "Starting DataCollectService")
            it.startForegroundService(serviceIntent)  // Required for Android 8+
        }
    }
}

