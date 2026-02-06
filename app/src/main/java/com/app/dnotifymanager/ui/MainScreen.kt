package com.app.dnotifymanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.dnotifymanager.data.FilterDao
import com.app.dnotifymanager.data.FilterEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(dao: FilterDao, onAddClick: () -> Unit) {
    var permissionsGranted by remember { mutableStateOf(false) }

    if (!permissionsGranted) {
        PermissionRequester {
            permissionsGranted = true
        }
    }

    if (permissionsGranted) {
        val filters by dao.getAllFilters().collectAsState(initial = emptyList())
        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("DNotify Manager") },
                    actions = {
                        IconButton(onClick = onAddClick) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                )
            }
        ) { padding ->
            if (filters.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No notification filter found")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(filters) { filter ->
                        ListItem(
                            headlineContent = { Text(filter.keyword) },
                            trailingContent = {
                                IconButton(onClick = { scope.launch { dao.delete(filter) } }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}