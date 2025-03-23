package com.example.tse_emotionalrecognition.complication

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.presentation.MainActivity

/**
 * Skeleton for complication data source that returns short text.
 */
class MainComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SMALL_IMAGE) {
            return null
        }
        // Use a neutral emoji for preview purposes
        val neutralIcon = Icon.createWithResource(this, R.drawable.unhappy_emoji)
        // Wrap the Icon into a SmallImage
        val smallImage = SmallImage.Builder(neutralIcon, SmallImageType.PHOTO).build()
        val tapIntent = createMainActivityTapIntent()
        return SmallImageComplicationData.Builder(
            smallImage = smallImage,
            contentDescription = PlainComplicationText.Builder("Neutral Emoji").build()
        )
            .setTapAction(tapIntent)
            .build()
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        // Determine which emoji to show based on your app's state/data.
        // For demonstration, we'll use a simple condition.
        val emojiState = getEmojiState() // Implement your logic to return "happy", "sad", or "neutral"
        val (emojiIconRes, descriptionText) = when (emojiState) {
            "happy" -> Pair(R.drawable.happy_emoji, "Happy Emoji")
            "sad" -> Pair(R.drawable.unhappy_emoji, "Sad Emoji")
            "happy_alert" -> Pair(R.drawable.happy_emoji_alert, "Happy Alert Emoji")
            "sad_alert" -> Pair(R.drawable.unhappy_emoji_alert, "Sad Alert Emoji")
            "neutral_alert" -> Pair(R.drawable.neutral_emoji_alert, "Neutral Alert Emoji")
            else -> Pair(R.drawable.neutral_emoji, "Neutral Emoji")
        }

//        val (emojiIconResource, text) = when (emojiState) {
//            "sad_alert" -> Pair(R.drawable.unhappy_emoji_alert, "Sad Alert Emoji")
//            else -> Pair(R.drawable.mo, "Neutral Emoji")
//        }



        val icon = Icon.createWithResource(this, emojiIconRes)
        val smallImage = SmallImage.Builder(icon, SmallImageType.PHOTO).build()
        val tapIntent = createMainActivityTapIntent()

        return SmallImageComplicationData.Builder(
            smallImage = smallImage,
            contentDescription = PlainComplicationText.Builder(descriptionText).build()
        )
            .setTapAction(tapIntent)
            .build()
    }

    @SuppressLint("WearRecents")
    private fun createMainActivityTapIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        // Use FLAG_MUTABLE instead of FLAG_IMMUTABLE
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT  or PendingIntent.FLAG_MUTABLE
        )
    }

    private fun getEmojiState(): String {
        val prefs = getSharedPreferences("emoji_prefs", MODE_PRIVATE)
        val state = prefs.getString("emoji_state", "NEUTRAL") ?: "NEUTRAL"
        return when(state.uppercase()) {
            "HAPPY" -> "happy"
            "UNHAPPY" -> "sad"
            "HAPPY_ALERT" -> "happy_alert"
            "UNHAPPY_ALERT" -> "sad_alert"
            "NEUTRAL_ALERT" -> "neutral_alert"
            else -> "neutral"
        }
    }
}