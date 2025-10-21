package com.uniguard.netguard_app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.presentation.ui.components.*
import com.uniguard.netguard_app.presentation.ui.theme.NetGuardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit
) {
    NetGuardTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Incident History") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Filter Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Server") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Status") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // History List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(10) { index ->
                        IncidentHistoryCard(
                            serverName = "Server ${(index % 3) + 1}",
                            status = if (index % 2 == 0) "DOWN" else "RESOLVED",
                            timestamp = "${index + 1} hours ago",
                            duration = if (index % 2 == 1) "1h 45m" else null,
                            resolvedBy = if (index % 2 == 1) "John Doe" else null,
                            onResolveClick = if (index % 2 == 0) { { /* TODO */ } } else null
                        )
                    }
                }
            }
        }
    }
}