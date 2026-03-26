package com.app.dnotifymanager.service

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

object AudioPlayer {
    var mediaPlayer: MediaPlayer? = null
    var onCompleteAction: (() -> Unit)? = null

    @Synchronized
    fun play(context: Context, tuneUri: Uri, onComplete: (() -> Unit)? = null) {
        stop()
        onCompleteAction = onComplete
        try {
            mediaPlayer = MediaPlayer.create(context, tuneUri).apply {
                setOnCompletionListener {
                    onCompleteAction?.invoke()
                    it.release()
                    mediaPlayer = null
                    onCompleteAction = null
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioPlayer", "MediaPlayer error: what=$what, extra=$extra")
                    mp.release()
                    mediaPlayer = null
                    true
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing custom tune", e)
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    @Synchronized
    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error stopping mediaPlayer", e)
        }
    }
}
