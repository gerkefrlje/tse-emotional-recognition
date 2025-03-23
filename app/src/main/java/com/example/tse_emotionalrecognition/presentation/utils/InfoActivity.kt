package com.example.tse_emotionalrecognition.presentation.utils

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.example.tse_emotionalrecognition.presentation.AppPhase
import com.example.tse_emotionalrecognition.presentation.MainActivity

class InfoActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPreferences =
            applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val firstLaunchTime = sharedPreferences.getLong("first_launch_time", 0L)
        val currentAppPackage = MainActivity.getAppPhase(firstLaunchTime)

        super.onCreate(savedInstanceState)

        setContent {
            InfoScreen(currentAppPackage)
        }
    }
}

@Composable
fun InfoScreen(appPhase: AppPhase) {
    val infoText = when (appPhase) {
        AppPhase.INITIAL_COLLECTION -> {
            "In this phase, you start training the model. Here you are asked to tell how you feel."
        }

        AppPhase.PREDICTION_WITH_FEEDBACK -> {
            "In this phase, the model is already trained and gives you a prediction. If it is wrong, you have to say how you feel in the moment."
        }

        AppPhase.PREDICTION_ONLY -> {
            "In this phase, the model is fully trained. Now it should be able to determine your emotion."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp) // Increased padding
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
    ) {
        Text(
            text = "Information", style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )
        Text(
            text = "You are currently in the ${appPhase.name} phase.",
            style = TextStyle(
                fontSize = 14.sp
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(text = infoText, modifier = Modifier.padding(top = 8.dp))
    }
}