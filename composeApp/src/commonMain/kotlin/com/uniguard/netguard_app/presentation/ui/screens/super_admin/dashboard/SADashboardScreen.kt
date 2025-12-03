package com.uniguard.netguard_app.presentation.ui.screens.super_admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.DashboardRowData
import com.uniguard.netguard_app.presentation.ui.components.showErrorToast
import com.uniguard.netguard_app.presentation.ui.components.showToast
import com.uniguard.netguard_app.presentation.ui.screens.super_admin.dashboard.composables.SADashboardCard
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
    val logoutState by authViewModel.logoutState.collectAsState()
    val dashboard by dashboardViewModel.dashboard.collectAsState()
    val isLoading by dashboardViewModel.isLoading.collectAsState()
    val error by dashboardViewModel.error.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dashboardViewModel.loadDashboard()
    }

    LaunchedEffect(logoutState) {
        when (logoutState) {
            is ApiResult.Success -> {
                showToast("Logged out successfully")
                authViewModel.resetLogoutState()
            }
            is ApiResult.Error -> {
                showErrorToast("Logged out failed: ${(logoutState as ApiResult.Error).message}")
                authViewModel.resetLogoutState()
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
                        DashboardListSection(
                            title = stringResource(Res.string.sa_dashboard_recent_servers),
                            items = data.servers.recent,
                            emptyText = stringResource(Res.string.sa_dashboard_no_servers)
                        ) { server ->

                            DashboardRowData(
                                title = server.name,
                                subtitle = server.groupName
                            )
                        }
                    }

                    // ===== RECENT USERS =====
                    item {
                        val noGroupLabel = stringResource(Res.string.sa_dashboard_no_group)

                        DashboardListSection(
                            title = stringResource(Res.string.sa_dashboard_recent_users),
                            items = data.users.recent,
                            maxItems = 5,
                            showViewAll = true,
                            onViewAll = onNavigateToUserList,
                            emptyText = stringResource(Res.string.sa_dashboard_no_users)
                        ) { user ->

                            DashboardRowData(
                                title = user.name,
                                subtitle = user.groupName ?: noGroupLabel,
                                badge = user.role
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

@Composable
fun <T> DashboardListSection(
    title: String,
    items: List<T>,
    maxItems: Int = 5,
    showViewAll: Boolean = false,
    onViewAll: (() -> Unit)? = null,
    emptyText: String,
    row: (T) -> DashboardRowData
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {

        Column(Modifier.padding(16.dp)) {

            // ===== HEADER =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium
                )

                if (showViewAll && onViewAll != null) {
                    TextButton(onClick = onViewAll) {
                        Text(stringResource(Res.string.sa_dashboard_view_all))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (items.isEmpty()) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = emptyText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            } else {

                items
                    .take(maxItems)
                    .forEachIndexed { index, item ->

                        DashboardListItem(
                            data = row(item)
                        )

                        if (index != minOf(maxItems - 1, items.lastIndex)) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                thickness = 0.5.dp
                            )
                        }
                    }
            }
        }
    }
}


@Composable
fun DashboardListItem(
    data: DashboardRowData
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ===== AVATAR INITIAL =====
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.title
                    .trim()
                    .firstOrNull()
                    ?.uppercase()
                    ?: "?",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        // ===== TEXT AREA =====
        Column(
            modifier = Modifier.weight(1f)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                // NAME
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.width(8.dp))

                // ROLE CHIP
                data.badge?.let { role ->

                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = role.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.height(22.dp)
                    )
                }
            }

            // SUBTITLE / GROUP
            data.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



