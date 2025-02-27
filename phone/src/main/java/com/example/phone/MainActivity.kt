package com.example.phone

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phone.data.HeartRateMeasurement
import com.example.phone.data.SkinTemperatureMeasurement
import com.example.phone.ui.theme.TSEEmotionalRecognitionTheme
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    var wearCount by mutableIntStateOf(0) // State in der Activity deklarieren
    private val heartRateList = mutableStateListOf<HeartRateMeasurement>()
    private val skinTemperatureList = mutableStateListOf<SkinTemperatureMeasurement>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Wearable.getDataClient(this).addListener(this)


        enableEdgeToEdge()
        setContent {
            TSEEmotionalRecognitionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val standardModifier = Modifier.padding(innerPadding)

                    Row {


                        Greeting(modifier = standardModifier, count = wearCount, this@MainActivity)
                        HeartRateView(
                            modifier = standardModifier.verticalScroll(rememberScrollState()),
                            heartRateList = heartRateList
                        )
                        SkinTemperatureView(
                            modifier = standardModifier.verticalScroll(rememberScrollState()),
                            skinTemperatureList = skinTemperatureList
                        )


                    }
                }
            }

        }
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

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("WearableReceiver", "DataEventBuffer received!")
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                when (dataItem.uri.path) {
                    "/button" -> {
                        val count = dataMap.getInt("count")
                        wearCount = count
                        Log.d("WearableReceiver", "Empfangene Count-Daten: $count")
                    }

                    "/hr" -> {
                        val hrJson = dataMap.getString("hr")
                        hrJson?.let {
                            val list = Json.decodeFromString<List<HeartRateMeasurement>>(it)
                            heartRateList.clear() // Vorherige Werte löschen
                            heartRateList.addAll(list)
                            Log.d("WearableMessage", "Herzfrequenz-Daten empfangen: $list")
                        }
                    }

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

    override fun onResume() {
        super.onResume()
        Log.d("WearableReceiver", "DataClient wird registriert...")
        Wearable.getDataClient(this).addListener(this) // Listener aktivieren
    }

    override fun onPause() {
        super.onPause()
        Log.d("WearableReceiver", "DataClient wird deaktiviert...")
        Wearable.getDataClient(this)
            .removeListener(this) // Listener entfernen, um Speicherlecks zu vermeiden
    }


}

@Composable
fun Greeting(modifier: Modifier = Modifier, count: Int, context: Context) {


    val SAD = "sad"
    val ANGRY = "angry"
    val dataClient = Wearable.getDataClient(context)

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
fun HeartRateView(heartRateList: List<HeartRateMeasurement>, modifier: Modifier) {

    Column(
        modifier = modifier
    ) {
        Text(
            text = "Heart Rate",
        )
        heartRateList.forEach { measurement ->
            Row(
            ) {
                Text(text = "Heart Rate: ${measurement.hr}")
            }
        }
    }
}

@Composable
fun SkinTemperatureView(
    skinTemperatureList: List<SkinTemperatureMeasurement>,
    modifier: Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "Skin Temperature",
        )
        skinTemperatureList.forEach { measurement ->
            Row()
            {
                Text(text = "Skin Temperature: ${measurement.objectTemperature}")
            }
        }
    }
}
