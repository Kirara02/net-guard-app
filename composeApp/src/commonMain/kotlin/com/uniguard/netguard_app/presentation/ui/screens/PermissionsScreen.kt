package com.uniguard.netguard_app.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.PermissionType
import com.uniguard.netguard_app.core.SettingsType
import com.uniguard.netguard_app.core.rememberAppSettings
import kotlinx.coroutines.launch
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onNavigateBack: () -> Unit
) {
    val appSettings = rememberAppSettings()
    val coroutineScope = rememberCoroutineScope()

    // State untuk status izin
    var batteryGranted by remember { mutableStateOf<Boolean?>(null) }
    var notifGranted by remember { mutableStateOf<Boolean?>(null) }

    // Load status izin saat layar dibuka
    LaunchedEffect(Unit) {
        batteryGranted = appSettings.checkPermission(PermissionType.BatteryOptimization)
        notifGranted = appSettings.checkPermission(PermissionType.Notification)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.app_permissions)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            PermissionItem(
                icon = Icons.Default.BatteryChargingFull,
                title = stringResource(Res.string.permission_battery),
                description = stringResource(Res.string.permission_battery_desc),
                granted = batteryGranted,
                onRequest = {
                    coroutineScope.launch {
                        val granted = appSettings.requestPermission(PermissionType.BatteryOptimization)
                        batteryGranted = granted
                    }
                },
                onOpenSettings = {
                    appSettings.open(SettingsType.BatteryOptimization, false)
                }
            )

            PermissionItem(
                icon = Icons.Default.Notifications,
                title = stringResource(Res.string.permission_notification),
                description = stringResource(Res.string.permission_notification_desc),
                granted = notifGranted,
                onRequest = {
                    coroutineScope.launch {
                        val granted = appSettings.requestPermission(PermissionType.Notification)
                        notifGranted = granted
                    }
                },
                onOpenSettings = {
                    appSettings.open(SettingsType.Notifications, false)
                }
            )
        }
    }
}

@Composable
private fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    granted: Boolean?,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    when (granted) {
                        true -> stringResource(Res.string.permission_status_granted)
                        false -> stringResource(Res.string.permission_status_denied)
                        null -> stringResource(Res.string.permission_status_checking)
                    },
                    color = when (granted) {
                        true -> MaterialTheme.colorScheme.primary
                        false -> MaterialTheme.colorScheme.error
                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onRequest) {
                    Text(stringResource(Res.string.permission_action_request))
                }
                TextButton(onClick = onOpenSettings) {
                    Text(stringResource(Res.string.permission_action_settings))
                }
            }
        }
    }
}