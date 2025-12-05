package com.uniguard.netguard_app.presentation.ui.screens.user.dashboard.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.domain.model.History
import com.uniguard.netguard_app.utils.formatRelativeTime
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardRecentIncidentsSection(
    incidents: List<History>,
) {

    Text(
        stringResource(Res.string.dashboard_recent_incidents),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (incidents.isEmpty()) {
        Card {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(Icons.Default.CheckCircle, null)

                Spacer(Modifier.width(12.dp))

                Text(stringResource(Res.string.dashboard_all_good))
            }
        }

    } else {

        incidents.take(3).forEach { incident ->

            DashboardIncidentCard(
                serverName = incident.serverName,
                status = incident.status,
                timestamp = formatRelativeTime(incident.resolvedAt ?: incident.timestamp),
                url = incident.url
            )

            Spacer(Modifier.height(6.dp))
        }
    }
}
