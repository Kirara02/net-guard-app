package com.uniguard.netguard_app.presentation.ui.screens.super_admin.groups.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToDrive
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupRemove
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.domain.model.Group
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
@Composable
fun GroupItemCard(
    group: Group,
    onAddUser: (Group) -> Unit,
    onAddServer: (Group) -> Unit,
    onEdit: (Group) -> Unit,
    onDelete: (Group) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ================= HEADER =================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {

                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = group.description ?: "-",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // ===== KEBAB MENU =====
                Box {

                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {

                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.groups_add_users)) },
                            onClick = {
                                showMenu = false
                                onAddUser(group)
                            },
                            leadingIcon = { Icon(Icons.Default.PersonAdd, null) }
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.groups_add_servers)) },
                            onClick = {
                                showMenu = false
                                onAddServer(group)
                            },
                            leadingIcon = { Icon(Icons.Default.Storage, null) }
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.groups_edit)) },
                            onClick = {
                                showMenu = false
                                onEdit(group)
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.groups_delete)) },
                            onClick = {
                                showMenu = false
                                onDelete(group)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.GroupRemove,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            // ================= MEMBERS INFO =================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${group.currentMembers}/${group.maxMembers} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // ================= CREATED BY =================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = stringResource(
                        Res.string.server_management_created_by,
                        group.createdBy
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
