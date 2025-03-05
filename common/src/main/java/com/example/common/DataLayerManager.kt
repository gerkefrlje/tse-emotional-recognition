package com.example.common

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import java.time.Instant

class DataLayerManager(context: Context) : DataClient.OnDataChangedListener {

    private val dataClient: DataClient = Wearable.getDataClient(context)

    private var listener: DataListener? = null

    interface DataListener {
        fun onDataReceived(message: String, timestamp: Long)
    }

    fun setListener(listener: DataListener) {
        this.listener = listener
    }

    fun clearListener() {
        this.listener = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessageToCompanion(message: String) {
        val putDataMapRequest = PutDataMapRequest.create(DataLayerContract.DATA_ITEM_PATH)
        val dataMap = putDataMapRequest.dataMap
        dataMap.putString(DataLayerContract.KEY_MESSAGE, message)
        dataMap.putLong(DataLayerContract.KEY_TIMESTAMP, Instant.now().toEpochMilli())

        val request = putDataMapRequest.asPutDataRequest()
        dataClient.putDataItem(request)
            .addOnSuccessListener { Log.d("DataLayerManager", "Message sent successfully") }
            .addOnFailureListener { e -> Log.e("DataLayerManager", "Failed to send message", e) }
    }

    fun addListener() {
        dataClient.addListener(this)
    }

    fun removeListener() {
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == DataLayerContract.DATA_ITEM_PATH) {
                    val dataMapItem = DataMapItem.fromDataItem(dataItem)
                    val dataMap = dataMapItem.dataMap
                    val message = dataMap.getString(DataLayerContract.KEY_MESSAGE)
                    val timestamp = dataMap.getLong(DataLayerContract.KEY_TIMESTAMP)
                    Log.d("DataLayerManager", "Received message: $message at $timestamp")
                    if (message != null) {
                        listener?.onDataReceived(message, timestamp)
                    }
                }
            }
        }
    }
}