package com.example.phone

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.phone.data.HeartRateMeasurement
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

class MainActivity : ComponentActivity(),DataClient.OnDataChangedListener {

    var wearCount by mutableIntStateOf(0) // State in der Activity deklarieren


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Wearable.getDataClient(this).addListener(this)


        enableEdgeToEdge()
        setContent {
            TSEEmotionalRecognitionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding),
                            count = wearCount
                        )
                        Button(onClick = {
                            wearCount++
                            val dataClient = Wearable.getDataClient(this@MainActivity)
                            val putDataRequest = PutDataMapRequest.create("/count").apply {
                                dataMap.putInt("count", wearCount)
                            }.asPutDataRequest()
                            dataClient.putDataItem(putDataRequest)
                        }
                        )
                        {
                            Text(text = "Increment")
                        }
                    }

                }
            }
        }

        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                Log.d("WearableReceiver", "Verbundene Geräte: ${nodes.map { it.displayName }}")
            }
            .addOnFailureListener { Log.e("WearableReceiver", "Fehler beim Abrufen der Nodes", it) }

    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("WearableReceiver", "DataEventBuffer received!")
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == "/button") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val count = dataMap.getInt("count")
                    wearCount = count
                    // Hier kannst du die empfangenen Daten verwenden
                    // Zum Beispiel, um sie in einer TextView anzuzeigen:
                    Log.d("WearableReceiver", "Empfangene Daten: $count")
                }

//                when (dataItem.uri.path) {
//                    "/button" -> {
//                        val count = dataMap.getInt("count")
//                        Log.d("WearableReceiver", "Empfangene Count-Daten: $count")
//                    }
//
//                    "/hr" -> {
//                        val hrJson = dataMap.getString("hr")
//                        val heartRate =
//                            Json.decodeFromString(HeartRateMeasurement.serializer(), hrJson!!)
//                        Log.d("WearableReceiver", "Empfangene Herzfrequenz: $heartRate")
//                    }
//                }
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
fun Greeting(name: String, modifier: Modifier = Modifier, count: Int) {
    Text(
        text = "$count $name!",
        modifier = modifier,

        )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TSEEmotionalRecognitionTheme {
        Greeting("Android", count = 0)
    }
}