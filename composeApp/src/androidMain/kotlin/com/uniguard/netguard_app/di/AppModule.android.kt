package com.uniguard.netguard_app.di

import android.app.Application
import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguard_app.data.local.preferences.AuthPreferences

// Global application context
lateinit var applicationContext: Application
    private set

// Initialize this in Application class
fun initializeAppContext(context: Application) {
    applicationContext = context
}

actual fun getDatabaseProvider(): DatabaseProvider {
    return DatabaseProvider(applicationContext)
}

actual fun getAuthPreferences(): AuthPreferences {
    return AuthPreferences(applicationContext)
}