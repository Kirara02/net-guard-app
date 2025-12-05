package com.uniguard.netguard_app.presentation.ui.screens.user.dashboard.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardQuickActionsSection(
    onServers: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onReport: () -> Unit
) {
    val actions = listOf(
        Triple(stringResource(Res.string.dashboard_servers),
            stringResource(Res.string.dashboard_servers_desc),
            Icons.Default.Dns to onServers),

        Triple(stringResource(Res.string.dashboard_history),
            stringResource(Res.string.dashboard_history_desc),
            Icons.Default.History to onHistory),

        Triple(stringResource(Res.string.settings),
            stringResource(Res.string.settings_desc),
            Icons.Default.Settings to onSettings),

        Triple(stringResource(Res.string.dashboard_report),
            stringResource(Res.string.dashboard_report_desc),
            Icons.Default.Assessment to onReport)
    )

    Column {

        Text(
            stringResource(Res.string.dashboard_quick_actions),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        actions.chunked(2).forEach { row ->

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                row.forEach { (title, subtitle, action) ->
                    DashboardActionCard(
                        title,
                        subtitle,
                        icon = action.first,
                        onClick = action.second,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (row.size == 1) Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
