/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.tse_emotionalrecognition.presentation

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Global
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.data.database.UserDataStore
import com.example.tse_emotionalrecognition.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.data.database.entities.AffectType
import com.example.tse_emotionalrecognition.data.database.UserRepository
import com.example.tse_emotionalrecognition.presentation.theme.TSEEmotionalRecognitionTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

class MainActivity : ComponentActivity() {
    private val userRepository by lazy { UserDataStore.getUserRepository(application) }


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            //WearApp("Android")
            SelectIntervention(userRepository)
        }
    }

    private fun triggerLable() {
        userRepository.insertAffect(
            CoroutineScope(Dispatchers.IO)
            , AffectData(sessionId = 1, affect = AffectType.HAPPY_RELAXED)
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
fun SelectIntervention(userRepository: UserRepository) {
    val context = LocalContext.current

    TSEEmotionalRecognitionTheme {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(16.dp)
        ) {
            item {
                Button(
                    onClick = {
                        val intent = Intent(context, BreathingActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Breathing Intervention")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Button(
                    onClick = {
                        userRepository.insertAffect(
                            CoroutineScope(Dispatchers.IO),
                            AffectData(sessionId = 1, affect = AffectType.HAPPY_RELAXED)
                        ) {
                            val affectDataID = it.id
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
                        val intent = Intent(context, MusicActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Music Activity")
                }
            }
        }
    }
}
