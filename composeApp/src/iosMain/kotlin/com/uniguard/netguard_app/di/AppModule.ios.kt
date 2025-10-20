package com.uniguard.netguard_app.di

import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguard_app.data.local.preferences.AuthPreferences
import org.koin.dsl.module

actual val databaseProviderModule = module {
    single { DatabaseProvider() }
}

actual val authPreferencesModule = module {
    single { AuthPreferences() }
}