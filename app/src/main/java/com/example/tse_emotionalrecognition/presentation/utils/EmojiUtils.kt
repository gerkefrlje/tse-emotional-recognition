package com.example.tse_emotionalrecognition.presentation.utils

import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import android.content.ComponentName
import com.example.tse_emotionalrecognition.R
import com.example.tse_emotionalrecognition.complication.MainComplicationService
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Intent
import android.util.Log
import java.util.Calendar

// WorkManager imports
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import java.util.concurrent.TimeUnit


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

// Retrieves two float values 'pref_x' and 'pref_y' from shared preferences, computes their ratio,
// and updates the emoji state accordingly. When x/y = 0, set UNHAPPY_ALERT; when x/y = 1, set HAPPY_ALERT;
// when 0 < x/y < 1, set NEUTRAL_ALERT. Uses "emoji_prefs" as the preference file and dummy keys "pref_x" and "pref_y".
fun dailyEmojiUpdate(context: Context) {
    /*
    val prefs = context.getSharedPreferences("emoji_prefs", Context.MODE_PRIVATE)
    // Dummy preference keys with default values: 0f for x and 1f for y
    val x = prefs.getFloat("pref_x", 0f)
    val y = prefs.getFloat("pref_y", 1f)

    // Avoid division by zero
    if (y == 0f) {
        updateEmoji(context, EmojiState.UNHAPPY_ALERT)
        return
    }

    val ratio = x / y
    when {
        ratio == 0f -> updateEmoji(context, EmojiState.UNHAPPY_ALERT)
        ratio == 1f -> updateEmoji(context, EmojiState.HAPPY_ALERT)
        ratio > 0f && ratio < 1f -> updateEmoji(context, EmojiState.NEUTRAL_ALERT)
        else -> updateEmoji(context, EmojiState.NEUTRAL_ALERT) // Fallback
    }*/

    updateEmoji(context, EmojiState.entries.random())

}

// Worker class for daily emoji update using WorkManager.
class DailyEmojiWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        dailyEmojiUpdate(applicationContext)
        Log.d("DailyEmojiWorker", "Emoji updated")
        return Result.success()
    }
}

// Schedules a daily emoji update using WorkManager. This method calculates the initial delay until 1 PM and then
// enqueues a periodic work request that runs every day.
fun scheduleDailyEmojiUpdateWorkManager(context: Context) {
    val currentTime = System.currentTimeMillis()

    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentTime
        set(Calendar.HOUR_OF_DAY, 0) // 1 PM in 24-hour format
        set(Calendar.MINUTE, 11)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    Log.d("DailyEmojiWorker", "Scheduled for: ${calendar.time}")
    // If 1 PM has already passed today, schedule for tomorrow
    if (calendar.timeInMillis < currentTime) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    val initialDelay = calendar.timeInMillis - currentTime

    val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyEmojiWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "DailyEmojiWork",
        ExistingPeriodicWorkPolicy.UPDATE,
        dailyWorkRequest
    )
}

// just for testing purposes
fun scheduleDailyEmojiUpdateWorkManagerr(context: Context) {
    val currentTime = System.currentTimeMillis()
    // For testing: schedule the work to run after 20 seconds
    val initialDelay = 10 * 1000L  // 20 seconds delay

    Log.d("DailyEmojiWorker", "OneTime work is being scheduled to run in 20 seconds")

    val oneTimeWorkRequest = OneTimeWorkRequestBuilder<DailyEmojiWorker>()
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
    Log.d("DailyEmojiWorker", "OneTime Worker scheduled successfully")
}