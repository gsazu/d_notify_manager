package com.app.dnotifymanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.dnotifymanager.data.FilterDao
import com.app.dnotifymanager.data.FilterEntity
import kotlinx.coroutines.launch

@Composable
fun AddFilterScreen(dao: FilterDao, onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter Keyword") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (text.isNotBlank()) {
                    scope.launch {
                        dao.insert(FilterEntity(keyword = text))
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