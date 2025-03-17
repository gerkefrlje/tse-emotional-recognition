package com.example.tse_emotionalrecognition.presentation


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.content.ContextCompat
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectType
import com.example.tse_emotionalrecognition.common.data.database.entities.InterventionStats
import com.example.tse_emotionalrecognition.common.data.database.entities.TAG
import com.example.tse_emotionalrecognition.common.data.database.utils.CommunicationDataSender
import com.example.tse_emotionalrecognition.presentation.MainActivity.Companion.getAppPhase
import com.example.tse_emotionalrecognition.presentation.interventions.InterventionOverviewActivity
import com.example.tse_emotionalrecognition.presentation.theme.TSEEmotionalRecognitionTheme
import com.example.tse_emotionalrecognition.presentation.utils.EmojiSelector
import com.example.tse_emotionalrecognition.presentation.utils.EmojiState
import com.example.tse_emotionalrecognition.presentation.utils.scheduleDailyEmojiUpdateWorkManager
import com.example.tse_emotionalrecognition.presentation.utils.updateEmoji
import com.example.tse_emotionalrecognition.presentation.utils.DataCollectReciever
import com.example.tse_emotionalrecognition.presentation.utils.DataCollectService
import com.example.tse_emotionalrecognition.presentation.utils.DataCollectWorker
import com.example.tse_emotionalrecognition.presentation.utils.InfoActivity
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar


class MainActivity : ComponentActivity() {

    companion object {
        fun getAppPhase(firstLaunchTime: Long): AppPhase {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - firstLaunchTime
            val daysElapsed = TimeUnit.MILLISECONDS.toDays(elapsedTime)

            return when {
                daysElapsed < 1 -> AppPhase.INITIAL_COLLECTION
                daysElapsed < 2 -> AppPhase.PREDICTION_WITH_FEEDBACK
                else -> AppPhase.PREDICTION_ONLY
            }
        }

        fun getDetailedAppPhase(firstLaunchTimeMillis: Long,
                                initialCollectionDurationDays: Long = 1,
                                predictionWithFeedbackDurationDays: Long = 1): AppPhaseResult {
            val currentTimeMillis = System.currentTimeMillis()

            // Error handling for invalid first launch time
            if (firstLaunchTimeMillis == 0L) {
                return AppPhaseResult(AppPhase.INITIAL_COLLECTION, Long.MAX_VALUE) // Or throw an exception
            }

            val initialCollectionEndMillis = firstLaunchTimeMillis + TimeUnit.DAYS.toMillis(initialCollectionDurationDays)
            val predictionWithFeedbackEndMillis = firstLaunchTimeMillis + TimeUnit.DAYS.toMillis(initialCollectionDurationDays + predictionWithFeedbackDurationDays)

            return when {
                currentTimeMillis < initialCollectionEndMillis -> {
                    AppPhaseResult(AppPhase.INITIAL_COLLECTION, initialCollectionEndMillis - currentTimeMillis)
                }
                currentTimeMillis < predictionWithFeedbackEndMillis -> {
                    AppPhaseResult(AppPhase.PREDICTION_WITH_FEEDBACK, predictionWithFeedbackEndMillis - currentTimeMillis)
                }
                else -> {
                    AppPhaseResult(AppPhase.PREDICTION_ONLY, 0L)
                }
            }
        }

        val DEBUG_TAG = "MainActivity"
        const val trackerID = 1L
    }

    private val userRepository by lazy { UserDataStore.getUserRepository(application) }

    private lateinit var sharedPreferences: SharedPreferences
    private val FIRST_LAUNCH_KEY = "first_launch_time"


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                scheduleDataCollection()
            } else {
                // Handle permission denials (e.g., show a message)
            }
        }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private val requestedPermissions = arrayOf(
        android.Manifest.permission.BODY_SENSORS,
        android.Manifest.permission.FOREGROUND_SERVICE,
        android.Manifest.permission.POST_NOTIFICATIONS,
        android.Manifest.permission.ACTIVITY_RECOGNITION,
        android.Manifest.permission.HIGH_SAMPLING_RATE_SENSORS,
        android.Manifest.permission.READ_CONTACTS
    )

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        requestPermissions(requestedPermissions, 0)
        setTheme(android.R.style.Theme_DeviceDefault)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val firstLaunchTime = sharedPreferences.getLong(FIRST_LAUNCH_KEY, 0L)


        initiateInterventionTracker()

        if (firstLaunchTime == 0L) {
            val currentTime = System.currentTimeMillis()
            sharedPreferences.edit().putLong(FIRST_LAUNCH_KEY, currentTime).apply()
        }


        setContent {
            SelectIntervention(userRepository)
        }



        checkAndRequestPermissions()
    }

    private fun initiateInterventionTracker() {
        CoroutineScope(Dispatchers.IO).launch {
            if(userRepository.getInterventionStatsById(trackerID)== null) {
                val interventionStats = InterventionStats(id = trackerID, tag = TAG.INTERVENTIONS)
                userRepository.insertInterventionStats(CoroutineScope(Dispatchers.IO), interventionStats)
                val sender = CommunicationDataSender(applicationContext)
                val interventionStatsString = Json.encodeToString(interventionStats)
                sender.sendStringData("/phone/notification", interventionStatsString)

            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val permissionsToRequest = mutableListOf<String>()

            requestedPermissions.forEach { permission ->
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(permission)
                }
            }

            if (permissionsToRequest.isNotEmpty()) {
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            } else {
                scheduleDataCollection()
            }
        } else {
            scheduleDataCollection()
        }
    }


    private fun scheduleDataCollection() {
        Log.d("DataCollectService", "Scheduling data collection")

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build()


        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(DataCollectWorker::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(constraints).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DataCollectionWork",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )

    }


}

@Composable
fun SelectIntervention(userRepository: com.example.tse_emotionalrecognition.common.data.database.UserRepository) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("emoji_prefs", Context.MODE_PRIVATE)
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
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val firstLaunchTime = sharedPreferences.getLong("first_launch_time", 0L)

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
                    onClick = {  val newEmojiState = EmojiState.entries.random()
                        updateEmoji(context, newEmojiState)

                        // Force UI Update
                        currentEmojiState = newEmojiState },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Randomize Emoji")
                }
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
                        val sessionId = Calendar.getInstance().timeInMillis

                        val intent =
                            Intent(context, DataCollectReciever::class.java)
                        intent.putExtra("COLLECT_DATA", true)
                        intent.putExtra("PHASE", getAppPhase(firstLaunchTime))
                        intent.putExtra("sessionId", sessionId)

                        context.sendBroadcast(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manual Data Collection", textAlign = TextAlign.Center)
                }
            }
            item {
                Button(
                    onClick = {
                        val intent = Intent(context, InfoActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Info")
                }
            }
            item {
                Button(
                    onClick = {
                        val debugIntent = Intent(context, DebugActivity::class.java)
                        context.startActivity(debugIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Debug-Menu")
                }
            }

        }
    }
}


enum class AppPhase {
    INITIAL_COLLECTION,
    PREDICTION_WITH_FEEDBACK,
    PREDICTION_ONLY
}

data class AppPhaseResult(
    val appPhase: AppPhase,
    val timeUntilNextPhaseMillis: Long
)