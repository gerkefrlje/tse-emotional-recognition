package com.example.tse_emotionalrecognition.presentation
import android.content.Context

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import coil.ImageLoader
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectType
import com.example.tse_emotionalrecognition.common.data.database.UserRepository
import com.example.tse_emotionalrecognition.presentation.interventions.InterventionOverviewActivity
import com.example.tse_emotionalrecognition.presentation.theme.TSEEmotionalRecognitionTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.request.repeatCount
import com.example.tse_emotionalrecognition.complication.MainComplicationService

enum class EmojiState {
    NEUTRAL, HAPPY, UNHAPPY
}

fun getEmojiResForState(state: EmojiState): Int {
    return when(state) {
        EmojiState.NEUTRAL -> R.drawable.neutral_emoji_animated
        EmojiState.HAPPY -> R.drawable.happy_emoji_animated
        EmojiState.UNHAPPY -> R.drawable.unhappy_emoji_animated
    }
}

@Composable
fun LoopingGifImage(
    @DrawableRes gifRes: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            add(GifDecoder.Factory())
        }
        .build()

    // Create ImageRequest with repeat count set to 0 (play once)
    val imageRequest = ImageRequest.Builder(context)
        .data(gifRes)
        .repeatCount(1) // 0 = play once, 1 = repeat once, etc.
        .build()

    // Use AsyncImage with the custom loader to display the animated GIF.
    AsyncImage(
        model = imageRequest,
        contentDescription = "Animated GIF",
        imageLoader = gifEnabledLoader,
        modifier = modifier.fillMaxWidth()
    )
}

class MainActivity : ComponentActivity() {
    private val userRepository by lazy { com.example.tse_emotionalrecognition.common.data.database.UserDataStore.getUserRepository(application) }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestedPermissions = arrayOf(
        android.Manifest.permission.BODY_SENSORS,
        android.Manifest.permission.FOREGROUND_SERVICE,
        android.Manifest.permission.POST_NOTIFICATIONS,
        android.Manifest.permission.ACTIVITY_RECOGNITION,
        android.Manifest.permission.HIGH_SAMPLING_RATE_SENSORS,
        android.Manifest.permission.READ_CONTACTS
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        requestPermissions(requestedPermissions, 0)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            SelectIntervention(userRepository)
        }
    }

    private fun triggerLable() {
        userRepository.insertAffect(
            CoroutineScope(Dispatchers.IO)
            ,
            com.example.tse_emotionalrecognition.common.data.database.entities.AffectData(
                sessionId = 1,
                affect = com.example.tse_emotionalrecognition.common.data.database.entities.AffectType.HAPPY_RELAXED
            )
        ){
            var affectDataID = it.id
            val intent = Intent(this, LabelActivity::class.java)
            intent.putExtra("affectDataId", affectDataID)
            startActivity(intent)
        }
    }
}

@Composable
fun WearApp(greetingName: String) {
    TSEEmotionalRecognitionTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}

@Composable
fun SelectIntervention(userRepository: com.example.tse_emotionalrecognition.common.data.database.UserRepository) {
    val context = LocalContext.current

    val prefs = context.getSharedPreferences("emoji_prefs", Context.MODE_PRIVATE)
    val savedState = prefs.getString("emoji_state", EmojiState.NEUTRAL.name) ?: EmojiState.NEUTRAL.name
    var currentEmojiState by remember { mutableStateOf(EmojiState.valueOf(savedState)) }

    var showDialog by remember { mutableStateOf(false) }

    fun updateEmoji(state: EmojiState) {
        currentEmojiState = state
        // Update shared preferences with the new emoji state
        val prefs = context.getSharedPreferences("emoji_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("emoji_state", state.name).apply()

        // Request complication update to refresh the complication appearance
        val requester = androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester.create(
            context,
            android.content.ComponentName(context, MainComplicationService::class.java)
        )
        requester.requestUpdateAll()
    }

    TSEEmotionalRecognitionTheme {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(16.dp)
        ) {
            item {
                LoopingGifImage(
                    gifRes = getEmojiResForState(currentEmojiState),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDialog = true }
                )
                // Display an AlertDialog when showDialog is true
                if (showDialog) {
                    val dialogText = when (currentEmojiState) {
                        EmojiState.NEUTRAL -> "This is a neutral emoji."
                        EmojiState.HAPPY -> "This is a happy emoji."
                        EmojiState.UNHAPPY -> "This is an unhappy emoji."
                    }
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(text = "Emoji Info") },
                        text = { Text(text = dialogText) },
                        confirmButton = {
                            Button(onClick = { showDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }

            // Display animated emoji based on current state, and show dialog on click
            }

            item {
                Button(
                    onClick = {
                        val intent = Intent(context, InterventionOverviewActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Interventions")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Button(
                    onClick = {
                        userRepository.insertAffect(
                            CoroutineScope(Dispatchers.IO)
                            ,
                            com.example.tse_emotionalrecognition.common.data.database.entities.AffectData(
                                sessionId = 1,
                                affect = com.example.tse_emotionalrecognition.common.data.database.entities.AffectType.HAPPY_RELAXED
                            )
                        ){
                            var affectDataID = it.id
                            val intent = Intent(context, LabelActivity::class.java)
                            intent.putExtra("affectDataId", affectDataID)
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Label Activity")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Button(
                    onClick = {
                        val intent = Intent(context, SendDataActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Transfer")
                }
            }
            item {
                Button(
                    onClick = { updateEmoji(EmojiState.entries.toTypedArray().random()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Randomize Emoji")
                }
            }
        }
    }
}