package com.app.dnotifymanager.service

import android.media.MediaPlayer
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.app.dnotifymanager.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.app.dnotifymanager.R
import android.content.BroadcastReceiver
import android.content.IntentFilter

class NotificationReceiver : NotificationListenerService() {

    private lateinit var db: AppDatabase
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val processedNotifications = mutableSetOf<String>()

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.app.dnotifymanager.STOP_ACTION") {
                AudioPlayer.stop()
                try {
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(MATCH_NOTIFICATION_ID)
                } catch (e: Exception) {
                    Log.e("NotificationReceiver", "Error dismissing notification", e)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getDatabase(applicationContext)
        val filter = IntentFilter("com.app.dnotifymanager.STOP_ACTION")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(stopReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(stopReceiver, filter)
            }
        } catch (e: Exception) {
            Log.e("NotificationReceiver", "Error registering receiver", e)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationReceiver", "Listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (sbn.packageName == packageName) return

        val key = sbn.key
        synchronized(processedNotifications) {
            if (processedNotifications.contains(key)) return
        }

        val title = sbn.notification?.extras?.getString("android.title")?.lowercase() ?: ""
        val text = sbn.notification?.extras?.getString("android.text")?.lowercase() ?: ""

        Log.d("notificationValues", "onNotificationPosted: $title, $text")

        scope.launch {
            val filters = db.filterDao().getAllFilters().first()

            for (filter in filters) {
                if (title.contains(filter.keyword.lowercase()) || text.contains(filter.keyword.lowercase())) {
                    synchronized(processedNotifications) {
                        if (processedNotifications.contains(key)) return@launch
                        processedNotifications.add(key)
                        if (processedNotifications.size > 200) {
                            val toKeep = processedNotifications.toList().takeLast(100)
                            processedNotifications.clear()
                            processedNotifications.addAll(toKeep)
                        }
                    }

                    playCustomTune(filter.tune) {
                        try {
                            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancel(MATCH_NOTIFICATION_ID)
                        } catch (e: Exception) {
                            Log.e("NotificationReceiver", "Error dismissing notification", e)
                        }
                    }
                    showMatchedNotification(title, text)
                    break
                }
            }
        }
    }

    private fun playCustomTune(tuneUri: String, onComplete: () -> Unit) {
        if (tuneUri.isBlank()) {
            onComplete()
            return
        }
        AudioPlayer.play(applicationContext, tuneUri.toUri(), onComplete)
    }

    private val MATCH_NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "dnotify_silent_match_channel"

    private fun showMatchedNotification(matchedTitle: String, matchedText: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Matched Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for matched keywords"
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val stopIntent = Intent("com.app.dnotifymanager.STOP_ACTION").apply {
            setPackage(packageName)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            MATCH_NOTIFICATION_ID,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_pause,
            "Stop",
            stopPendingIntent
        ).build()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(matchedTitle.ifBlank { "Matched Notification" })
            .setContentText(matchedText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(0)
            .setSilent(true)
            .setAutoCancel(false)
            .addAction(stopAction)
            .build()
            
        notificationManager.notify(MATCH_NOTIFICATION_ID, notification)
    }


    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("NotificationReceiver", "Listener disconnected")
        cleanup()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(stopReceiver)
        } catch (e: Exception) {}
        cleanup()
    }

    private fun cleanup() {
        scope.cancel()
        AudioPlayer.stop()
    }
}