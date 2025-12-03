package com.uniguard.netguard_app.presentation.ui.screens.user.servers.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ServerOverviewCard(
    total: Int,
    online: Int,
    offline: Int,
    unknown: Int
) {
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
                text = stringResource(Res.string.server_management_overview),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ServerStatItem(
                    label = stringResource(Res.string.server_management_total),
                    value = total.toString(),
                    color = MaterialTheme.colorScheme.primary
                )

                ServerStatItem(
                    label = stringResource(Res.string.server_management_online),
                    value = online.toString(),
                    color = Color(0xFF4CAF50)
                )

                ServerStatItem(
                    label = stringResource(Res.string.server_management_offline),
                    value = offline.toString(),
                    color = Color(0xFFF44336)
                )

                ServerStatItem(
                    label = stringResource(Res.string.server_management_unknown),
                    value = unknown.toString(),
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}