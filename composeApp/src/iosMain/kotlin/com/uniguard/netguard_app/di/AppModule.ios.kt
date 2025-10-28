package com.uniguard.netguard_app.di

import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.localization.Localization
import org.koin.dsl.module

actual val databaseProviderModule = module {
    single { DatabaseProvider() }
}

actual val appPreferencesModule = module {
    single { AppPreferences() }
}

actual val localeModule = module {
    single<Localization> { Localization() }
}