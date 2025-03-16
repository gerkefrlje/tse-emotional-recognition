package com.example.phone.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.phone.R
import com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

class CommunicationListenerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("WearListenerService", "Daten empfangen")
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                when (dataItem.uri.path) {
                    "/button" -> {
                        val buttonValue = dataMap.getInt("count")
                        SharedPreferenceHelper.saveCount(applicationContext, buttonValue)
                        Log.d("WearListenerService", "Neuer Knopfwert empfangen: $buttonValue")
                        showNotification(applicationContext, "Neuer Knopfwert empfangen: $buttonValue")
                    }

                    "/hr" -> {
                        Log.d("WearListenerService", "Neue Herzfrequenz empfangen")
                        val heartRate = dataMap.getString("hr")
                        heartRate?.let {
                            SharedPreferenceHelper.saveHRListString(applicationContext, it)
                        }
                        showNotification(applicationContext, "Neue Sensordaten")

                    }

                    "/skin" -> {
                        Log.d("WearListenerService", "Neue Hauttemperatur empfangen")
                        val skinTemperature = dataMap.getString("skin")
                        Log.d("WearListenerService", "Neue Sensordaten $skinTemperature")
                        skinTemperature?.let {
                            SharedPreferenceHelper.saveSkinTemperatureString(applicationContext, it)
                        }
                    }

                }

                if (dataItem.uri.path == "/count") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val count = dataMap.getInt("count")
                    if (SharedPreferenceHelper.getCount(applicationContext) < count) {
                        SharedPreferenceHelper.saveCount(applicationContext, count)
                    }
                    Log.d("WearListenerService", "Neue Daten empfangen: $count")
                }
            }
        }
    }

    private fun showNotification(context: Context, message: String) {
        val channelId = "wearable_channel"
        val notificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Wear OS Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Daten empfangen")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }

}