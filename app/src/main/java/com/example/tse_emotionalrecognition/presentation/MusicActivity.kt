package com.example.tse_emotionalrecognition.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

enum class ConnectionStatus {
    Loading,
    Connected,
    Failed
}

class MusicActivity : ComponentActivity() {
    // Using Compose's mutable state to trigger recomposition when changed.
    private var connectionStatus by mutableStateOf(ConnectionStatus.Loading)
    private val clientId = "629001cccf774723ab98f023b2b40fab"
    private val redirectUri = "tse-emotion-recognition://callback"
    private var spotifyAppRemote: SpotifyAppRemote? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        // Set up your UI. It will automatically update when connectionStatus changes.
        setContent {
            MusicScreen(
                connectionStatus = connectionStatus,
                onPlayClicked = { playMusic() },
                onRetryClicked = { connectToSpotify() }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        connectToSpotify()
    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }

    // Attempt to connect to Spotify
    private fun connectToSpotify() {
        // Update state to loading while attempting connection.
        connectionStatus = ConnectionStatus.Loading
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                connectionStatus = ConnectionStatus.Connected
                // Optionally, perform any additional actions after connection
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("MusicActivity", throwable.message, throwable)
                connectionStatus = ConnectionStatus.Failed
            }
        })
    }

    // Call this when the play button is clicked
    private fun playMusic() {
        spotifyAppRemote?.let {
            val playlistURI = "spotify:playlist:37i9dQZF1DX2sUQwD7tbmL"
            it.playerApi.play(playlistURI)
        }
    }

    @Composable
    fun MusicScreen(
        connectionStatus: ConnectionStatus,
        onPlayClicked: () -> Unit,
        onRetryClicked: () -> Unit
    ) {
        when (connectionStatus) {
            ConnectionStatus.Loading -> {
                // Show a loading indicator, e.g., a CircularProgressIndicator.
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            ConnectionStatus.Connected -> {
                // Show the play button.
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = onPlayClicked) {
                        Text(text = "Play Music")
                    }
                }
            }
            ConnectionStatus.Failed -> {
                // Show the retry button with a red background.
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        modifier = Modifier.fillMaxSize(),
                        onClick = onRetryClicked,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                    ) {
                        Text(text = "Connection Failed - Retry", color = Color.White)
                    }
                }
            }
        }
    }

}