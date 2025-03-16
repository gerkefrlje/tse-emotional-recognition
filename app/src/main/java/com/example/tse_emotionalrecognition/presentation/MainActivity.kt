package com.example.tse_emotionalrecognition.presentation


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Global
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.*
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import coil.ImageLoader
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.UserRepository
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectType
import com.example.tse_emotionalrecognition.presentation.interventions.InterventionOverviewActivity
import com.example.tse_emotionalrecognition.presentation.theme.TSEEmotionalRecognitionTheme
import com.example.tse_emotionalrecognition.presentation.utils.EmojiSelector
import com.example.tse_emotionalrecognition.presentation.utils.EmojiState
import com.example.tse_emotionalrecognition.presentation.utils.scheduleDailyEmojiUpdateWorkManager
import com.example.tse_emotionalrecognition.presentation.utils.updateEmoji
import com.example.tse_emotionalrecognition.presentation.utils.DataCollectReciever
import com.example.tse_emotionalrecognition.presentation.utils.DataCollectWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
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

        if (firstLaunchTime == 0L) {
            val currentTime = System.currentTimeMillis()
            sharedPreferences.edit().putLong(FIRST_LAUNCH_KEY, currentTime).apply()
        }


        setContent {
            SelectIntervention(userRepository)
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val permissionsToRequest = mutableListOf<String>()

            requestedPermissions.forEach { permission ->
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
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

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build()

        //val dataCollectionRequest = PeriodicWorkRequest.Builder(DataCollectWorker::class.java, 15, TimeUnit.MINUTES).setConstraints(constraints).build()

        val periodicWorkRequest = PeriodicWorkRequest.Builder(DataCollectWorker::class.java, 15, TimeUnit.MINUTES)
            .setConstraints(constraints).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork("DataCollectionWork", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest)

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
    var currentEmojiState by remember {
        mutableStateOf(EmojiState.valueOf(prefs.getString("emoji_state", EmojiState.NEUTRAL.name)!!))
    }
    scheduleDailyEmojiUpdateWorkManager(context)

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
                    Text("Transfer")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Button(
                    onClick = {
                        val intent = Intent(context, DataCollectReciever::class.java)
                        intent.putExtra("COLLECT_DATA", true)
                        intent.putExtra("PHASE", AppPhase.INITIAL_COLLECTION)
                        intent.putExtra("sessionId", 1L)

                        context.sendBroadcast(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Sensor")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Button(
                    onClick = {
                        val intent = Intent(context, DataCollectReciever::class.java)
                        intent.putExtra("COLLECT_DATA", true)
                        intent.putExtra("PHASE", AppPhase.INITIAL_COLLECTION)
                        intent.putExtra("sessionId", 1L)

                        context.sendBroadcast(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Sensor")
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


enum class AppPhase {
    INITIAL_COLLECTION,
    PREDICTION_WITH_FEEDBACK,
    PREDICTION_ONLY
}