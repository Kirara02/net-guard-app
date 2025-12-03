package com.uniguard.netguard_app.presentation.ui.screens.user.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.presentation.ui.components.*
import com.uniguard.netguard_app.presentation.viewmodel.user.AuthViewModel
import com.uniguard.netguard_app.presentation.viewmodel.user.DashboardViewModel
import com.uniguard.netguard_app.utils.formatRelativeTime
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = rememberKoinViewModel<DashboardViewModel>(),
    authViewModel: AuthViewModel,
    onNavigateToServerList: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToUsers: () -> Unit
) {
    val logoutState by authViewModel.logoutState.collectAsState()
    val recentIncidents by viewModel.recentIncidents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val totalUsers by viewModel.totalUsers.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(logoutState) {
        when (logoutState) {
            is ApiResult.Success -> {
                showToast("Logged out successfully")
            }
            is ApiResult.Error -> {
                showErrorToast("Logged out failed: ${(logoutState as ApiResult.Error).message}")
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = "NetGuard Logo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(Res.string.dashboard_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, null)
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.refresh)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Refresh,
                                        null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.loadDashboardData()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.logout_title)) },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                                },
                                onClick = {
                                    showMenu = false
                                    showLogoutDialog = true
                                }
                            )
                        }
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
                            stringResource(Res.string.dashboard_loading),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Welcome Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
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
                                    Icons.Default.WavingHand,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Column {
                                    Text(
                                        stringResource(Res.string.dashboard_welcome_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        stringResource(Res.string.dashboard_welcome_subtitle),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Quick Stats Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EnhancedStatCard(
                                title = stringResource(Res.string.dashboard_total_servers),
                                value = viewModel.totalServers.toString(),
                                icon = Icons.Default.Dns,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            EnhancedStatCard(
                                title = stringResource(Res.string.dashboard_online),
                                value = viewModel.onlineServers.toString(),
                                icon = Icons.Default.CheckCircle,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EnhancedStatCard(
                                title = stringResource(Res.string.dashboard_down),
                                value = viewModel.downServers.toString(),
                                icon = Icons.Default.Error,
                                color = Color(0xFFF44336),
                                modifier = Modifier.weight(1f)
                            )
                            EnhancedStatCard(
                                title = stringResource(Res.string.dashboard_incidents),
                                value = viewModel.downIncidents.toString(),
                                icon = Icons.Default.Warning,
                                color = Color(0xFFFF9800),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Admin-only User Management Card
                    currentUser?.let { user ->
                        if (user.role.lowercase() == "admin") {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToUsers() },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.People,
                                            contentDescription = "Users",
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.dashboard_users),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = stringResource(Res.string.dashboard_users_desc),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = totalUsers.toString(),
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = stringResource(Res.string.dashboard_total_users),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "Navigate to users",
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Quick Actions
                    item {
                        Column {
                            Text(
                                text = stringResource(Res.string.dashboard_quick_actions),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Responsive grid layout for Quick Actions
                            val actionCards = listOf(
                                Triple(stringResource(Res.string.dashboard_servers), stringResource(Res.string.dashboard_servers_desc), Icons.Default.Dns to onNavigateToServerList),
                                Triple(stringResource(Res.string.dashboard_history), stringResource(Res.string.dashboard_history_desc), Icons.Default.History to onNavigateToHistory),
                                Triple(stringResource(Res.string.settings), stringResource(Res.string.settings_desc), Icons.Default.Settings to onNavigateToSettings),
                                Triple(stringResource(Res.string.dashboard_report), stringResource(Res.string.dashboard_report_desc), Icons.Default.Assessment to onNavigateToReport)
                            )

                            // Use responsive columns based on screen width
                            actionCards.chunked(2).forEach { rowCards ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowCards.forEach { (title, subtitle, iconAndAction) ->
                                        val (icon, onClick ) = iconAndAction
                                        ActionCard(
                                            title = title,
                                            subtitle = subtitle,
                                            icon = icon,
                                            onClick = onClick,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    // Fill empty space if odd number of cards in row
                                    if (rowCards.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                if (rowCards != actionCards.chunked(2).last()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }

                    // Recent Incidents
                    item {
                        Text(
                            text = stringResource(Res.string.dashboard_recent_incidents),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (recentIncidents.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = stringResource(Res.string.dashboard_all_good),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(recentIncidents.take(3)) { incident ->
                            EnhancedIncidentCard(
                                serverName = incident.serverName,
                                status = incident.status,
                                timestamp = formatRelativeTime(incident.timestamp),
                                url = incident.url
                            )
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
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(Res.string.logout_title)) },
            text = { Text(stringResource(Res.string.logout_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                    }
                ) {
                    Text(stringResource(Res.string.logout_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}