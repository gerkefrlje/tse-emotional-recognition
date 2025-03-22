package com.example.tse_emotionalrecognition.common.data.database.utils

import android.util.Log
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.UserRepository
import com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement
import com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class CommunicationListenerService : WearableListenerService() {

    private lateinit var userRepository: UserRepository

    private lateinit var applicationContextSafe: android.content.Context // speichere den Context nach der Initialisierung

    override fun onCreate() {
        super.onCreate()
        applicationContextSafe = applicationContext // Initialisierung hier
        userRepository = UserDataStore.getUserRepository(applicationContextSafe)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("WearListenerService", "Daten empfangen")
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                val path = dataItem.uri.path
                Log.d("WearListenerService", "Pfad: $path")

                path?.let {
                    val parts = it.split("/")
                    if (parts.size > 2) {
                        val prefix = parts[1]
                        Log.d("WearListenerService", "Prefix: $prefix")
                        val remainingPath = "/${parts[2]}"
                        Log.d("WearListenerService", "Remaining Path: $remainingPath")


                        when (prefix) {
                            "phone" -> {
                                getPhoneData(remainingPath, dataMap)
                            }

                            "watch" -> {
                                getWatchData(remainingPath, dataMap)
                            }

                            else -> {
                                Log.d("WearListenerService", "Ungültiger Pfad: $path")
                            }
                        }
                    } else {
                        Log.d("WearListenerService", "Ungültiger Pfad: $path")
                    }
                }

            }
        }
    }

    private fun getPhoneData(remainingPath: String, dataMap: DataMap) {
        when (remainingPath) {
            "/button" -> {
                val buttonValue = dataMap.getInt("count")
                Log.d("WearListenerService", "Neuer Knopfwert empfangen: $buttonValue")
            }

            "/hr" -> {
                Log.d("WearListenerService", "Neue Herzfrequenz empfangen")
                val heartRate = dataMap.getString("data")
                heartRate?.let {
                    val hrList = Json.decodeFromString<List<HeartRateMeasurement>>(it)
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d("WearListenerService", "Trying to store data")
                        userRepository.insertHeartRateMeasurementList(
                            CoroutineScope(Dispatchers.IO),
                            hrList.toMutableList()
                        )
                    }
                }
                Log.d("WearListenerService", "Neue Sensordaten")
            }

            "/skin" -> {
                Log.d("WearListenerService", "Neue Hauttemperatur empfangen")
                val skinTemperature = dataMap.getString("data")
                Log.d("WearListenerService", "Neue Sensordaten $skinTemperature")
                skinTemperature?.let {
                    Log.d("WearListenerService", "Trying to store skin data")
                    val stList = Json.decodeFromString<List<SkinTemperatureMeasurement>>(it)
                    CoroutineScope(Dispatchers.IO).launch {
                        userRepository.insertSkinTemperatureMeasurementList(
                            CoroutineScope(
                                Dispatchers.IO
                            ), stList.toMutableList()
                        )
                    }
                }
            }
        }
    }

    private fun getWatchData(remainingPath: String, dataMap: DataMap) {

    }
}