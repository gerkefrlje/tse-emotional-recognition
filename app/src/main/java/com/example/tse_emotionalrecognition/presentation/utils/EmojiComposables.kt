package com.example.tse_emotionalrecognition.presentation.utils

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import coil.request.repeatCount
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign


@Composable
fun LoopingGifImage(
    @DrawableRes gifRes: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components { add(GifDecoder.Factory()) }
        .build()

    val imageRequest = ImageRequest.Builder(context)
        .data(gifRes)
        .repeatCount(1)
        .build()

    AsyncImage(
        model = imageRequest,
        contentDescription = "Animated Emoji",
        imageLoader = imageLoader,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun EmojiSelector(currentEmojiState: EmojiState) {
    var showDialog by remember { mutableStateOf(false) }
    var isNonAlertState = false
    LoopingGifImage(
        gifRes = getEmojiResForState(currentEmojiState),
        modifier = Modifier.clickable { showDialog = true }
    )

    isNonAlertState = currentEmojiState == EmojiState.NEUTRAL || currentEmojiState == EmojiState.HAPPY || currentEmojiState == EmojiState.UNHAPPY

    if (showDialog && !isNonAlertState) {
        AlertDialog(
            containerColor = Color.Black,
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Hey Du!",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (currentEmojiState) {
                        EmojiState.NEUTRAL_ALERT -> {
                            Text(text = "aktuell bist du ausgegelichen", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                        EmojiState.HAPPY_ALERT -> {
                            Text(text = "aktuell bist du gut gelaunt", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                        EmojiState.UNHAPPY_ALERT -> {
                            Text(text = "bist du aktuell unzufrieden?", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                        else -> { Text(text = "aktuell bist du unzufrieden", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)}
                    }

                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                }
            }
        )
    }
}