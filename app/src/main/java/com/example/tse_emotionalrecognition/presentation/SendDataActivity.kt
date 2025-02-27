package com.example.tse_emotionalrecognition.presentation

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.data.database.UserDataStore
import com.example.tse_emotionalrecognition.data.database.entities.HeartRateMeasurement
import com.example.tse_emotionalrecognition.presentation.theme.TSEEmotionalRecognitionTheme
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class SendDataActivity : ComponentActivity(){


    private val userRepository by lazy { UserDataStore.getUserRepository(application) }
    private val sessionId = 1

    // State in der Activity deklarieren

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)


        setContent {
            var wearCount by remember { mutableIntStateOf(0) }
            wearCount = sharedPreferences.getInt("button_count", 0)
            Screen(wearCount = wearCount, onCountChange = { newCount ->
                wearCount = newCount
                with(sharedPreferences.edit()) {
                    putInt("button_count", wearCount)
                    apply()
                }
            })
        }
    }




//    private fun startCollection() {
//        val collectionServiceIntent = Intent(this, DataCollectService::class.java)
//        collectionServiceIntent.putExtra("sessionId", sessionId)
//        startService(collectionServiceIntent)
//    }


//    override fun onDestroy() {
//        super.onDestroy()
//        stopService(Intent(this, DataCollectService::class.java))
//
//    }

}


@Composable
fun Screen(wearCount: Int, onCountChange: (Int) -> Unit) {
    TSEEmotionalRecognitionTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.TopCenter
        ) {
            TimeText()
            Greeting(wearCount = wearCount, onCountChange = onCountChange)

        }
    }
}


@Composable
fun Greeting(wearCount: Int = 0, onCountChange: (Int) -> Unit) {

    val context = androidx.compose.ui.platform.LocalContext.current
    val userRepository = UserDataStore.getUserRepository(context.applicationContext)
    var heartRateMeasurements by remember { mutableStateOf<List<HeartRateMeasurement>>(emptyList()) }

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(key1 = true) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                heartRateMeasurements = userRepository.getHeartRateMeasurements()
            } catch (e: Exception) {
                Log.e("Error", "Error fetching heart rate measurements", e)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = stringResource(R.string.hello_world, "Test")
        )
        Button(

            onClick = {
                val count = wearCount + 1
                onCountChange(count)

//                val nodeClient = Wearable.getNodeClient(context)
//                nodeClient.connectedNodes.addOnSuccessListener { nodes ->
//                    if (nodes.isEmpty()) {
//                        Log.e("WearableDebug", "Keine verbundenen Geräte gefunden!")
//                    } else {
//                        for (node in nodes) {
//                            Wearable.getMessageClient(context)
//                                .sendMessage(node.id, "/message_path", count.toString().toByteArray())
//                                .addOnSuccessListener {
//                                    Log.d("WearableMessage", "Nachricht erfolgreich gesendet: $count")
//                                }
//                                .addOnFailureListener {
//                                    Log.e("WearableMessage", "Fehler beim Senden der Nachricht", it)
//                                }
//                        }
//                    }
//                }

                // Daten senden
                val dataClient = Wearable.getDataClient(context)
//                val putDataRequest = PutDataMapRequest.create("/count").apply {
//                    dataMap.putInt("count", count)
//                }.asPutDataRequest().setUrgent()
//                dataClient.putDataItem(putDataRequest)
                    val putDataRequest = PutDataMapRequest.create("/button").apply {
                        dataMap.putInt("count", count)
                        dataMap.putLong("timeStamp", System.currentTimeMillis())
                    }.asPutDataRequest().setUrgent()
                    dataClient.putDataItem(putDataRequest).addOnSuccessListener {
                        Log.d("MainActivity", "Data sent $count")
                    }.addOnFailureListener { e ->
                        Log.e("MainActivity", "Error sending data", e)
                    }
                Log.d("MainActivity", "Send Data: $count")


//                if (count % 10 == 0) {
//                    for (measurement in heartRateMeasurements) {
//                        Log.v("HeartRateMeasurement", "$measurement")
//                        val sendHR = PutDataMapRequest.create("/hr").apply {
//                            dataMap.putString(
//                                "hr",
//                                Json.encodeToString(
//                                    HeartRateMeasurement.serializer(),
//                                    measurement
//                                )
//                            )
//                            dataMap.putString("timeStamp", System.currentTimeMillis().toString())
//                        }.asPutDataRequest()
//                        dataClient.putDataItem(sendHR)
//                    }
//
//                }

            },
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Text(text = "Count: ")
                Text(text = wearCount.toString())
            }
        }

    }
}


