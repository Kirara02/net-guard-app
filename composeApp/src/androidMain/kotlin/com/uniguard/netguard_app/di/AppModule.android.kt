package com.uniguard.netguard_app.di


import android.app.Application
import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguard_app.data.local.preferences.AuthPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// Global application context
lateinit var applicationContext: Application
    private set

// Initialize this in Application class
fun initializeAppContext(context: Application) {
    applicationContext = context
}

actual val databaseProviderModule = module {
    single { DatabaseProvider(androidContext()) }
}

actual val authPreferencesModule = module {
    single { AuthPreferences(androidContext()) }
}