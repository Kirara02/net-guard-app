package com.uniguard.netguard_app.presentation.ui.screens.admin.users.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isProcessing: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete User") },
        text = { Text("Are you sure? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isProcessing
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}