package com.uniguard.netguard_app.presentation.ui.screens.admin.users.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.domain.model.Group
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.domain.model.UserRole

@Composable
fun UserList(
    users: List<User>,
    currentUser: User?,
    selectedRole: String,
    selectedGroup: String,
    groups: List<Group>,
    onRoleChange: (String) -> Unit,
    onGroupChange: (String) -> Unit,
    onEdit: (User) -> Unit,
    onDelete: (User) -> Unit
) {
    val isSuperAdmin = currentUser?.userRole == UserRole.SUPER_ADMIN

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // FILTER SECTION (selalu tampil)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            if (isSuperAdmin) {
                GroupFilterDropdown(
                    groups = groups,
                    selectedGroup = selectedGroup,
                    onGroupChange = onGroupChange
                )
                Spacer(Modifier.height(12.dp))
            }

            RoleFilterDropdown(
                selectedRole = selectedRole,
                onRoleChange = onRoleChange,
                isSuperAdmin = isSuperAdmin
            )
        }

        // CONTENT SECTION
        if (users.isEmpty()) {
            EmptyUserSection()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users) { user ->
                    UserItemCard(
                        user = user,
                        isCurrentUser = currentUser?.id == user.id,
                        isSuperAdmin = isSuperAdmin,
                        onEdit = onEdit,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupFilterDropdown(
    groups: List<Group>,
    selectedGroup: String,
    onGroupChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val isEmpty = groups.isEmpty()
    val groupNames = if (isEmpty) listOf("No Groups — Create One First")
    else listOf("ALL") + groups.map { it.name }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            // Hanya bisa expand kalau ada group
            if (!isEmpty) expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = if (isEmpty) "No Groups" else selectedGroup,
            readOnly = true,
            onValueChange = {},
            label = { Text("Filter Group") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            enabled = !isEmpty, // ❗ Tidak bisa klik kalau tidak ada group
            modifier = Modifier
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
        )

        // Jika tidak ada grup → tidak tampilkan menu
        if (!isEmpty) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                groupNames.forEach { groupName ->
                    DropdownMenuItem(
                        text = { Text(groupName) },
                        onClick = {
                            onGroupChange(groupName)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleFilterDropdown(
    selectedRole: String,
    onRoleChange: (String) -> Unit,
    isSuperAdmin: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    val options = remember(isSuperAdmin) {
        if (isSuperAdmin) {
            listOf("ALL", "USER", "ADMIN", "SUPER_ADMIN")
        } else {
            listOf("ALL", "USER", "ADMIN")
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedRole,
            readOnly = true,
            onValueChange = {},
            label = { Text("Filter Role") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.replace("_", " ")) },
                    onClick = {
                        onRoleChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}