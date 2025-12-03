package com.uniguard.netguard_app.presentation.ui.screens.user.servers.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.domain.model.Server
import com.uniguard.netguard_app.utils.formatRelativeTime
import com.uniguard.netguardapp.db.ServerStatusEntity
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ServerCard(
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
        else -> Icons.AutoMirrored.Filled.Help
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
                                contentDescription = stringResource(Res.string.server_management_edit),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.server_management_delete_action),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}