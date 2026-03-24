package com.app.dnotifymanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.app.dnotifymanager.R
import com.app.dnotifymanager.data.FilterDao
import com.app.dnotifymanager.data.FilterEntity
import kotlinx.coroutines.launch

@Composable
fun AddFilterScreen(dao: FilterDao, onBack: () -> Unit) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var selectedTune by remember { mutableStateOf(Tune(title = "Default", uri = "android.resource://${context.packageName}/${R.raw.my_special_tune}".toUri())) }
    var showTuneSelection by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showTuneSelection) {
        TuneSelectionScreen(
            onTuneSelected = {
                selectedTune = it
                showTuneSelection = false
            },
            onDismiss = { showTuneSelection = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter Keyword") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = selectedTune.title,
            onValueChange = { },
            label = { Text("Selected Tune") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showTuneSelection = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Tune")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (text.isNotBlank()) {
                    scope.launch {
                        dao.insert(FilterEntity(keyword = text, tune = selectedTune.uri.toString()))
                        onBack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Filter")
        }
    }
}