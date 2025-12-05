package com.uniguard.netguard_app.presentation.ui.screens.user.servers.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.domain.model.Group
import com.uniguard.netguard_app.domain.model.GroupInfo
import com.uniguard.netguard_app.domain.model.Server
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerFormDialog(
    title: String,
    server: Server? = null,
    currentUserRole: String,
    groups: List<GroupInfo>,
    preSelectedGroup: GroupInfo? = null,
    onConfirm: (name: String, url: String, groupId: String?) -> Unit,
    onDismiss: () -> Unit
) {

    var name by remember { mutableStateOf(server?.name ?: "") }
    var url by remember { mutableStateOf(server?.url ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var urlError by remember { mutableStateOf<String?>(null) }

    val isSuperAdmin = currentUserRole.equals("SUPER_ADMIN", ignoreCase = true)

    val initialGroup = remember(server, preSelectedGroup) {
        preSelectedGroup ?: server?.group
    }
    var selectedGroupId by remember(server) {
        mutableStateOf(initialGroup?.id ?: "")
    }
    var selectedGroupName by remember(server) {
        mutableStateOf(initialGroup?.name ?: "No Group")
    }

    var showGroupDropdown by remember { mutableStateOf(false) }

    val isLockedGroup = preSelectedGroup != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ✅ GROUP DROPDOWN (SUPER ADMIN ONLY)
                if (isSuperAdmin) {

                    ExposedDropdownMenuBox(
                        expanded = showGroupDropdown,
                        onExpandedChange = {
                            if (!isLockedGroup) {
                                showGroupDropdown = !showGroupDropdown
                            }
                        }
                    ) {

                        OutlinedTextField(
                            value = selectedGroupName,
                            onValueChange = {},
                            readOnly = isLockedGroup,
                            enabled = !isLockedGroup,
                            label = { Text("Group") },
                            trailingIcon = {
                                if (!isLockedGroup) {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = showGroupDropdown
                                    )
                                }
                            },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = showGroupDropdown,
                            onDismissRequest = {
                                showGroupDropdown = false
                            }
                        ) {

                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(Res.string.sa_dashboard_no_group))
                                },
                                onClick = {
                                    selectedGroupId = ""
                                    selectedGroupName = "No Group"
                                    showGroupDropdown = false
                                }
                            )

                            // ---- GROUP LIST ----
                            groups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name) },
                                    onClick = {
                                        selectedGroupId = group.id
                                        selectedGroupName = group.name
                                        showGroupDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = {
                        Text(stringResource(Res.string.server_management_name))
                    },
                    isError = nameError != null,
                    supportingText = nameError?.let {
                        { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        urlError = null
                    },
                    label = {
                        Text(stringResource(Res.string.server_management_url))
                    },
                    placeholder = {
                        Text(
                            stringResource(
                                Res.string.server_management_url_placeholder
                            )
                        )
                    },
                    isError = urlError != null,
                    supportingText = urlError?.let {
                        { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    )
                )

            }
        },

        confirmButton = {

            val isValid =
                name.isNotBlank()
                        && url.isNotBlank()
                        && (!isSuperAdmin || selectedGroupId.isNotBlank())

            TextButton(
                enabled = isValid,
                onClick = {

                    var valid = true

                    if (name.isBlank()) {
                        nameError = "Name is required"
                        valid = false
                    }

                    if (url.isBlank()) {
                        urlError = "URL is required"
                        valid = false
                    } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        urlError = "URL must start with http:// or https://"
                        valid = false
                    }

                    if (!valid) return@TextButton

                    val finalGroupId =
                        if (isSuperAdmin && selectedGroupId.isNotBlank())
                            selectedGroupId
                        else null

                    onConfirm(
                        name.trim(),
                        url.trim(),
                        finalGroupId
                    )
                }
            ) {
                Text(stringResource(Res.string.server_management_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.server_management_cancel))
            }
        }
    )
}
