package com.example.phone

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.phone.ui.theme.TSEEmotionalRecognitionTheme
import com.example.phone.utils.HealthDataViewModelFactory
import com.example.phone.utils.HealthViewModel
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.UserRepository
import com.example.phone.utils.HealthDataViewModelFactory
import com.example.phone.utils.HealthViewModel
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.UserRepository
import com.google.android.gms.wearable.Wearable
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiveDataActivity : ComponentActivity() {//, DataClient.OnDataChangedListener

    /**
     * Todo for communication between phone and watch:
     *  create "common" module which contains classes needed fro both phone and watch app
     *  check if data can be send in background, right now it is mandatory to have both activity open
     *  create database on phone to be able to save the sent data from the watch
     *  chose better communication between phone and watch f.e MessageClient, DataClient or other options -> look in documentation
     */

    var wearCount by mutableIntStateOf(0) // State in der Activity deklarieren
    private lateinit var userRepository: UserRepository

    private val viewModel: HealthViewModel by viewModels {
        HealthDataViewModelFactory(UserDataStore.getUserRepository(applicationContext))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userRepository = UserDataStore.getUserRepository(applicationContext)
        //Necessary for recieving data from watch
        //look in app/SendDataActivity for watch implementation
        //Wearable.getDataClient(this).addListener(this)


        enableEdgeToEdge()
        setContent {
            val heartRateList by viewModel.heartRateList.observeAsState(initial = emptyList())
            Log.d("WearableReceiver", "HeartRateList: $heartRateList")
            val skinTemperatureList by viewModel.skinTemperatureList.observeAsState(initial = emptyList())
            TSEEmotionalRecognitionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val standardModifier = Modifier.padding(innerPadding)

                    LazyRow(modifier = standardModifier) {
                        item {
                            Greeting(
                                modifier = standardModifier,
                                count = wearCount,
                                this@ReceiveDataActivity
                            )

                        }
                        item {
                            HeartRateView(
                                heartRateList = heartRateList
                            )
                        }

                        item {
                            }

                        item {
                            }

                        item {SkinTemperatureView(
                                skinTemperatureList = skinTemperatureList
                            )

                        }


                    }
                }
            }

        }
        // for debug purposes
        // checks if there is a phone connected to the watch
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                Log.d("WearableReceiver", "Verbundene Geräte: ${nodes.map { it.displayName }}")
            }
            .addOnFailureListener {
                Log.e(
                    "WearableReceiver",
                    "Fehler beim Abrufen der Nodes",
                    it
                )
            }

    }



    /**
    // when data from phone is recieved this function is called
    override fun onDataChanged(dataEvents: DataEventBuffer) {
    Log.d("WearableReceiver", "DataEventBuffer received!")
    for (event in dataEvents) {
    if (event.type == DataEvent.TYPE_CHANGED) {// checks for data event. look in documention for different options
    val dataItem = event.dataItem // gets data from event -> data which was send
    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
    when (dataItem.uri.path) { // now checks which data was send
    //                    "/button" -> {
    //                        val count = dataMap.getInt("count") // compare to data send
    //                        wearCount = count
    //                        Log.d("WearableReceiver", "Empfangene Count-Daten: $count")
    //                    }
    //
    //                    "/hr" -> {
    //                        val hrJson = dataMap.getString("hr")
    //                        hrJson?.let {
    //                            val list = Json.decodeFromString<List<HeartRateMeasurement>>(it)
    //                            heartRateList.clear() // Vorherige Werte löschen
    //                            heartRateList.addAll(list)
    //                            Log.d("WearableMessage", "Herzfrequenz-Daten empfangen: $list")
    //                        }
    //                    }

    "/skin" -> {
    val skinJson = dataMap.getString("skin")
    skinJson?.let {
    val list =
    Json.decodeFromString<List<SkinTemperatureMeasurement>>(it)
    skinTemperatureList.clear() // Vorherige
    skinTemperatureList.addAll(list)
    Log.d("WearableMessage", "Hauttemperatur-Daten empfangen: $list")
    }
    }
    }
    }
    }
    }
     **/
    override fun onResume() {
        super.onResume()
        Log.d("WearableReceiver", "DataClient wird registriert...")
        //Wearable.getDataClient(this).addListener(this) // Listener aktivieren
    }

    override fun onPause() {
        super.onPause()
        Log.d("WearableReceiver", "DataClient wird deaktiviert...")
        //Wearable.getDataClient(this).removeListener(this) // Listener entfernen, um Speicherlecks zu vermeiden
    }


}

@Composable
fun Greeting(modifier: Modifier = Modifier, count: Int, context: Context) {


    val SAD = "sad"
    val ANGRY = "angry"

    Column(
        modifier = modifier
    ) {
        Text(
            text = "presses",
        )
        Text(
            text = count.toString(),
        )
        Button(
            onClick = {
                Wearable.getNodeClient(context).connectedNodes
                    .addOnSuccessListener { nodes ->
                        for (node in nodes) {
                            Wearable.getMessageClient(context)
                                .sendMessage(node.id, "/emotion", SAD.toByteArray())
                                .addOnSuccessListener {
                                    Log.d("MainActivity", "Emotion gesendet: $SAD")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("MainActivity", "Fehler beim Senden", e)
                                }
                        }
                    }
            }
        ) {
            Text(text = "Sad")
        }
        Button(
            onClick = {
                Wearable.getNodeClient(context).connectedNodes
                    .addOnSuccessListener { nodes ->
                        for (node in nodes) {
                            Wearable.getMessageClient(context)
                                .sendMessage(node.id, "/emotion", ANGRY.toByteArray())
                                .addOnSuccessListener {
                                    Log.d("MainActivity", "Emotion gesendet: $ANGRY")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("MainActivity", "Fehler beim Senden", e)
                                }
                        }
                    }
            }
        ) {
            Text(text = "Angry")
        }
    }
}

@Composable
fun HeartRateView(heartRateList: List<com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement>) {

    Column {
        Text(text = "Heart Rate")
        LazyColumn(
        ) {
            items(heartRateList) { measurement ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "HR: ${measurement.hr}")
                        Text(text = "ID: ${formatTimeOnly(measurement.sessionId)}")
                    }
                }
            }
        }
    }

}

@Composable
fun SkinTemperatureView(
    skinTemperatureList: List<com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement>,
) {
    Column() {
        Text(text = "Skin Temperature")

        LazyColumn {
            items(skinTemperatureList) { measurement ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Temp: ${measurement.objectTemperature}")
                        Text(text = "ID: ${formatTimeOnly(measurement.sessionId)}")
                    }
                }
            }
        }
    }
}

fun formatTimeOnly(sessionId: Long): String {
    val date = Date(sessionId)
    val sdf =
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()) // Format für Stunden, Minuten, Sekunden
    return sdf.format(date)
}

