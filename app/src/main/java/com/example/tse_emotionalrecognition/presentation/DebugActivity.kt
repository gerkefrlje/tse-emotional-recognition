package com.example.tse_emotionalrecognition.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.MaterialTheme
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectType
import com.example.tse_emotionalrecognition.presentation.MainActivity.Companion.getDetailedAppPhase
import com.example.tse_emotionalrecognition.presentation.theme.TSEEmotionalRecognitionTheme
import com.example.tse_emotionalrecognition.presentation.utils.DataCollectReciever
import com.example.tse_emotionalrecognition.presentation.utils.EmojiState
import com.example.tse_emotionalrecognition.presentation.utils.updateEmoji
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DebugActivity : ComponentActivity() {

    private val userRepository by lazy { UserDataStore.getUserRepository(application) }

    fun formatMillisToHHMMSS(millis: Long): String {
        if (millis < 0) {
            return "00:00:00" // Or handle negative values as needed
        }

        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis % TimeUnit.HOURS.toMillis(1))
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis % TimeUnit.MINUTES.toMillis(1))

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = applicationContext.getSharedPreferences("emoji_prefs", Context.MODE_PRIVATE)

        val sharedPreferences =
            applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val firstLaunchTime = sharedPreferences.getLong("first_launch_time", 0L)

        val detail = getDetailedAppPhase(firstLaunchTime)
        val appPhase = detail.appPhase
        val timeUntilNextPhaseMillis = detail.timeUntilNextPhaseMillis
        val timeUntilNextPhase = formatMillisToHHMMSS(timeUntilNextPhaseMillis)

        Log.d("DebugActivity", "next App Phase in ${timeUntilNextPhase}")

        setContent {
            TSEEmotionalRecognitionTheme {

                val context = LocalContext.current


                var currentEmojiState by remember {
                    mutableStateOf(
                        EmojiState.valueOf(
                            prefs.getString(
                                "emoji_state",
                                EmojiState.NEUTRAL.name
                            )!!
                        )
                    )
                }



                ScalingLazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                        .padding(16.dp)
                ) {
                    item {
                        Text("Debug Menu")
                    }
                    item {
                        Column {
                            Text(
                                "Current App Phase: ${MainActivity.getAppPhase(firstLaunchTime)}",
                                maxLines = 2
                            )
                            Text(
                                "Next App Phase in: $timeUntilNextPhase",
                                maxLines = 2
                            )
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                userRepository.insertAffect(
                                    CoroutineScope(Dispatchers.IO),
                                    AffectData(sessionId = 1, affect = AffectType.NULL)
                                ) {
                                    val affectDataID = it.id
                                    val intent =
                                        Intent(context, LabelActivity::class.java)
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
                                val sessionId = Calendar.getInstance().timeInMillis

                                val intent =
                                    Intent(applicationContext, DataCollectReciever::class.java)
                                intent.putExtra("COLLECT_DATA", true)
                                intent.putExtra("PHASE", AppPhase.INITIAL_COLLECTION)
                                intent.putExtra("sessionId", sessionId)

                                applicationContext.sendBroadcast(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start Sensor")
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                val sessionId = Calendar.getInstance().timeInMillis

                                val intent =
                                    Intent(applicationContext, DataCollectReciever::class.java)
                                intent.putExtra("COLLECT_DATA", true)
                                intent.putExtra("PHASE", AppPhase.PREDICTION_WITH_FEEDBACK)
                                intent.putExtra("sessionId", sessionId)

                                applicationContext.sendBroadcast(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Model Training")
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                val intent =
                                    Intent(applicationContext, DataCollectReciever::class.java)
                                intent.putExtra("COLLECT_DATA", true)
                                intent.putExtra("PHASE", AppPhase.PREDICTION_ONLY)
                                intent.putExtra("sessionId", 1L)

                                applicationContext.sendBroadcast(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Model Prediction")
                        }
                    }
//                        item {
//                            Spacer(modifier = Modifier.height(16.dp))
//                        }
                    item {
                        Button(
                            onClick = {
                                val newEmojiState = EmojiState.entries.random()
                                updateEmoji(applicationContext, newEmojiState)

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
    }
}