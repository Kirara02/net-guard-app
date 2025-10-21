package com.uniguard.netguard_app.core

import platform.UserNotifications.*
import platform.UIKit.UIApplication
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.registerForRemoteNotifications

@OptIn(ExperimentalForeignApi::class)
actual class PermissionManager {

    actual fun requestNotificationPermission() {
        val center = UNUserNotificationCenter.currentNotificationCenter()

        // 🔍 Periksa status permission terlebih dahulu
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val currentSettings = settings ?: run {
                println("⚠️ Unable to get notification settings (null result)")
                return@getNotificationSettingsWithCompletionHandler
            }

            when (currentSettings.authorizationStatus) {
                UNAuthorizationStatusAuthorized -> {
                    println("✅ Notification permission already granted.")
                    registerForRemoteNotifications()
                }

                UNAuthorizationStatusDenied -> {
                    println("🚫 Notification permission previously denied.")
                }

                UNAuthorizationStatusNotDetermined -> {
                    // 🔔 Baru minta izin jika belum pernah diminta
                    center.requestAuthorizationWithOptions(
                        options = ((1uL shl 0) or (1uL shl 1) or (1uL shl 2)), // alert, sound, badge
                        completionHandler = { granted, error ->
                            if (error != null) {
                                println("❌ Notification permission error: $error")
                            } else {
                                println("🔔 Notification permission granted: $granted")
                                if (granted) {
                                    registerForRemoteNotifications()
                                }
                            }
                        }
                    )
                }

                else -> {
                    println("⚠️ Unknown notification permission state.")
                }
            }
        }
    }

    private fun registerForRemoteNotifications() {
        UIApplication.sharedApplication.registerForRemoteNotifications()
        println("📡 Registered for remote notifications (APNs)")
    }
}
