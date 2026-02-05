package com.app.dnotifymanager.service

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.app.dnotifymanager.data.AppDatabase
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationReceiver : NotificationListenerService() {

    private lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "filter-db").build()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val title = sbn?.notification?.extras?.getString("android.title")?.lowercase() ?: ""
        val text = sbn?.notification?.extras?.getString("android.text")?.lowercase() ?: ""

        Log.d("notificationValues", "onNotificationPosted: $title, $text")

        CoroutineScope(Dispatchers.IO).launch {
            val filters = db.filterDao().getAllFilters().first()

            for (filter in filters) {
                if (title.contains(filter.keyword.lowercase()) || text.contains(filter.keyword.lowercase())) {
                    playCustomTune()
                    break
                }
            }
        }
    }

//    private fun playCustomTune() {
//        try {
//            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
//            val mp = MediaPlayer.create(applicationContext, notificationUri)
//            mp.start()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    private fun playCustomTune() {
        try {
            // R.raw.my_special_tune aapki file ka naam hai
            val mp = MediaPlayer.create(applicationContext, com.app.dnotifymanager.R.raw.my_special_tune)

            mp.setOnCompletionListener {
                it.release() // Sound khatam hone par memory free kar dega
            }

            mp.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}