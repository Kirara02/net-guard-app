package com.uniguard.netguard_app.core

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class AppSettings actual constructor(
    private val activity: Any?
) {
    private val context = activity as Activity?

    actual fun open(type: SettingsType, asAnotherTask: Boolean) {
        when (type) {
            SettingsType.Accessibility -> open(Settings.ACTION_ACCESSIBILITY_SETTINGS, asAnotherTask)
            SettingsType.Alarm -> openAlarmSettings(asAnotherTask)
            SettingsType.AppLocale -> openAppLocaleSettings(asAnotherTask)
            SettingsType.BatteryOptimization -> open(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, asAnotherTask)
            SettingsType.Bluetooth -> open(Settings.ACTION_BLUETOOTH_SETTINGS, asAnotherTask)
            SettingsType.DataRoaming -> open(Settings.ACTION_DATA_ROAMING_SETTINGS, asAnotherTask)
            SettingsType.Date -> open(Settings.ACTION_DATE_SETTINGS, asAnotherTask)
            SettingsType.Developer -> open(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS, asAnotherTask)
            SettingsType.Device -> open(Settings.ACTION_DEVICE_INFO_SETTINGS, asAnotherTask)
            SettingsType.Display -> open(Settings.ACTION_DISPLAY_SETTINGS, asAnotherTask)
            SettingsType.General -> open(Settings.ACTION_SETTINGS, asAnotherTask)
            SettingsType.Hotspot -> openIntent(
                Intent().setClassName("com.android.settings", "com.android.settings.TetherSettings"),
                asAnotherTask
            )
            SettingsType.Location -> open(Settings.ACTION_LOCATION_SOURCE_SETTINGS, asAnotherTask)
            SettingsType.Notifications -> openNotificationSettings(asAnotherTask)
            SettingsType.Security -> open(Settings.ACTION_SECURITY_SETTINGS, asAnotherTask)
            SettingsType.Sound -> open(Settings.ACTION_SOUND_SETTINGS, asAnotherTask)
            SettingsType.VPN -> open(Settings.ACTION_VPN_SETTINGS, asAnotherTask)
            SettingsType.WiFi -> open(Settings.ACTION_WIFI_SETTINGS, asAnotherTask)
            SettingsType.AppDetails -> openAppSettings(asAnotherTask)
        }
    }

    actual suspend fun checkPermission(type: PermissionType): Boolean {
        val ctx = context ?: return false
        return when (type) {
            PermissionType.Location -> {
                ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
            }
            PermissionType.Notification -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                            PackageManager.PERMISSION_GRANTED
                } else true
            }
            PermissionType.Overlay -> Settings.canDrawOverlays(ctx)
            PermissionType.BatteryOptimization -> {
                val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
                pm.isIgnoringBatteryOptimizations(ctx.packageName)
            }
            PermissionType.PostNotifications -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                            PackageManager.PERMISSION_GRANTED
                else true
            }
        }
    }

    // ✅ Minta permission (langsung munculkan dialog sistem)
    actual suspend fun requestPermission(type: PermissionType): Boolean =
        suspendCancellableCoroutine { cont ->
            val ctx = context ?: run {
                cont.resume(false)
                return@suspendCancellableCoroutine
            }

            when (type) {
                PermissionType.Location -> {
                    ActivityCompat.requestPermissions(
                        ctx,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1001
                    )
                    cont.resume(true) // fake resume; idealnya handle callback
                }
                PermissionType.Notification -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(
                            ctx,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            1002
                        )
                    }
                    cont.resume(true)
                }
                PermissionType.Overlay -> {
                    if (!Settings.canDrawOverlays(ctx)) {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        intent.data = "package:${ctx.packageName}".toUri()
                        ctx.startActivity(intent)
                    }
                    cont.resume(true)
                }
                PermissionType.BatteryOptimization -> {
                    val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (!pm.isIgnoringBatteryOptimizations(ctx.packageName)) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = "package:${ctx.packageName}".toUri()
                        ctx.startActivity(intent)
                    }
                    cont.resume(true)
                }
                PermissionType.PostNotifications -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(
                            ctx,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            1003
                        )
                    }
                    cont.resume(true)
                }
            }
        }

    private fun open(action: String, asAnotherTask: Boolean) {
        try {
            val intent = Intent(action)
            if (asAnotherTask) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openIntent(intent: Intent, asAnotherTask: Boolean) {
        try {
            if (asAnotherTask) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun openAlarmSettings(asAnotherTask: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uri = "package:${context?.packageName}".toUri()
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, uri)
            openIntent(intent, asAnotherTask)
        } else {
            openAppSettings(asAnotherTask)
        }
    }

    private fun openNotificationSettings(asAnotherTask: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)
            openIntent(intent, asAnotherTask)
        } else {
            openAppSettings(asAnotherTask)
        }
    }

    private fun openAppLocaleSettings(asAnotherTask: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
            intent.data = "package:${context?.packageName}".toUri()
            openIntent(intent, asAnotherTask)
        } else {
            openAppSettings(asAnotherTask)
        }
    }

    private fun openAppSettings(asAnotherTask: Boolean) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        if (asAnotherTask) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = "package:${context?.packageName}".toUri()
        openIntent(intent, asAnotherTask)
    }


}


@Composable
actual fun rememberAppSettings(): AppSettings {
    val context = LocalContext.current
    val activity = context as? Activity
    return remember(activity) { AppSettings(activity) }
}