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

        Log.v("NotificationMonitor", "Notification dismissed")
        val affectDataId = intent.getLongExtra("affectDataId", -1L)
        if (affectDataId != -1L) {
            userRepository.deleteAffect(
                CoroutineScope(Dispatchers.IO), affectDataId,
            ) { deletedID ->
                if (deletedID != null) {
                    Log.v("NotificationMonitor", "AffectData deleted with ID: $deletedID")
                    CoroutineScope(Dispatchers.IO).launch {
                        updateNotificationTracker(userRepository, context)
                    }
                } else {
                    Log.e("NotificationMonitor", "Failed to delete AffectData")
                }


            }
        }





    }

    private suspend fun updateNotificationTracker(userRepository: UserRepository, context: Context){
        Log.v("NotificationMonitor", "Notification dismissed")

        userRepository.incrementDismissed(CoroutineScope(Dispatchers.IO), MainActivity.trackerID)
        val sender = CommunicationDataSender(context)

        delay(5000L)

        val interventionStats = userRepository.getInterventionStatsByTag(TAG.INTERVENTIONS)
        val interventionStatsString = Json.encodeToString(interventionStats)

        sender.sendStringData("/phone/notification", interventionStatsString)
    }

}