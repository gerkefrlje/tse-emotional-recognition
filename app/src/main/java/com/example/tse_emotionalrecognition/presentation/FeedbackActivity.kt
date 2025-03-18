package com.example.tse_emotionalrecognition.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.UserRepository
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectColumns
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FeedbackActivity: ComponentActivity() {
    private val userRepository by lazy { UserDataStore.getUserRepository(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prediction = intent.getSerializableExtra("prediction") as? AffectType

        if (prediction == null) {
            Log.e("FeedbackActivity", "No prediction provided")
            finish()
            return
        }

        setContent {
            val affectDataId = intent.getLongExtra("affectDataId", -1)

            insertEngagementTime(affectDataId)

            FeedbackScreen(
                affectDataId = affectDataId,
                prediction = prediction,
            ) {
                startActivity(
                    Intent(this, LabelActivity::class.java).apply {
                        putExtra("affectDataId", affectDataId)
                    }
                )
            }
        }
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
                finished()
            }
        }

    }

    @Composable
    fun FeedbackScreen(
        affectDataId: Long = -1,
        prediction: AffectType,
        onIncorrect: () -> Unit
    ) {
        val context = LocalContext.current
        val predictionString = when (prediction) {
            AffectType.NEGATIVE -> "Angry or Sad"
            AffectType.POSITIVE -> "Happy or Relaxed"
            else -> "Neutral"

        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Are you currently feeling $predictionString?",
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
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            updateAffect(affectDataId, AffectColumns.AFFECT, prediction)
                        }
                    ) {
                        Text(
                            text = "Yes",
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onIncorrect()
                            (context as? FeedbackActivity)?.finish()
                        }
                    ) {
                        Text(
                            text = "No",
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }


    @Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
    @Composable
    fun PreviewFeedbackScreen() {
        FeedbackScreen(
            affectDataId = 1L,
            prediction = AffectType.POSITIVE,
            onIncorrect = {}
        )
    }
}