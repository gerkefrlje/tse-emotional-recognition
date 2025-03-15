package com.example.tse_emotionalrecognition.presentation.utils

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import coil.request.repeatCount


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

    LoopingGifImage(
        gifRes = getEmojiResForState(currentEmojiState),
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Emoji Info") },
            text = { Text(text = currentEmojiState.name) },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}