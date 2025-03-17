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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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
                } else {
                    Log.e("NotificationMonitor", "Failed to delete AffectData")
                }


            }
        }


    }

}