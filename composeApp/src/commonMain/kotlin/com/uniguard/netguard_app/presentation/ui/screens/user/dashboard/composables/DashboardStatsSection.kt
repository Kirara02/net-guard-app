package com.uniguard.netguard_app.presentation.ui.screens.user.dashboard.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardStatsSection(
    total: Int,
    online: Int,
    down: Int,
    incidents: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardStatCard(
                title = stringResource(Res.string.dashboard_total_servers),
                value = total.toString(),
                icon = Icons.Default.Dns,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            DashboardStatCard(
                title = stringResource(Res.string.dashboard_online),
                value = online.toString(),
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardStatCard(
                title = stringResource(Res.string.dashboard_down),
                value = down.toString(),
                icon = Icons.Default.Error,
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )

            DashboardStatCard(
                title = stringResource(Res.string.dashboard_incidents),
                value = incidents.toString(),
                icon = Icons.Default.Warning,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
