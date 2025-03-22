package com.example.tse_emotionalrecognition.common.data.database.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf

interface DataSendListener {
    fun onDataSent(path: String, data: Any?)
    fun onDataSendFailed(path: String, data: Any?, exception: Exception)
}

class CommunicationDataSender(private val context: Context) {

    private val dataClient = Wearable.getDataClient(context)

    fun sendStringData(path: String, data: String) {
        sendData(path, data)
    }

    fun sendIntData(path: String, data: Int) {
        sendData(path, data)
    }

    fun sendLongData(path: String, data: Long) {
        sendData(path, data)
    }

    private fun sendData(path: String, data: Any) {
        val putDataRequest = PutDataMapRequest.create(path).apply {
            when (data) {
                is String -> dataMap.putString("data", data)
                is Int -> dataMap.putInt("data", data)
                is Long -> dataMap.putLong("data", data)
                else -> throw IllegalArgumentException("Ungültiger Datentyp: ${data::class.java}")
            }
            dataMap.putLong("timeStamp", System.currentTimeMillis())
        }.asPutDataRequest()

        dataClient.putDataItem(putDataRequest).addOnSuccessListener {
            Log.d("WearableDataSender", "Daten gesendet an Pfad $path: $data")
        }.addOnFailureListener { e ->
            Log.e("WearableDataSender", "Fehler beim Senden der Daten an Pfad $path", e)
        }
    }
}