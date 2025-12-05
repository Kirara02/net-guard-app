package com.uniguard.netguard_app.presentation.ui.screens.super_admin.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.DashboardRowData
import com.uniguard.netguard_app.presentation.ui.screens.super_admin.dashboard.composables.SADashboardCard
import com.uniguard.netguard_app.presentation.ui.screens.super_admin.dashboard.composables.SADashboardListSection
import com.uniguard.netguard_app.presentation.ui.screens.super_admin.dashboard.composables.SAStatsGrid
import com.uniguard.netguard_app.presentation.viewmodel.super_admin.SADashboardViewModel
import com.uniguard.netguard_app.presentation.viewmodel.user.AuthViewModel
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SADashboardScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: SADashboardViewModel = rememberKoinViewModel(),
    onNavigateToUserList: () -> Unit,
    onNavigateToGroupList: () -> Unit,
    onNavigateToServerList: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val dashboard by dashboardViewModel.dashboard.collectAsState()
    val isLoading by dashboardViewModel.isLoading.collectAsState()
    val error by dashboardViewModel.error.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dashboardViewModel.loadDashboard()
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
                            stringResource(Res.string.sa_dashboard_title),
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
    ) { padding ->

        when {

            isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(text = error ?: "Unexpected error")

                    Spacer(Modifier.height(12.dp))

                    Button(onClick = dashboardViewModel::loadDashboard) {
                        Text(stringResource(Res.string.sa_dashboard_retry))
                    }
                }
            }

            dashboard != null -> {

                val data = dashboard!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // ===== KPI GRID =====
                    item {

                        SAStatsGrid(
                            overview = data.overview,
                            onUsersClick = onNavigateToUserList,
                            onGroupsClick = onNavigateToGroupList,
                            onServersClick = onNavigateToServerList
                        )
                    }

                    // ===== INCIDENT SUMMARY =====
                    item {
                        SADashboardCard(
                            title = stringResource(Res.string.sa_dashboard_incidents),
                            value = stringResource(
                                Res.string.sa_dashboard_incident_status,
                                data.incidents.unresolved,
                                data.incidents.total
                            ),
                            description = stringResource(
                                Res.string.sa_dashboard_avg_resolution,
                                data.incidents.avgResolutionTime
                            ),
                            icon = Icons.Default.Warning,
                            valueTextStyle = MaterialTheme.typography.titleMedium
                        )
                    }

                    item {
                        SADashboardCard(
                            title = stringResource(Res.string.sa_dashboard_settings),
                            value = stringResource(Res.string.sa_dashboard_settings_description),
                            icon = Icons.Default.Settings,
                            valueTextStyle = MaterialTheme.typography.bodySmall,
                            onClick = onNavigateToSettings
                        )
                    }

                    // ===== RECENT SERVERS =====
                    item {
                        SADashboardListSection(
                            title = stringResource(Res.string.sa_dashboard_recent_servers),
                            items = data.servers.recent,
                            maxItems = 5,
                            showViewAll = true,
                            onViewAll = onNavigateToServerList,
                            emptyText = stringResource(Res.string.sa_dashboard_no_servers)
                        ) { server ->

                            DashboardRowData(
                                title = server.name,
                                subtitle = server.groupName,
                                createdAt = server.createdAt
                            )
                        }
                    }

                    // ===== RECENT USERS =====
                    item {
                        val noGroupLabel = stringResource(Res.string.sa_dashboard_no_group)

                        SADashboardListSection(
                            title = stringResource(Res.string.sa_dashboard_recent_users),
                            items = data.users.recent,
                            maxItems = 5,
                            showViewAll = true,
                            onViewAll = onNavigateToUserList,
                            emptyText = stringResource(Res.string.sa_dashboard_no_users)
                        ) { user ->

                            DashboardRowData(
                                title = user.name,
                                subtitle = user.groupName,
                                badge = user.role,
                                createdAt = user.createdAt
                            )
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
                        },
                    )
                    {
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
}




