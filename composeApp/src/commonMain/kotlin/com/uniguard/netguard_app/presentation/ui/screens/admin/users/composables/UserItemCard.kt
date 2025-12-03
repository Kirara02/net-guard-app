package com.uniguard.netguard_app.presentation.ui.screens.admin.users.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.uniguard.netguard_app.domain.model.User


@Composable
fun UserItemCard(
    user: User,
    isCurrentUser: Boolean,
    isSuperAdmin: Boolean,
    onEdit: (User) -> Unit,
    onDelete: (User) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Avatar
            AsyncImage(
                model = "https://ui-avatars.com/api/?name=${user.name}",
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.name, style = MaterialTheme.typography.titleMedium)

                    if (isCurrentUser) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "(You)",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text(
                    user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                RoleBadge(user.role)

                if (isSuperAdmin && user.group != null) {
                    Spacer(Modifier.height(4.dp))
                    GroupBadge(user.group.name)
                }
            }

            // Menu
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = {
                            menuExpanded = false
                            onEdit(user)
                        }
                    )

                    if (!isCurrentUser) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = { Icon(Icons.Default.PersonRemove, null) },
                            onClick = {
                                menuExpanded = false
                                onDelete(user)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun RoleBadge(role: String) {
    val color = if (role.lowercase() == "admin")
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = role.replaceFirstChar { it.uppercase() },
            color = color,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun GroupBadge(groupName: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = groupName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}