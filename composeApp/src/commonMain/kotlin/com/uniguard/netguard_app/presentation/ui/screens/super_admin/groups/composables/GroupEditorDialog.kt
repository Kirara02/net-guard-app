package com.uniguard.netguard_app.presentation.ui.screens.super_admin.groups.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import netguardapp.composeapp.generated.resources.*
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


                // --- NAME ---
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validate()
                    },
                    label = { Text(stringResource(Res.string.groups_name)) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                // --- DESCRIPTION ---
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = { Text(stringResource(Res.string.groups_description)) },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                // --- MAX MEMBERS ---
                OutlinedTextField(
                    value = maxMembers,
                    onValueChange = {
                        maxMembers = it
                        validate()
                    },
                    label = { Text(stringResource(Res.string.groups_max_members)) },
                    isError = maxMembersError != null,
                    supportingText = maxMembersError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )

            }
        },
        confirmButton = {
            val isValid = validate()

            TextButton(
                onClick = {
                    if (isValid) {
                        onConfirm(
                            name.trim(),
                            description.ifBlank { null },
                            maxMembers.toInt()
                        )
                    }
                },
                enabled = isValid
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