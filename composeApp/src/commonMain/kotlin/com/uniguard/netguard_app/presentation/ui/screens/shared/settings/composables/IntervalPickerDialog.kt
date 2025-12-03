package com.uniguard.netguard_app.presentation.ui.screens.shared.settings.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import netguardapp.composeapp.generated.resources.Res
import netguardapp.composeapp.generated.resources.cancel
import netguardapp.composeapp.generated.resources.monitoring_interval
import netguardapp.composeapp.generated.resources.monitoring_interval_minutes
import netguardapp.composeapp.generated.resources.ok
import org.jetbrains.compose.resources.stringResource


@Composable
fun IntervalPickerDialog(
    currentInterval: Long,
    onDismiss: () -> Unit,
    onIntervalSelected: (Long) -> Unit
) {
    val intervals = listOf(15L, 30L, 60L, 120L) // minutes
    var selectedInterval by remember { mutableStateOf(currentInterval) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.monitoring_interval)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                intervals.forEach { interval ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedInterval == interval,
                            onClick = { selectedInterval = interval }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.monitoring_interval_minutes, interval.toInt()))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onIntervalSelected(selectedInterval)
            }) {
                Text(stringResource(Res.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}
