package com.uniguard.netguard_app

import platform.Foundation.NSBundle


actual fun getAppInfo(): AppInfo {
    return try {
        val bundle = NSBundle.mainBundle
        val displayName = bundle.objectForInfoDictionaryKey("CFBundleDisplayName") as? String
        val bundleName = bundle.objectForInfoDictionaryKey("CFBundleName") as? String
        val version = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String

        AppInfo(
            name = displayName ?: bundleName ?: "NetGuard",
            version = version ?: "1.0.0"
        )
    } catch (_: Exception) {
        AppInfo(
            name = "NetGuard",
            version = "1.0.0"
        )
    }
}