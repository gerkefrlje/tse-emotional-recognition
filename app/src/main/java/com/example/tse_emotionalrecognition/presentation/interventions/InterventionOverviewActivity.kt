package com.example.tse_emotionalrecognition.presentation.interventions

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.tse_emotionalrecognition.presentation.theme.TSEEmotionalRecognitionTheme

class InterventionOverviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            ChoseIntervention()
        }
    }
}

@Composable
fun ChoseIntervention() {
    val context = LocalContext.current
    TSEEmotionalRecognitionTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Button(//Breathin Intervention
                    onClick = {

                        val intent = Intent(context, BreathingActivity::class.java)
                        context.startActivity(intent)

                    },
                    modifier = Modifier.fillMaxWidth(1f)
                ) {
                    Text(
                        text = "Breathing Intervention",
                        modifier = Modifier
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }

                Button(//Call Intervention
                    onClick = {
                        val intent = Intent(context, CallInterventionActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(1f)
                ) {
                    Text(
                        text = "Call Intervention",
                        modifier = Modifier
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

}