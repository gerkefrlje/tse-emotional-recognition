package com.example.tse_emotionalrecognition.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.navigation.compose.*
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.tse_emotionalrecognition.data.database.UserDataStore
import com.example.tse_emotionalrecognition.data.database.entities.AffectColumns
import com.example.tse_emotionalrecognition.data.database.entities.AffectType
import com.example.tse_emotionalrecognition.presentation.utils.FullText
import com.example.tse_emotionalrecognition.presentation.utils.RowButton


class LabelActivity : ComponentActivity() {
    private val userRepository by lazy { UserDataStore.getUserRepository(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val affectDataId = intent.getLongExtra("affectDataId", -1)

            Log.d("LabelActivity", "affectDataId from intent: $affectDataId") // Logging hinzufügen

            insertEngagementTime(affectDataId)
            LabelWatch(
                affectDataId,
                startDestination = "Select",
                navController = rememberSwipeDismissableNavController(),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun insertAffect() {
        userRepository
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
    private fun LabelWatch(
        affectId: Long,
        navController: NavHostController,
        startDestination: String = "AngrySadSelect",
        modifier: Modifier = Modifier
    ) {
        var loops: Int = 0
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("Select") {
                val scrollState = rememberScrollState()
                Column(
                    modifier = modifier.verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "What fits better for you?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    RowButton(
                        text = "Happy or Relaxed",
                        onClick = {
                            updateAffect(affectId, AffectColumns.AFFECT, AffectType.HAPPY_RELAXED)
                            navController.navigate("HappyRelaxedIntervention") {
                                popUpTo(0)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    RowButton(
                        text = "Angry or Sad",
                        onClick = {
                            updateAffect(affectId, AffectColumns.AFFECT, AffectType.ANGRY_SAD)
                            navController.navigate("AngrySadIntervention") {
                                popUpTo(0)
                            }
                        }
                    )
                }
            }
            composable("AngrySadIntervention") {
                FullText(
                    text = "Life is to short to worry about stupid things !",
                    finished = {
                        updateAffect(
                            affectId,
                            AffectColumns.TIME_OF_FINISHED,
                            System.currentTimeMillis()
                        )
                        finish()
                    }
                )
            }
            composable("HappyRelaxedIntervention") {
                FullText(
                    text = "Keep going, nice progress !",
                    finished = {
                        updateAffect(
                            affectId,
                            AffectColumns.TIME_OF_FINISHED,
                            System.currentTimeMillis()
                        )
                        finish()
                    }
                )
            }

        }
    }
}

