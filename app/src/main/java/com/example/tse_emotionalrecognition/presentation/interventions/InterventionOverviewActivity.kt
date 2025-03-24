package com.example.tse_emotionalrecognition.presentation.interventions

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
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

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun ChoseIntervention() {
    val context = LocalContext.current
    TSEEmotionalRecognitionTheme {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(16.dp)
        ) {
            item {
                Button(//Breathin Intervention
                    onClick = {

                        val intent = Intent(context, BreathingActivity::class.java)
                        context.startActivity(intent)

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Breathing Intervention")
                }
            }
            /** altes Design
            item {
                Spacer(modifier = Modifier.height(16.dp))
            } //Spacer zwischen den Buttons
            item {
                Button(//Call Intervention
                    onClick = {
                        val intent = Intent(context, CallInterventionActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Call Intervention")
                }
            }**/
            item {
                Spacer(modifier = Modifier.height(16.dp))
            } //Spacer zwischen den Buttons
            item {
                Button(//Music Intervention
                    onClick = {
                        val intent = Intent(context, MusicActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Music Intervention")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            } //Spacer zwischen den Buttons
            item {
                Button(//Contact Intervention
                    onClick = {
                        val intent = Intent(context, ContactActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Contact Intervention")
                }
            }
        }

        /** alte UI
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
        **/
    }

}