package com.example.tse_emotionalrecognition.presentation.interventions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices

class CallInterventionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            CallIntervention()
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun CallIntervention() {
    ContactList()
}

@Composable
fun ContactList(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Kontakte", color = Color.White, fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterHorizontally))

        Text(text = "möchtest du jemand anrufen?", fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(all = 2.dp), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        ContactItem(name = "Mama", onClick = {
            //makeCall(context, "1234567890") // Ersetze mit echter Nummer
        })
        ContactItem(name = "Papa", onClick = {
            //makeCall(context, "0987654321") // Ersetze mit echter Nummer
        })
        ContactItem(name = "Prof. Alexander Mädche", onClick = {
            //makeCall(context, "1122334455") // Ersetze mit echter Nummer
        })
        Spacer(modifier = Modifier.weight(1f))
//        IconButton(onClick = {
//            Toast.makeText(context, "Neuen Kontakt hinzufügen", Toast.LENGTH_SHORT).show()
//        }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
//            Icon(painterResource(id = android.R.drawable.ic_input_add), contentDescription = "Add Contact")
//        }
    }
}

@Composable
fun ContactItem(name: String, onClick: () -> Unit) {
    Card (
//        onClick = { onClick() },

//        contentColor = Color.Cyan,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 2.dp)
        colors = CardDefaults.cardColors(containerColor = Color.Cyan),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick() }

    ){
        Row(
            horizontalArrangement = Arrangement.Center
        ) {
//        Icon(painterResource(id = android.R.drawable.ic_menu_call), contentDescription = "Call", tint = Color.White)
//        Spacer(modifier = Modifier.width(8.dp))
            Text(text = name, color = Color.Black, textAlign = TextAlign.Center)
        }
    }

}