package com.example.tse_emotionalrecognition.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tse_emotionalrecognition.data.database.UserDataStore
import com.example.tse_emotionalrecognition.data.database.UserRepository
import com.example.tse_emotionalrecognition.data.database.entities.AffectColumns
import com.example.tse_emotionalrecognition.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.data.database.entities.AffectType
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
            AffectType.ANGRY_SAD -> "Angry or Sad"
            AffectType.HAPPY_RELAXED -> "Happy or Relaxed"
            else -> "Neutral"
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "We have detected that your feelings are $predictionString. Is this correct?")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                updateAffect(affectDataId, AffectColumns.AFFECT, prediction)
            }) {
                Text(text = "Yes")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                // Launch the LabelActivity
                onIncorrect()
                (context as? FeedbackActivity)?.finish()
            }) {
                Text(text = "No")
            }
        }
    }
}