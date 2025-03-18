package com.example.tse_emotionalrecognition.presentation.interventions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.tse_emotionalrecognition.presentation.theme.TSEEmotionalRecognitionTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BreathingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            TSEEmotionalRecognitionTheme {
                // State variable to track which screen to show:
                // false = initial screen, true = breathing screen
                var showBreathingScreen by remember { mutableStateOf(false) }

                if (showBreathingScreen) {
                    BreathingScreen()
                } else {
                    InitialScreen(
                        onStartClicked = { showBreathingScreen = true },
                        onCancelClicked = { finish() } // Calls finish() to close the activity.
                    )
                }
            }
        }
    }

    @Composable
    fun InitialScreen(
        onStartClicked: () -> Unit,
        onCancelClicked: () -> Unit
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start Button
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onStartClicked,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                ) {
                    Text(
                        text = "Start",
                        textAlign = TextAlign.Center
                    )
                }
                // Cancel Button
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onCancelClicked,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text(
                        text = "Cancel",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    @Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
    @Composable
    fun BreathingScreen() {

        val scope = rememberCoroutineScope()
        // Animatable value that controls the size factor; initial value is 0.5 (small size)
        val sizeAnim = remember { androidx.compose.animation.core.Animatable(0.4f)}

        var buttonText by remember { mutableStateOf("Breath in") }

        fun animate() {
            scope.launch {
                // Animate to a smaller size (e.g., 50% of full size) over 1 second
                for(i in 0..1) {
                    buttonText = "Breath in"
                    sizeAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 2000)
                    )
                    buttonText = "Breath out"
                    delay(1000)
                    // Animate back to full size over 1 second
                    sizeAnim.animateTo(
                        targetValue = 0.5f,
                        animationSpec = tween(durationMillis = 4000)
                    )
                    delay(1000)
                }
                buttonText = "Done"

                delay(2000)
                // Return to the previous intent by finishing the current activity
                finish()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Box {
                Button(
                    onClick = {
                        // Launch a coroutine to run the animations sequentially
                        animate()
                    },
                    // Use the animated size factor with fillMaxSize modifier
                    modifier = Modifier.fillMaxSize(sizeAnim.value)
                ) {
                    Text(text = buttonText)
                }
            }
        }

    }
}