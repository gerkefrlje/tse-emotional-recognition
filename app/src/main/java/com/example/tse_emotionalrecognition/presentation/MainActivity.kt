package com.example.tse_emotionalrecognition.presentation

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.*
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.presentation.interventions.InterventionOverviewActivity
import com.example.tse_emotionalrecognition.presentation.theme.TSEEmotionalRecognitionTheme
import com.example.tse_emotionalrecognition.presentation.utils.EmojiSelector
import com.example.tse_emotionalrecognition.presentation.utils.EmojiState
import com.example.tse_emotionalrecognition.presentation.utils.updateEmoji
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    private val userRepository by lazy { UserDataStore.getUserRepository(application) }

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
}

@Composable
fun SelectIntervention(userRepository: com.example.tse_emotionalrecognition.common.data.database.UserRepository) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("emoji_prefs", Context.MODE_PRIVATE)
    var currentEmojiState by remember {
        mutableStateOf(EmojiState.valueOf(prefs.getString("emoji_state", EmojiState.NEUTRAL.name)!!))
    }

    TSEEmotionalRecognitionTheme {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(16.dp)
        ) {
            item {
                EmojiSelector(currentEmojiState)
            }
            item {
                Button(
                    onClick = {
                        val intent = Intent(context, InterventionOverviewActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Interventions")
                }
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
                Button(
                    onClick = {
                        val newEmojiState = EmojiState.entries.random()
                        updateEmoji(context, newEmojiState)

                        // Force UI Update
                        currentEmojiState = newEmojiState
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Randomize Emoji")
                }
            }
        }
    }
}