@file:OptIn(ExperimentalForeignApi::class)

package com.uniguard.netguard_app.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.registerForRemoteNotifications
import platform.UserNotifications.*
import kotlin.coroutines.resume

actual class AppSettings actual constructor(
    private val activity: Any?
) {

    actual fun open(type: SettingsType, asAnotherTask: Boolean) {
        when (type) {
            SettingsType.Notifications -> openNotificationSettings()
            else -> openAppSettings()
        }
    }

    // ✅ Cek izin
    actual suspend fun checkPermission(type: PermissionType): Boolean {
        return when (type) {
            PermissionType.Notification -> checkNotificationPermission()
            else -> true // iOS tidak punya konsep battery optimization
        }
    }

    // ✅ Request izin (sama seperti PermissionManager kamu)
    actual suspend fun requestPermission(type: PermissionType): Boolean {
        return when (type) {
            PermissionType.Notification -> requestNotificationPermission()
            else -> true
        }
    }

    // ✅ Cek status notifikasi
    private suspend fun checkNotificationPermission(): Boolean =
        suspendCancellableCoroutine { cont ->
            val center = UNUserNotificationCenter.currentNotificationCenter()
            center.getNotificationSettingsWithCompletionHandler { settings ->
                val granted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
                cont.resume(granted)
            }
        }

    // ✅ Request notifikasi (versi lengkap seperti PermissionManager kamu)
    private suspend fun requestNotificationPermission(): Boolean =
        suspendCancellableCoroutine { cont ->
            val center = UNUserNotificationCenter.currentNotificationCenter()

            center.getNotificationSettingsWithCompletionHandler { settings ->
                val currentStatus = settings?.authorizationStatus
                when (currentStatus) {
                    UNAuthorizationStatusAuthorized -> {
                        println("✅ Notification permission already granted.")
                        registerForRemoteNotifications()
                        cont.resume(true)
                    }

                    UNAuthorizationStatusDenied -> {
                        println("🚫 Notification permission previously denied.")
                        cont.resume(false)
                    }

                    UNAuthorizationStatusNotDetermined -> {
                        // 🔔 Baru minta izin kalau belum pernah diminta
                        val options = UNAuthorizationOptionAlert or
                                UNAuthorizationOptionSound or
                                UNAuthorizationOptionBadge

                        center.requestAuthorizationWithOptions(options) { granted, error ->
                            if (error != null) {
                                println("❌ Notification permission error: $error")
                                cont.resume(false)
                            } else {
                                println("🔔 Notification permission granted: $granted")
                                if (granted) registerForRemoteNotifications()
                                cont.resume(granted)
                            }
                        }
                    }

                    else -> {
                        println("⚠️ Unknown notification permission state.")
                        cont.resume(false)
                    }
                }
            }
        }

    private fun registerForRemoteNotifications() {
        UIApplication.sharedApplication.registerForRemoteNotifications()
        println("📡 Registered for remote notifications (APNs)")
    }

    private fun openNotificationSettings() {
        val url = NSURL(string = "App-Prefs:root=NOTIFICATIONS_ID")
        val app = UIApplication.sharedApplication
        if (app.canOpenURL(url)) {
            app.openURL(url)
        }
    }

    private fun openAppSettings() {
        // Kotlin/Native belum expose UIApplication.openSettingsURLString,
        // jadi kita pakai literal resmi Apple
        val url = NSURL(string = "app-settings:")
        val app = UIApplication.sharedApplication
        if (app.canOpenURL(url)) {
            app.openURL(url)
        }
    }


    private fun isAtLeastIOS(major: Int, minor: Int = 0): Boolean {
        val version = cValue<NSOperatingSystemVersion> {
            this.majorVersion = major.toLong()
            this.minorVersion = minor.toLong()
            this.patchVersion = 0
        }
        return NSProcessInfo.processInfo.isOperatingSystemAtLeastVersion(version)
    }
}

@Composable
actual fun rememberAppSettings(): AppSettings {
    return remember { AppSettings(null) }
}
