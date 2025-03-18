package com.example.tse_emotionalrecognition.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import com.example.common.InterventionTriggerHelper
import com.example.tse_emotionalrecognition.presentation.theme.TSEEmotionalRecognitionTheme

class InterventionTriggerActivity : ComponentActivity() {

    private lateinit var interventionTriggerHelper: InterventionTriggerHelper // Initialisierung verschieben

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        interventionTriggerHelper = InterventionTriggerHelper(this)
        super.onCreate(savedInstanceState)
        setContent {
            TSEEmotionalRecognitionTheme {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    InterventionButton(
                        InterventionTriggerHelper.BREATH,
                        interventionTriggerHelper,
                        "Breath"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InterventionButton(
                        InterventionTriggerHelper.CONTACT,
                        interventionTriggerHelper,
                        "Contact"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InterventionButton(
                        InterventionTriggerHelper.MUSIC,
                        interventionTriggerHelper,
                        "Music"
                    )
                }
            }
        }
    }
}
@Composable
fun InterventionButton(
    intervention: Int,
    interventionTriggerHelper: InterventionTriggerHelper,
    text: String
) {
    Button(
        onClick = {
            interventionTriggerHelper.testTrigger(intervention)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text)
    }
}