package com.example.tse_emotionalrecognition.presentation

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.navigation.compose.*
import androidx.wear.compose.material.Button
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectColumns
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectType
import com.example.tse_emotionalrecognition.presentation.utils.EmojiState
import com.example.tse_emotionalrecognition.presentation.utils.FullText
import com.example.tse_emotionalrecognition.presentation.utils.InterventionTriggerHelper
import com.example.tse_emotionalrecognition.presentation.utils.RowButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.tse_emotionalrecognition.presentation.utils.updateEmoji


class LabelActivity : ComponentActivity() {
    private val userRepository by lazy {
        com.example.tse_emotionalrecognition.common.data.database.UserDataStore.getUserRepository(
            application
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val affectDataId = intent.getLongExtra("affectDataId", -1)

            Log.d("LabelActivity", "affectDataId from intent: $affectDataId") // Logging hinzufügen

            insertEngagementTime(affectDataId)
            LabelWatch(
                affectDataId,
            )
        }
    }

    private fun insertAffect() {
    }

    private fun insertEngagementTime(id: Long) {
        userRepository.updateAffectColumn(
            CoroutineScope(Dispatchers.IO),
            id, AffectColumns.TIME_OF_ENGAGEMENT, System.currentTimeMillis()
        )
    }

    private fun updateAffect(
        id: Long,
        column: AffectColumns,
        value: Any,
        finished: (() -> Unit)? = null
    ) {
        userRepository.updateAffectColumn(
            CoroutineScope(Dispatchers.IO),
            id, column, value
        ) {
            if (finished != null) {
                if (it.affect == AffectType.NEGATIVE) {
                    updateEmoji(
                        applicationContext,
                        com.example.tse_emotionalrecognition.presentation.utils.EmojiState.UNHAPPY_ALERT
                    )
                } else if (it.affect == AffectType.POSITIVE) {
                    updateEmoji(
                        applicationContext,
                        com.example.tse_emotionalrecognition.presentation.utils.EmojiState.HAPPY
                    )
                } else {
                    updateEmoji(
                        applicationContext,
                        com.example.tse_emotionalrecognition.presentation.utils.EmojiState.NEUTRAL
                    )
                }
                finished()
            }
        }

    }

    @Composable
    private fun LabelWatch(
        affectId: Long
    ) {
        val context = LocalContext.current
        var showThankYou by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (showThankYou) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Thank you!",
                        fontSize = 20.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "✖",
                        fontSize = 24.sp,
                        color = Color.White,
                        modifier = Modifier.clickable { finish() },
                        textAlign = TextAlign.Center
                    )
                }

                finish()

            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 0.dp)
                ) {
                    Text(
                        text = "How are you feeling\nright now?",
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        listOf(
                            "😊" to AffectType.POSITIVE,
                            "😐" to AffectType.NONE,
                            "😞" to AffectType.NEGATIVE
                        ).forEach { (emoji, affectType) ->
                            Text(
                                text = emoji,
                                fontSize = 32.sp,
                                modifier = Modifier.clickable {
                                    updateAffect(affectId, AffectColumns.AFFECT, affectType) {
                                        showThankYou = true
                                        updateEmojiUi(context, affectType)
                                    }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        delay(1000)
                                        startIntervention(context)
                                    }
                                    finish()
                                },
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateEmojiUi(context: Context, affectType: AffectType) {
        when (affectType) {
            AffectType.POSITIVE -> {
                updateEmoji(
                    context,
                    EmojiState.HAPPY
                )
            }
            AffectType.NEGATIVE -> {
                updateEmoji(
                    context,
                    EmojiState.UNHAPPY_ALERT
                )
            }
            AffectType.NONE -> {
                updateEmoji(
                    context,
                    EmojiState.NEUTRAL
                )
            }
            AffectType.NULL -> {}
        }
    }

    private fun startIntervention(context: Context) {
        val triggerHelper = InterventionTriggerHelper(context)
        triggerHelper.showRandomIntervention()
    }

    @Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
    @Composable
    fun PreviewLabelWatch() {
        LabelWatch(
            affectId = 1L
        )
    }


}

