package com.uniguard.netguard_app.presentation.ui.screens.shared.permissions.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import netguardapp.composeapp.generated.resources.Res
import netguardapp.composeapp.generated.resources.permission_action_request
import netguardapp.composeapp.generated.resources.permission_action_settings
import netguardapp.composeapp.generated.resources.permission_status_checking
import netguardapp.composeapp.generated.resources.permission_status_denied
import netguardapp.composeapp.generated.resources.permission_status_granted
import org.jetbrains.compose.resources.stringResource

@Composable
fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    granted: Boolean?,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    when (granted) {
                        true -> stringResource(Res.string.permission_status_granted)
                        false -> stringResource(Res.string.permission_status_denied)
                        null -> stringResource(Res.string.permission_status_checking)
                    },
                    color = when (granted) {
                        true -> MaterialTheme.colorScheme.primary
                        false -> MaterialTheme.colorScheme.error
                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onRequest) {
                    Text(stringResource(Res.string.permission_action_request))
                }
                TextButton(onClick = onOpenSettings) {
                    Text(stringResource(Res.string.permission_action_settings))
                }
            }
        }
    }
}