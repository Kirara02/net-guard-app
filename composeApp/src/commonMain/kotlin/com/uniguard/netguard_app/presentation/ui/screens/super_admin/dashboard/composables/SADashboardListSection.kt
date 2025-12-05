package com.uniguard.netguard_app.presentation.ui.screens.super_admin.dashboard.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.domain.model.DashboardRowData
import com.uniguard.netguard_app.presentation.ui.components.GroupBadge
import com.uniguard.netguard_app.presentation.ui.components.InitialAvatar
import com.uniguard.netguard_app.presentation.ui.components.RoleBadge
import com.uniguard.netguard_app.utils.formatRelativeTime
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> SADashboardListSection(
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

                        SADashboardListItem(
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
private fun SADashboardListItem(
    data: DashboardRowData
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ===== AVATAR =====
        InitialAvatar(
            name = data.title
        )

        Spacer(Modifier.width(12.dp))

        // ===== CONTENT =====
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = data.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ---------- CHIP ROW ----------
            if (data.badge != null || data.subtitle != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    data.subtitle?.let { GroupBadge(it) }
                    data.badge?.let { RoleBadge(it) }
                }
            }

            // ---------- CREATED AT ----------
            data.createdAt?.let { timestamp ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )

                    Text(
                        text = formatRelativeTime(timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
