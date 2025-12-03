package com.uniguard.netguard_app.presentation.ui.screens.super_admin.groups.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.domain.model.Group
import com.uniguard.netguard_app.presentation.ui.components.TextFieldWithLabel
import netguardapp.composeapp.generated.resources.Res
import netguardapp.composeapp.generated.resources.groups_cancel
import netguardapp.composeapp.generated.resources.groups_create
import netguardapp.composeapp.generated.resources.groups_update
import org.jetbrains.compose.resources.stringResource


@Composable
fun GroupEditorDialog(
    title: String,
    group: Group? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Int) -> Unit
) {
    var name by remember { mutableStateOf(group?.name ?: "") }
    var description by remember { mutableStateOf(group?.description ?: "") }
    var maxMembers by remember { mutableStateOf(group?.maxMembers?.toString() ?: "10") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var maxMembersError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        nameError = if (name.isBlank()) "Name cannot be empty" else null

        val maxValue = maxMembers.toIntOrNull()
        maxMembersError = when {
            maxMembers.isBlank() -> "Max members is required"
            maxValue == null -> "Invalid number"
            maxValue < 1 -> "Minimum value is 1"
            else -> null
        }

        return nameError == null && maxMembersError == null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Name
                TextFieldWithLabel(
                    label = "Name",
                    value = name,
                    onValueChange = {
                        name = it
                        validate()
                    }
                )
                if (nameError != null) {
                    Text(
                        text = nameError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Description
                TextFieldWithLabel(
                    label = "Description",
                    value = description,
                    onValueChange = { description = it }
                )

                // Max Members
                TextFieldWithLabel(
                    label = "Max Members",
                    value = maxMembers,
                    onValueChange = {
                        maxMembers = it
                        validate()
                    }
                )
                if (maxMembersError != null) {
                    Text(
                        text = maxMembersError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (validate()) {
                        onConfirm(
                            name,
                            description.ifBlank { null },
                            maxMembers.toInt()
                        )
                    }
                },
                enabled = validate()  // Disable button when invalid
            ) {
                Text(
                    stringResource(
                        if (group == null) Res.string.groups_create
                        else Res.string.groups_update
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.groups_cancel))
            }
        }
    )
}