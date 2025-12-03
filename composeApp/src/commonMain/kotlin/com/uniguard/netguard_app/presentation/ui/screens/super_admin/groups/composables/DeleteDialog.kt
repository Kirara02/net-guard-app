package com.uniguard.netguard_app.presentation.ui.screens.super_admin.groups.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.uniguard.netguard_app.domain.model.Group
import netguardapp.composeapp.generated.resources.Res
import netguardapp.composeapp.generated.resources.groups_delete
import netguardapp.composeapp.generated.resources.groups_delete_confirm
import org.jetbrains.compose.resources.stringResource


@Composable
fun ConfirmDeleteDialog(
    group: Group,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.groups_delete)) },
        text = {
            Text(stringResource(Res.string.groups_delete_confirm))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}