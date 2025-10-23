package com.uniguard.netguard_app.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.Server
import com.uniguard.netguard_app.presentation.ui.components.PrimaryButton
import com.uniguard.netguard_app.presentation.ui.theme.NetGuardTheme
import com.uniguard.netguard_app.presentation.viewmodel.ServerViewModel
import com.uniguard.netguard_app.utils.formatRelativeTime
import com.uniguard.netguardapp.db.ServerStatusEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerManagementScreen(
    viewModel: ServerViewModel = rememberKoinViewModel<ServerViewModel>(),
    onNavigateBack: () -> Unit
) {
    val servers by viewModel.servers.collectAsState()
    val serverStatuses by viewModel.serverStatuses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    NetGuardTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Server Management") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refreshServers() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Server",
                                tint = MaterialTheme.colorScheme.primary
                            )
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
                if (isLoading && servers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Loading servers...")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Stats Header
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Server Overview",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        StatItem(
                                            label = "Total",
                                            value = viewModel.totalServers.toString(),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        StatItem(
                                            label = "Online",
                                            value = serverStatuses.values.count { it.status == "UP" }.toString(),
                                            color = Color(0xFF4CAF50)
                                        )
                                        StatItem(
                                            label = "Offline",
                                            value = serverStatuses.values.count { it.status == "DOWN" }.toString(),
                                            color = Color(0xFFF44336)
                                        )
                                        StatItem(
                                            label = "Unknown",
                                            value = (viewModel.totalServers - serverStatuses.size).toString(),
                                            color = Color(0xFF9E9E9E)
                                        )
                                    }
                                }
                            }
                        }

                        // Server List
                        if (servers.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Dns,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "No servers configured",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Add your first server to start monitoring",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        PrimaryButton(
                                            text = "Add Server",
                                            onClick = { showAddDialog = true },
                                            modifier = Modifier.fillMaxWidth(0.6f)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Group servers by status for better organization
                            val onlineServers = servers.filter { serverStatuses[it.id]?.status == "UP" }
                            val offlineServers = servers.filter { serverStatuses[it.id]?.status == "DOWN" }
                            val unknownServers = servers.filter { serverStatuses[it.id]?.status != "UP" && serverStatuses[it.id]?.status != "DOWN" }

                            // Online Servers Section
                            if (onlineServers.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "🟢 Online Servers (${onlineServers.size})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFF4CAF50),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(onlineServers) { server ->
                                    val serverStatus = serverStatuses[server.id]
                                    ServerCard(
                                        server = server,
                                        serverStatus = serverStatus,
                                        onEdit = {
                                            viewModel.selectServer(server)
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            viewModel.selectServer(server)
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }

                            // Offline Servers Section
                            if (offlineServers.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "🔴 Offline Servers (${offlineServers.size})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFFF44336),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(offlineServers) { server ->
                                    val serverStatus = serverStatuses[server.id]
                                    ServerCard(
                                        server = server,
                                        serverStatus = serverStatus,
                                        onEdit = {
                                            viewModel.selectServer(server)
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            viewModel.selectServer(server)
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }

                            // Unknown Servers Section
                            if (unknownServers.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "⚪ Unmonitored Servers (${unknownServers.size})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFF9E9E9E),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(unknownServers) { server ->
                                    val serverStatus = serverStatuses[server.id]
                                    ServerCard(
                                        server = server,
                                        serverStatus = serverStatus,
                                        onEdit = {
                                            viewModel.selectServer(server)
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            viewModel.selectServer(server)
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }
                        }

                        // Error message
                        error?.let {
                            item {
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
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(onClick = { viewModel.clearError() }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Dismiss",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Loading overlay
                if (isLoading && servers.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Text("Syncing servers...")
                            }
                        }
                    }
                }
            }
        }

        // Add Server Dialog
        if (showAddDialog) {
            ServerDialog(
                title = "Add Server",
                initialName = "",
                initialUrl = "",
                onConfirm = { name, url ->
                    viewModel.addServer(name, url)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }

        // Edit Server Dialog
        if (showEditDialog && selectedServer != null) {
            ServerDialog(
                title = "Edit Server",
                initialName = selectedServer!!.name,
                initialUrl = selectedServer!!.url,
                onConfirm = { name, url ->
                    viewModel.updateServer(selectedServer!!.id, name, url)
                    showEditDialog = false
                    viewModel.selectServer(null)
                },
                onDismiss = {
                    showEditDialog = false
                    viewModel.selectServer(null)
                }
            )
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog && selectedServer != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    viewModel.selectServer(null)
                },
                title = { Text("Delete Server") },
                text = {
                    Text("Are you sure you want to delete '${selectedServer!!.name}'? This action cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteServer(selectedServer!!.id)
                            showDeleteDialog = false
                            viewModel.selectServer(null)
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.selectServer(null)
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ServerCard(
    server: Server,
    serverStatus: ServerStatusEntity?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (serverStatus?.status) {
        "UP" -> Color(0xFF4CAF50)
        "DOWN" -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }

    val statusIcon = when (serverStatus?.status) {
        "UP" -> Icons.Default.CheckCircle
        "DOWN" -> Icons.Default.Error
        else -> Icons.Default.Help
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (serverStatus?.status) {
                "UP" -> MaterialTheme.colorScheme.surface
                "DOWN" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Server Header with Status Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status Icon
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        statusIcon,
                        contentDescription = serverStatus?.status ?: "Unknown",
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Server Name and Status Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = server.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Status Badge
                        Surface(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = serverStatus?.status ?: "UNKNOWN",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = server.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Status information
                    serverStatus?.let { status ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = formatRelativeTime(status.last_checked),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            status.response_time?.let { responseTime ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = when {
                                            responseTime < 500 -> Color(0xFF4CAF50)
                                            responseTime < 2000 -> Color(0xFFFF9800)
                                            else -> Color(0xFFF44336)
                                        },
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${responseTime}ms",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = when {
                                            responseTime < 500 -> Color(0xFF4CAF50)
                                            responseTime < 2000 -> Color(0xFFFF9800)
                                            else -> Color(0xFFF44336)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Action buttons
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerDialog(
    title: String,
    initialName: String,
    initialUrl: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var url by remember { mutableStateOf(initialUrl) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var urlError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Server Name") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        urlError = null
                    },
                    label = { Text("Server URL") },
                    placeholder = { Text("https://example.com") },
                    isError = urlError != null,
                    supportingText = urlError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Validation
                    var isValid = true

                    if (name.isBlank()) {
                        nameError = "Name is required"
                        isValid = false
                    }

                    if (url.isBlank()) {
                        urlError = "URL is required"
                        isValid = false
                    } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        urlError = "URL must start with http:// or https://"
                        isValid = false
                    }

                    if (isValid) {
                        onConfirm(name.trim(), url.trim())
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}