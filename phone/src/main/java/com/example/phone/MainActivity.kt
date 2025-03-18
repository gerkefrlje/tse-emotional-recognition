package com.example.phone

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import com.example.phone.ui.theme.TSEEmotionalRecognitionTheme
import com.example.tse_emotionalrecognition.common.data.database.utils.InterventionTrackerViewModel
import com.example.tse_emotionalrecognition.common.data.database.utils.InterventionTrackerViewModelFactory
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore


class MainActivity : ComponentActivity() {

    private val interventionTrackerViewModel: InterventionTrackerViewModel by viewModels {
        InterventionTrackerViewModelFactory(
            UserDataStore.getUserRepository(
                applicationContext
            )
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val interventionStats by interventionTrackerViewModel.interventionStats.observeAsState()
            val completedCount = interventionStats?.triggeredCount ?: 0
            val missedCount = interventionStats?.dismissedCount ?: 0

            TSEEmotionalRecognitionTheme {
                MainScreen(completedCount, missedCount)
            }
        }
    }
}

/**
 * A donut chart (ring-style pie chart) that takes the completed and missed values.
 * The full circle represents the sum of both values. The green arc shows the percentage of
 * completed interventions, and the red arc shows the remaining portion.
 */
@Composable
fun DonutChart(
    completed: Int,
    missed: Int,
    modifier: Modifier = Modifier,
    donutThickness: Dp = 20.dp
) {
    Canvas(modifier = modifier) {
        val total = completed + missed
        val strokeWidth = donutThickness.toPx()
        if (total == 0) {
            // Draw a placeholder full circle when there is no data.
            drawArc(
                color = Color.Gray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        } else {
            val completedAngle = (completed / total.toFloat()) * 360f
            val missedAngle = 360f - completedAngle
            // Draw completed interventions arc in green.
            drawArc(
                color = Color.Green,
                startAngle = -90f,
                sweepAngle = completedAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Draw missed interventions arc in red.
            drawArc(
                color = Color.Red,
                startAngle = -90f + completedAngle,
                sweepAngle = missedAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * MainScreen displays the donut chart behind the intervention numbers and titles.
 * It also includes a Menu button at the bottom center.
 */
@Composable
fun MainScreen(completedCount: Int, missedCount: Int) {
    // Example dynamic values; replace these with dynamic state or repository values as needed.
    val completed = completedCount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
                .background(
                    color = Color(0xFF202020),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ), // Adjust this value for your top third position
            contentAlignment = Alignment.Center // Centers content inside the Box
        ) {
            Text(
                text = "Companion App",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Use a Column to stack the text box and the donut chart container vertically.
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Text Box above the donut chart.
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFF202020),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Your Intervention Statistics",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            // Central Box for the donut chart and the texts.
            Box(
                modifier = Modifier.size(250.dp)
            ) {
                // Draw the donut chart.
                DonutChart(
                    completed = completed,
                    missed = missedCount,
                    modifier = Modifier.fillMaxSize(),
                    donutThickness = 35.dp
                )
                // Overlay the texts in the center.
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Completed",
                        color = Color.Green,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "$completed",
                        color = Color.Green,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Missed",
                        color = Color.Red,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "$missedCount",
                        color = Color.Red,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        // Menu button placed a bit higher from the bottom.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            MenuButton()
        }
    }
}

/**
 * MenuButton displays a large button. When tapped, it opens a centered pop up dialog.
 */
@Composable
fun MenuButton() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF202020),
                contentColor = Color.White
            ),
            modifier = Modifier
                .height(60.dp)      // Bigger height
                .fillMaxWidth(0.6f) // Wider button
        ) {
            Text(text = "Menu", fontSize = 20.sp)
        }
        if (expanded) {

            CenteredMenuDialog(
                onDismissRequest = { expanded = false },
                onContactConfig = {
                    context.startActivity(Intent(context, MLTest::class.java))
                    expanded = false
                },
                onMusicConfig = {
                    // TODO: forward to Music Configuration activity
                    expanded = false
                },
                onLabelConfig = {
                    context.startActivity(Intent(context, ReceiveDataActivity::class.java))
                    expanded = false
                }
            )
        }
    }
}

/**
 * CenteredMenuDialog creates a dialog centered in the middle of the screen.
 * It uses a dark blue background and displays three menu options with dividers between them.
 */
@Composable
fun CenteredMenuDialog(
    onDismissRequest: () -> Unit,
    onContactConfig: () -> Unit,
    onMusicConfig: () -> Unit,
    onLabelConfig: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(dismissOnClickOutside = true)
    ) {
        // Wrap the content in a Box that fills the screen.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter  // Align content at the bottom center.
        ) {
            // Use padding or Modifier.offset to move the dialog upward from the very bottom.
            Surface(
                modifier = Modifier

                    .padding(bottom = 90.dp)  // Adjust this value to move the dialog up/down.
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                color = Color(0xFF202020) // Dark blue background
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MenuDialogItem(text = "Contact Configuration", onClick = onContactConfig)
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    MenuDialogItem(text = "Music Configuration", onClick = onMusicConfig)
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    MenuDialogItem(text = "Label Configuration", onClick = onLabelConfig)
                }
            }
        }
    }
}

/**
 * MenuDialogItem displays an individual menu option that is clickable.
 */
@Composable
fun MenuDialogItem(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 18.sp, color = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    TSEEmotionalRecognitionTheme {
        MainScreen(0, 0)
    }
}