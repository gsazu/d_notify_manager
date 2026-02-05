
package com.app.dnotifymanager

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.app.dnotifymanager.data.AppDatabase
import com.app.dnotifymanager.ui.AddFilterScreen
import com.app.dnotifymanager.ui.MainScreen
import com.app.dnotifymanager.ui.NotificationPermissionScreen
import com.app.dnotifymanager.ui.theme.DNotifyManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(this)
        val dao = db.filterDao()

        setContent {
            DNotifyManagerTheme {
                var hasNotificationAccess by remember { mutableStateOf(isNotificationServiceEnabled()) }

                val settingsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    hasNotificationAccess = isNotificationServiceEnabled()
                }

                if (hasNotificationAccess) {
                    var currentScreen by remember { mutableStateOf("main") }

                    if (currentScreen == "main") {
                        MainScreen(dao = dao, onAddClick = { currentScreen = "add" })
                    } else {
                        AddFilterScreen(dao = dao, onBack = { currentScreen = "main" })
                    }
                } else {
                    NotificationPermissionScreen(
                        onGrantClick = {
                            settingsLauncher.launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }
                    )
                }
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val names = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return names?.contains(packageName) ?: false
    }
}
