package com.example.tse_emotionalrecognition.complication

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.presentation.MainActivity
import java.util.Calendar

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
        val smallImage = SmallImage(neutralIcon, SmallImageType.PHOTO, neutralIcon)
        val tapIntent = createMainActivityTapIntent()
        return SmallImageComplicationData.Builder(
            smallImage = neutralIcon,
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
            else -> Pair(R.drawable.neutral_emoji, "Neutral Emoji")
        }

        val icon = Icon.createWithResource(this, emojiIconRes)
        val tapIntent = createMainActivityTapIntent()

        return SmallImageComplicationData.Builder(
            smallImage = icon,
            contentDescription = PlainComplicationText.Builder(descriptionText).build()
        )
            .setTapAction(tapIntent)
            .build()
    }

    // Helper function to create the tap action PendingIntent
    private fun createMainActivityTapIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            // Optionally set flags if you need to clear existing activities
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    // Placeholder: replace with your actual logic to determine the emoji state
    private fun getEmojiState(): String {
        // For example, this might check a value from a repository, data store, or sensor
        return "neutral" // Or "happy", "sad"
    }
}