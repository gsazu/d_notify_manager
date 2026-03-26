package com.app.dnotifymanager.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.app.dnotifymanager.STOP_ACTION") {
            // Stop playing the tune
            AudioPlayer.stop()
            
            // Dismiss the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)
            if (notificationId != -1) {
                notificationManager.cancel(notificationId)
            }
        }
    }
}
