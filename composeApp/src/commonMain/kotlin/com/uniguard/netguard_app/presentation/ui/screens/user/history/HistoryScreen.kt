package com.uniguard.netguard_app.presentation.ui.screens.user.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.History
import com.uniguard.netguard_app.domain.model.Server
import com.uniguard.netguard_app.presentation.ui.components.IncidentHistoryCard
import com.uniguard.netguard_app.presentation.ui.screens.super_admin.groups.LoadingColumn
import com.uniguard.netguard_app.presentation.viewmodel.user.HistoryViewModel
import com.uniguard.netguard_app.presentation.viewmodel.user.ServerViewModel
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
    serverViewModel: ServerViewModel = rememberKoinViewModel<ServerViewModel>(),
    onNavigateBack: () -> Unit
) {
    val historiesState by viewModel.histories.collectAsState()
    val servers by serverViewModel.servers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showResolveDialog by remember { mutableStateOf(false) }
    var selectedHistory by remember { mutableStateOf<History?>(null) }

    var selectedServerFilter by remember { mutableStateOf("ALL") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    LaunchedEffect(Unit) {
        viewModel.loadHistories()
        serverViewModel.loadServers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                }
            )
        }
    ) { padding ->


        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (isLoading) {
                LoadingSection()
            } else {
                when (historiesState) {

                    is ApiResult.Success -> {
                        val histories = (historiesState as ApiResult.Success).data

                        val filteredHistories = histories
                            .filter { history ->
                                val serverMatch = when (selectedServerFilter) {
                                    "ALL" -> true
                                    else -> history.serverName == selectedServerFilter
                                }

                                val statusMatch = when (selectedStatusFilter) {
                                    "ALL" -> true
                                    else -> history.status == selectedStatusFilter
                                }

                                serverMatch && statusMatch
                            }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            ServerFilterDropdown(
                                servers = servers,
                                selectedServer = selectedServerFilter,
                                onServerChange = {
                                    selectedServerFilter = it
                                }
                            )

                            StatusFilterDropdown(
                                statuses = listOf("ALL", "RESOLVED", "DOWN"),
                                selectedStatus = selectedStatusFilter,
                                onStatusChange = {
                                    selectedStatusFilter = it
                                }
                            )

                            Spacer(Modifier.height(12.dp))

                            if (filteredHistories.isEmpty()) {
                                EmptyState()
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(filteredHistories, key = { it.id }) { history ->
                                        IncidentHistoryCard(
                                            serverName = history.serverName,
                                            status = history.status,
                                            timestamp = formatRelativeTime(history.resolvedAt ?: history.timestamp),
                                            duration = null, // TODO: Calculate duration if needed
                                            reportedBy = history.createdBy,
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
                            }

                        }
                    }

                    is ApiResult.Error -> {
                        ErrorSection((historiesState as ApiResult.Error).message)
                    }

                    ApiResult.Loading -> LoadingColumn()
                    ApiResult.Initial -> {}
                }
            }

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerFilterDropdown(
    servers: List<Server>,
    selectedServer: String,
    onServerChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val isEmpty = servers.isEmpty()
    val groupNames = if (isEmpty) listOf("No Servers — Create One First")
    else listOf("ALL") + servers.map { it.name }

    ExposedDropdownMenuBox(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        expanded = expanded,
        onExpandedChange = {
            // Hanya bisa expand kalau ada group
            if (!isEmpty) expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = if (isEmpty) "No Servers" else selectedServer,
            readOnly = true,
            onValueChange = {},
            label = { Text("Filter Server") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            enabled = !isEmpty,
            modifier = Modifier
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
        )

        if (!isEmpty) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                groupNames.forEach { groupName ->
                    DropdownMenuItem(
                        text = { Text(groupName) },
                        onClick = {
                            onServerChange(groupName)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterDropdown(
    statuses: List<String>,
    selectedStatus: String,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val isEmpty = statuses.isEmpty()

    ExposedDropdownMenuBox(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        expanded = expanded,
        onExpandedChange = {
            if (!isEmpty) expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = if (isEmpty) "No Status" else selectedStatus,
            readOnly = true,
            onValueChange = {},
            label = { Text("Filter Status") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            enabled = !isEmpty,
            modifier = Modifier
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
        )

        if (!isEmpty) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                statuses.forEach { statusName ->
                    DropdownMenuItem(
                        text = { Text(statusName) },
                        onClick = {
                            onStatusChange(statusName)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingSection() {
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
}

@Composable
private fun EmptyState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No histories found", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ErrorSection(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}