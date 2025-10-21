package com.uniguard.netguard_app.core

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

actual class PermissionManager(private val activity: Activity) {

    actual fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            println("✅ Notification permission not required for Android < 13")
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS

        when {
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED -> {
                println("✅ Notification permission already granted.")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) -> {
                println("ℹ️ User previously denied notification permission (show rationale).")
                // Kamu bisa tampilkan UI edukatif di sini kalau mau
                ActivityCompat.requestPermissions(activity, arrayOf(permission), REQUEST_CODE_NOTIFICATIONS)
            }

            else -> {
                println("🔔 Requesting notification permission for the first time.")
                ActivityCompat.requestPermissions(activity, arrayOf(permission), REQUEST_CODE_NOTIFICATIONS)
            }
        }
    }

    companion object {
        const val REQUEST_CODE_NOTIFICATIONS = 1001
    }
}
