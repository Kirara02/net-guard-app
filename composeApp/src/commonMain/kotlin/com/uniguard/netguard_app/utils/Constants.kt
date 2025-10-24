package com.uniguard.netguard_app.utils

object Constants {
    // API URLs for different platforms
    const val API_BASE_URL_ANDROID = "http://10.0.2.2:8080/api"  // Android emulator
    const val API_BASE_URL_IOS = "http://127.0.0.1:8080/api"     // iOS simulator

    const val API_BASE_URL = "http://192.168.1.172:8080/api"  // Desktop

    // Database
    const val DATABASE_NAME = "netguard.db"

    // Preferences
    const val AUTH_PREFERENCES_NAME = "auth_prefs"

    // Worker
    const val SERVER_CHECK_INTERVAL_MINUTES = 15L

    // UI
    const val ANIMATION_DURATION_MS = 300
    const val DEBOUNCE_DELAY_MS = 500L

    // Network
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val MAX_RETRIES = 3
}