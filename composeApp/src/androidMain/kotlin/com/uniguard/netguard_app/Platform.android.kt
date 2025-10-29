package com.uniguard.netguard_app

import com.uniguard.netguard_app.di.applicationContext


actual fun getAppInfo(): AppInfo {
    val context = applicationContext
    return try {
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)

        AppInfo(
            name = packageManager.getApplicationLabel(applicationInfo).toString(),
            version = packageInfo.versionName ?: "1.0.0"
        )
    } catch (_: Exception) {
        AppInfo(
            name = "NetGuard",
            version = "1.0.0"
        )
    }
}