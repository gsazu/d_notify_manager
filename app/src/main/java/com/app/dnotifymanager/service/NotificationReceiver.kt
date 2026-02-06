package com.app.dnotifymanager.service

import android.media.MediaPlayer
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.app.dnotifymanager.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class NotificationReceiver : NotificationListenerService() {

    private lateinit var db: AppDatabase
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getDatabase(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val title = sbn?.notification?.extras?.getString("android.title")?.lowercase() ?: ""
        val text = sbn?.notification?.extras?.getString("android.text")?.lowercase() ?: ""

        Log.d("notificationValues", "onNotificationPosted: $title, $text")

        CoroutineScope(Dispatchers.IO).launch {
            val filters = db.filterDao().getAllFilters().first()

            for (filter in filters) {
                if (title.contains(filter.keyword.lowercase()) || text.contains(filter.keyword.lowercase())) {
                    playCustomTune(filter.tune)
                    break
                }
            }
        }
    }

    private fun playCustomTune(tuneUri: String) {
        if (tuneUri.isBlank()) {
            return
        }

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()

            val uri = tuneUri.toUri()
            mediaPlayer = MediaPlayer.create(applicationContext, uri).apply {
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("NotificationReceiver", "MediaPlayer error: what=$what, extra=$extra")
                    mp.release()
                    mediaPlayer = null
                    true
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("NotificationReceiver", "Error playing custom tune", e)
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}