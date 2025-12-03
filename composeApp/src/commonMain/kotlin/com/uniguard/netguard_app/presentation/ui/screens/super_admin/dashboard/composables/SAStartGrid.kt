package com.uniguard.netguard_app.presentation.ui.screens.super_admin.dashboard.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import com.uniguard.netguard_app.domain.model.OverviewDashboard
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun SAStatsGrid(
    overview: OverviewDashboard,
    onUsersClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onServersClick: () -> Unit,
) {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ===== ROW 1 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            SADashboardCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.sa_dashboard_groups),
                value = overview.totalGroups.toString(),
                icon = Icons.Default.Groups,
                onClick = onGroupsClick
            )

            SADashboardCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.sa_dashboard_users),
                value = overview.totalUsers.toString(),
                icon = Icons.Default.Person,
                onClick = onUsersClick
            )
        }

        // ===== ROW 2 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            SADashboardCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.sa_dashboard_servers),
                value = overview.totalServers.toString(),
                icon = Icons.Default.Storage,
                onClick = onServersClick
            )

            SADashboardCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.sa_dashboard_active_sessions),
                value = overview.activeSessions.toString(),
                icon = Icons.Default.Wifi
            )
        }
    }
}

