package com.example.tse_emotionalrecognition.presentation.utils


import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.UserRepository
import com.example.tse_emotionalrecognition.common.data.database.entities.TAG
import com.example.tse_emotionalrecognition.common.data.database.utils.CommunicationDataSender
import com.example.tse_emotionalrecognition.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NotificationMonitor() : BroadcastReceiver() {



    override fun onReceive(context: Context, intent: Intent) {
        val userRepository = UserDataStore.getUserRepository(context)

        val sharedPreferences =
            context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val isAlreadyHandled = sharedPreferences.getBoolean(
            "dismissed",
            false
        )

        if (isAlreadyHandled) {
            Log.v("NotificationMonitor", "Notification already handled, skipping...")
            return
        }

        // Setze das Flag auf "true", damit es nicht erneut verarbeitet wird
        sharedPreferences.edit()
            .putBoolean("dismissed", true).apply()


        updateNotificationTracker(userRepository, context)


        Log.v("NotificationMonitor", "Notification dismissed")
    }




private fun updateNotificationTracker(userRepository: UserRepository, context: Context) {

    CoroutineScope(Dispatchers.IO).launch {

            userRepository.incrementDismissed(
                CoroutineScope(Dispatchers.IO),
                MainActivity.trackerID
            )
            val sender = CommunicationDataSender(context)

        delay(5000L)

        val interventionStats = userRepository.getInterventionStatsById(MainActivity.trackerID)
        val interventionStatsString = Json.encodeToString(interventionStats)

        sender.sendStringData("/phone/notification", interventionStatsString)
    }

}
}