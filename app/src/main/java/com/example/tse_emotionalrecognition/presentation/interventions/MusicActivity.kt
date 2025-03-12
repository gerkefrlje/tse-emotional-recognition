package com.example.tse_emotionalrecognition.presentation.interventions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text


class MusicActivity : ComponentActivity() {


    private var isInstalled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        val launchIntent = packageManager.getLaunchIntentForPackage("com.spotify.music")
        isInstalled = launchIntent != null

        // Set up your UI. It will automatically update when connectionStatus changes.
        setContent {
            MusicScreen(
                thirdPartyAppStatus = isInstalled,
                launchIntent = launchIntent
            )
        }
    }

    @Composable
    fun MusicScreen(thirdPartyAppStatus: Boolean, launchIntent: android.content.Intent?) {
        when (thirdPartyAppStatus) {
            false -> {
                // Show the retry button with a red background.
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        modifier = Modifier.fillMaxSize(0.5f),
                        onClick = {finish()},
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)

                    ) {
                        Text(
                            text = "Spotify is not installed",
                            color = Color.White,
                            textAlign = TextAlign.Center)

                    }
                }
            }
            true -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Rectangle text box above the buttons
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.DarkGray,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(8.dp)  // internal padding for the text
                        ) {
                            Text(
                                text = "Hey, play your favourite song!",
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                        // Row containing two buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                        ) {
                            // Green Play button
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = { startActivity(launchIntent) },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                            ) {
                                Text(
                                    text = "Play",
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                            // Red Cancel button
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {finish()}, // Activity closes
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}