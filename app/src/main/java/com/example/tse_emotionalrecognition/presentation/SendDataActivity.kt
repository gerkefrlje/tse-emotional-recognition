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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement
import com.example.tse_emotionalrecognition.common.data.database.entities.InterventionStats
import com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement
import com.example.tse_emotionalrecognition.common.data.database.entities.TAG
import com.example.tse_emotionalrecognition.common.data.database.utils.CommunicationDataSender
import com.example.tse_emotionalrecognition.presentation.MainActivity.Companion.trackerID
import com.example.tse_emotionalrecognition.presentation.interventions.BreathingActivity
import com.example.tse_emotionalrecognition.presentation.interventions.CallInterventionActivity
import com.example.tse_emotionalrecognition.presentation.theme.TSEEmotionalRecognitionTheme
import com.example.tse_emotionalrecognition.presentation.utils.DataCollectService
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class SendDataActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {


    /**
     * Todo for communication between phone and watch:
     *  create "common" module which contains classes needed fro both phone and watch app
     *  check if data can be send in background, right now it is mandatory to have both activity open
     *  create database on phone to be able to save the sent data from the watch
     *  chose better communication between phone and watch f.e MessageClient, DataClient or other options -> look in documentation
     */

    private val sessionId = 1

    // State in der Activity deklarieren

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        startCollection()

        //Necessary for recieving messages from phone
        Wearable.getMessageClient(this).addListener(this)
        // look in phone/MainActivty for the phone implementation

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

    // when message from phone is recieved this function is called
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/emotion") {
            val emotion = String(messageEvent.data)
            Log.d("WearableReceiver", "Empfangene Emotion: $emotion")

            //following code could also be written with if and else statements
            when (emotion) { // only for development purposes to test different interventions
                //starts call inbńervention when the sad button is pressed on the phoen
                "sad" -> startActivity(Intent(this, CallInterventionActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                //starts breathing intervention when the angry button is pressed on the phone
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
    val userRepository =
        com.example.tse_emotionalrecognition.common.data.database.UserDataStore.getUserRepository(
            context.applicationContext
        )
    var heartRateMeasurements by remember {
        mutableStateOf<List<com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement>>(
            emptyList()
        )
    }
    var skinTemperatureMeasurements by remember {
        mutableStateOf<List<com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement>>(
            emptyList()
        )
    }
    val lifecycleOwner = LocalLifecycleOwner.current


    fun compressByteArray(data: ByteArray): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val gzipOutputStream = GZIPOutputStream(byteArrayOutputStream)
        gzipOutputStream.write(data)
        gzipOutputStream.close()
        return byteArrayOutputStream.toByteArray()
    }


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
                val interventionStats = InterventionStats(id = trackerID, tag = TAG.INTERVENTIONS)
                userRepository.insertInterventionStats(CoroutineScope(Dispatchers.IO), interventionStats)
                val sender = CommunicationDataSender(context)
                val interventionStatsString = Json.encodeToString(interventionStats)
                sender.sendStringData("/phone/notification", interventionStatsString)

            },
        ) { Text("reset Intervention")}
        Button(

            onClick = {
                val count = wearCount + 1
                onCountChange(count)


                val dataClient =
                    Wearable.getDataClient(context) //dataClient in this case is the phone
                val putDataRequest = PutDataMapRequest.create("/button").apply {
                    dataMap.putInt("count", count)
                    dataMap.putLong("timeStamp", System.currentTimeMillis())
                }.asPutDataRequest()
                    .setUrgent() //this request creates a "package" which should be send to phone
                dataClient.putDataItem(putDataRequest).addOnSuccessListener { // sends data to phone
                    Log.d("MainActivity", "Data sent $count") // for debug purposes
                }.addOnFailureListener { e -> // can be removed in when app is finished
                    Log.e("MainActivity", "Error sending data", e)
                }
                Log.d("MainActivity", "Send Data: $count")

                userRepository.incrementDismissed(CoroutineScope(Dispatchers.IO), MainActivity.trackerID)



                val sender =
                    com.example.tse_emotionalrecognition.common.data.database.utils.CommunicationDataSender(
                        context
                    )

                //sender.sendIntData("/phone/notification", count)

                if (heartRateMeasurements.isNotEmpty()) {

                    // Convert the list of HeartRateMeasurement objects to a JSON string
                    // Use the kotlinx.serialization library for this
                    val jsonString =
                        Json.encodeToString(heartRateMeasurements) // currently limited to 20 measurements due to limitations

                        val byteArray = jsonString.toByteArray(Charsets.UTF_8)

                        val compressedByteArray = compressByteArray(byteArray)

                    sender.sendStringData("/phone/hr", jsonString)
                }

                if (skinTemperatureMeasurements.isNotEmpty()) {
                    val jsonString =
                        Json.encodeToString(skinTemperatureMeasurements) // Ganze Liste

                    sender.sendStringData("/phone/skin", jsonString)
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


