package com.uniguard.netguard_app.core

import androidx.compose.runtime.Composable

enum class SettingsType {
    Accessibility,
    Alarm,
    AppLocale,
    BatteryOptimization,
    Bluetooth,
    DataRoaming,
    Date,
    Developer,
    Device,
    Display,
    General,
    Hotspot,
    Location,
    Notifications,
    Security,
    Sound,
    VPN,
    WiFi,
    AppDetails
}

enum class PermissionType {
    Location,
    Notification,
    BatteryOptimization,
    Overlay,
    PostNotifications,
}

expect class AppSettings(activity: Any?) {
    fun open(type: SettingsType, asAnotherTask: Boolean)
    suspend fun checkPermission(type: PermissionType): Boolean
    suspend fun requestPermission(type: PermissionType): Boolean
}

@Composable
expect fun rememberAppSettings(): AppSettings
