package com.example.tse_emotionalrecognition.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
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
            BreathingScreen()
        }
    }

    @Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
    @Composable
    fun BreathingScreen() {
        TSEEmotionalRecognitionTheme {
            val scope = rememberCoroutineScope()
            // Animatable value that controls the size factor; initial value is 1 (full size)
            val sizeAnim = remember { androidx.compose.animation.core.Animatable(1f)}

            var buttonText by remember { mutableStateOf("Touch") }

            fun animate() {
                scope.launch {
                    buttonText = "Breath"

                    // Animate to a smaller size (e.g., 50% of full size) over 1 second
                    for(i in 0..1) {
                        sizeAnim.animateTo(
                            targetValue = 0.5f,
                            animationSpec = tween(durationMillis = 2000)
                        )
                        // Animate back to full size over 1 second
                        sizeAnim.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 4000)
                        )
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
}