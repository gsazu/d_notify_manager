package com.app.dnotifymanager.ui

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.app.dnotifymanager.R
import androidx.core.net.toUri

@Composable
fun TuneSelectionScreen(
    onTuneSelected: (Tune) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val ringtones = remember { getRingtones(context) }
    var currentlyPlaying by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a Tune") },
        text = {
            LazyColumn {
                items(ringtones) { tune ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTuneSelected(tune) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tune.title, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            if (currentlyPlaying == tune.uri.toString()) {
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                mediaPlayer = null
                                currentlyPlaying = null
                            } else {
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                mediaPlayer = MediaPlayer.create(context, tune.uri).apply {
                                    start()
                                    setOnCompletionListener { currentlyPlaying = null }
                                }
                                currentlyPlaying = tune.uri.toString()
                            }
                        }) {
                            Icon(
                                if (currentlyPlaying == tune.uri.toString()) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause"
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class Tune(val title: String, val uri: Uri)

private fun getRingtones(context: Context): List<Tune> {
    val tunes = mutableListOf<Tune>()

    // Add default ringtone from raw resources
    val defaultRingtoneUri =
        "android.resource://${context.packageName}/${R.raw.my_special_tune}".toUri()
    tunes.add(Tune("Default", defaultRingtoneUri))

    // Add system notification tones
    val ringtoneManager = RingtoneManager(context)
    ringtoneManager.setType(RingtoneManager.TYPE_NOTIFICATION)
    val cursor = ringtoneManager.cursor
    while (cursor.moveToNext()) {
        val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
        val uri = ringtoneManager.getRingtoneUri(cursor.position)
        tunes.add(Tune(title, uri))
    }

    // Add audio files from storage
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE
    )
    val audioCursor = context.contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        null
    )
    audioCursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val title = it.getString(titleColumn)
            val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
            tunes.add(Tune(title, uri))
        }
    }

    return tunes.distinctBy { it.uri }
}