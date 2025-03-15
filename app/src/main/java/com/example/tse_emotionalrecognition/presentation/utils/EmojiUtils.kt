package com.example.tse_emotionalrecognition.presentation.utils

import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import android.content.ComponentName
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.complication.MainComplicationService

enum class EmojiState {
    NEUTRAL, HAPPY, UNHAPPY, NEUTRAL_ALERT, HAPPY_ALERT, UNHAPPY_ALERT
}

fun getEmojiResForState(state: EmojiState): Int {
    return when (state) {
        EmojiState.NEUTRAL -> R.drawable.neutral_emoji_animated
        EmojiState.HAPPY -> R.drawable.happy_emoji_animated
        EmojiState.UNHAPPY -> R.drawable.unhappy_emoji_animated
        EmojiState.NEUTRAL_ALERT -> R.drawable.neutral_emoji_alert_animated
        EmojiState.HAPPY_ALERT -> R.drawable.happy_emoji_alert_animated
        EmojiState.UNHAPPY_ALERT -> R.drawable.unhappy_emoji_alert_animated
    }
}

fun updateEmoji(context: Context, state: EmojiState) {
    val prefs = context.getSharedPreferences("emoji_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("emoji_state", state.name).apply()

    val requester = ComplicationDataSourceUpdateRequester.create(
        context,
        ComponentName(context, MainComplicationService::class.java)
    )
    requester.requestUpdateAll()
}
