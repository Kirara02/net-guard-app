package com.uniguard.netguard_app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.presentation.ui.components.*
import com.uniguard.netguard_app.presentation.viewmodel.HistoryViewModel
import com.uniguard.netguard_app.utils.formatRelativeTime
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResolveDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    serverName: String
) {
    var comment by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(Res.string.history_resolve_incident)) },
            text = {
                Column {
                    Text(stringResource(Res.string.history_resolve_for_server, serverName))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text(stringResource(Res.string.history_resolution_comment)) },
                        placeholder = { Text(stringResource(Res.string.history_resolution_placeholder)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (comment.isNotBlank()) {
                            onConfirm(comment)
                            comment = ""
                        }
                    },
                    enabled = comment.isNotBlank()
                ) {
                    Text(stringResource(Res.string.history_resolve))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.history_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = rememberKoinViewModel<HistoryViewModel>(),
    onNavigateBack: () -> Unit
) {
    val histories by viewModel.histories.collectAsState()
    val filteredHistories by viewModel.filteredHistories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val serverFilter by viewModel.serverFilter.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val serverOptions by viewModel.serverOptions.collectAsState()
    val statusOptions by viewModel.statusOptions.collectAsState()

    var showResolveDialog by remember { mutableStateOf(false) }
    var selectedHistory by remember { mutableStateOf<com.uniguard.netguard_app.domain.model.History?>(null) }

    var serverDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.refresh))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            stringResource(Res.string.history_loading),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Filter Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Server Dropdown
                        ExposedDropdownMenuBox(
                            expanded = serverDropdownExpanded,
                            onExpandedChange = { serverDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = serverFilter,
                                onValueChange = { },
                                label = { Text(stringResource(Res.string.history_server)) },
                                placeholder = { Text(stringResource(Res.string.history_select_server)) },
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.menuAnchor(
                                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = serverDropdownExpanded,
                                onDismissRequest = { serverDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.history_all_servers)) },
                                    onClick = {
                                        viewModel.updateServerFilter("")
                                        serverDropdownExpanded = false
                                    }
                                )
                                serverOptions.forEach { server ->
                                    DropdownMenuItem(
                                        text = { Text(server) },
                                        onClick = {
                                            viewModel.updateServerFilter(server)
                                            serverDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Status Dropdown
                        ExposedDropdownMenuBox(
                            expanded = statusDropdownExpanded,
                            onExpandedChange = { statusDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = statusFilter,
                                onValueChange = { },
                                label = { Text(stringResource(Res.string.history_status)) },
                                placeholder = { Text(stringResource(Res.string.history_select_status)) },
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.menuAnchor(
                                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = statusDropdownExpanded,
                                onDismissRequest = { statusDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.history_all_statuses)) },
                                    onClick = {
                                        viewModel.updateStatusFilter("")
                                        statusDropdownExpanded = false
                                    }
                                )
                                statusOptions.forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status) },
                                        onClick = {
                                            viewModel.updateStatusFilter(status)
                                            statusDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Clear Filters Button
                    if (serverFilter.isNotEmpty() || statusFilter.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { viewModel.clearFilters() }) {
                                Text(stringResource(Res.string.history_clear_filters))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // History List
                    val displayHistories = if (serverFilter.isNotEmpty() || statusFilter.isNotEmpty()) {
                        filteredHistories
                    } else {
                        histories
                    }

                    if (displayHistories.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (histories.isEmpty()) stringResource(Res.string.history_no_history) else stringResource(Res.string.history_no_matches),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(displayHistories) { history ->
                                IncidentHistoryCard(
                                    serverName = history.serverName,
                                    status = history.status,
                                    timestamp = formatRelativeTime(history.timestamp),
                                    duration = null, // TODO: Calculate duration if needed
                                    resolvedBy = history.resolvedBy,
                                    onResolveClick = if (history.status == "DOWN") {
                                        {
                                            selectedHistory = history
                                            showResolveDialog = true
                                        }
                                    } else null
                                )
                            }
                        }

                        // Resolve Dialog
                        ResolveDialog(
                            showDialog = showResolveDialog,
                            onDismiss = {
                                showResolveDialog = false
                                selectedHistory = null
                            },
                            onConfirm = { comment ->
                                selectedHistory?.let { history ->
                                    viewModel.resolveHistory(history.id, comment)
                                }
                                showResolveDialog = false
                                selectedHistory = null
                            },
                            serverName = selectedHistory?.serverName ?: ""
                        )
                    }
                }
            }

            // Error message
            error?.let {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}