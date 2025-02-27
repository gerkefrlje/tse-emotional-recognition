package com.example.tse_emotionalrecognition.presentation

import android.content.Context
import android.content.Intent
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
import com.example.tse_emotionalrecognition.data.database.entities.SkinTemperatureMeasurement
import com.example.tse_emotionalrecognition.presentation.interventions.BreathingActivity
import com.example.tse_emotionalrecognition.presentation.interventions.CallInterventionActivity
import com.example.tse_emotionalrecognition.presentation.theme.TSEEmotionalRecognitionTheme
import com.example.tse_emotionalrecognition.presentation.utils.DataCollectService
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SendDataActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {


    private val userRepository by lazy { UserDataStore.getUserRepository(application) }
    private val sessionId = 1

    // State in der Activity deklarieren

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        startCollection()

        Wearable.getMessageClient(this).addListener(this)

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


    private fun startCollection() {
        val collectionServiceIntent = Intent(this, DataCollectService::class.java)
        collectionServiceIntent.putExtra("sessionId", sessionId)
        startService(collectionServiceIntent)
    }


    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, DataCollectService::class.java))
        Log.d("WearableReceiver", "DataClient wird deaktiviert...")
        Wearable.getMessageClient(this)
            .removeListener(this) // Listener entfernen, um Speicherlecks zu vermeiden

    }

    override fun onResume() {
        super.onResume()
        Log.d("WearableReceiver", "DataClient wird registriert...")
        Wearable.getMessageClient(this).addListener(this) // Listener aktivieren
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/emotion") {
            val emotion = String(messageEvent.data)
            Log.d("WearableReceiver", "Empfangene Emotion: $emotion")

            when (emotion) {
                "sad" -> startActivity(Intent(this, CallInterventionActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                "angry" -> startActivity(Intent(this, BreathingActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }

    }
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
    var skinTemperatureMeasurements by remember {
        mutableStateOf<List<SkinTemperatureMeasurement>>(
            emptyList()
        )
    }
    val lifecycleOwner = LocalLifecycleOwner.current


    LaunchedEffect(Unit) {  // `Unit` statt `true`, um unnötige Re-Compositions zu vermeiden
        lifecycleOwner.lifecycleScope.launch {
            try {
                heartRateMeasurements = userRepository.getHeartRateMeasurements()
                skinTemperatureMeasurements = userRepository.getSkinTemperatureMeasurements()
            } catch (e: Exception) {
                Log.e("HeartRateScreen", "Error fetching heart rate measurements", e)
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

                // Daten senden
                val dataClient = Wearable.getDataClient(context)
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



                Log.v("MainActivity", "count % 10: ${count % 10}")
                if (count % 10 == 0) {

                    if (heartRateMeasurements.isNotEmpty()) {

                        val jsonString =
                            Json.encodeToString(heartRateMeasurements.takeLast(20)) // Ganze Liste serialisieren

                        val sendHR = PutDataMapRequest.create("/hr").apply {
                            dataMap.putString("hr", jsonString) // JSON-String speichern
                            dataMap.putLong("timeStamp", System.currentTimeMillis())
                        }.asPutDataRequest()

                        dataClient.putDataItem(sendHR)
                        Log.d("WearableMessage", "Herzfrequenz-Daten gesendet: $jsonString")
                    }

                    if (skinTemperatureMeasurements.isNotEmpty()) {
                        val jsonString =
                            Json.encodeToString(skinTemperatureMeasurements) // Ganze Liste
                        val sendSkin = PutDataMapRequest.create("/skin").apply {
                            dataMap.putString("skin", jsonString) // JSON-String speichern
                            dataMap.putLong("timeStamp", System.currentTimeMillis())
                        }.asPutDataRequest()
                        dataClient.putDataItem(sendSkin)
                        Log.d("WearableMessage", "Hauttemperatur-Daten gesendet: $jsonString")
                    }

                }

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


