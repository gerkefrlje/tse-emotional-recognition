package com.example.tse_emotionalrecognition.presentation.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.tse_emotionalrecognition.presentation.interventions.BreathingActivity
import com.example.tse_emotionalrecognition.presentation.interventions.ContactActivity
import com.example.tse_emotionalrecognition.presentation.interventions.MusicActivity
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import kotlin.time.Duration.Companion.seconds

class InterventionTriggerHelper(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "intervention_channel"
        private const val NOTIFICATION_ID = 1
        private const val TWO_HOURS_IN_MILLIS: Long = 7200000 // 2 * 60 * 60 * 1000
        private var lastNotificationTime: Long = 0
        private var lastTriggeredActivity: Class<*>? = null

        val BREATH = 1
        val CONTACT = 2
        val MUSIC = 3
    }

    init {
        createNotificationChannel()
    }

    fun triggerIntervention(value: Double) {
        Log.d("InterventionTriggerHelper", "Received value: $value")
        if (value > 0.75) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastNotificationTime >= TWO_HOURS_IN_MILLIS) {
                val activityToStart = selectRandomActivity()
                showNotification(activityToStart)
                lastNotificationTime = currentTime
                lastTriggeredActivity = activityToStart
            } else {
                Log.d("InterventionTriggerHelper", "Notification suppressed due to time limit")
            }
        }
    }

    fun testTrigger(int: Int) {
        when(int){
            BREATH -> {
                val activityToStart = BreathingActivity::class.java
                showNotification(activityToStart)
                lastTriggeredActivity = activityToStart
            }
            CONTACT -> {
                val activityToStart = ContactActivity::class.java
                showNotification(activityToStart)
                lastTriggeredActivity = activityToStart
            }
            MUSIC -> {
                val activityToStart = MusicActivity::class.java
                showNotification(activityToStart)
                lastTriggeredActivity = activityToStart
            }
        }
    }

    fun showRandomIntervention(){
        val activityToStart = selectRandomActivity()
        showNotification(activityToStart)
        lastTriggeredActivity = activityToStart
    }

    private fun selectRandomActivity(): Class<*> {
        val activities = listOf(
            BreathingActivity::class.java,
            ContactActivity::class.java,
            MusicActivity::class.java
        )
        val availableActivities = activities.filter { it != lastTriggeredActivity }
        return if (availableActivities.isNotEmpty()) {
            availableActivities.random()
        } else {
            activities.random()
        }
    }

    private fun showNotification(activityClass: Class<*>) {
        val intent = Intent(context, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        //Todo info button
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.example.tse_emotionalrecognition.R.drawable.splash_icon) // Ersetze durch dein Icon
            .setContentTitle("Intervention suggested")
            .setContentText(notificationText(activityClass))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
        Log.d("InterventionTriggerHelper", "Notification shown for activity: ${activityClass.simpleName}")
    }

    private fun createNotificationChannel() {
        val name = "Intervention Channel"
        val descriptionText = "Channel for intervention notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun notificationText(activityClass: Class<*>): String {
        return when (activityClass) {
            BreathingActivity::class.java -> "Let's take a breath"
            ContactActivity::class.java -> "Do you want to call someone?"
            MusicActivity::class.java -> "How about listening to your favorite song?"
            else -> "Unknown"
        }
    }
}