package com.uniguard.netguard_app.di

import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.localization.Localization
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

actual val databaseProviderModule = module {
    single { DatabaseProvider() }
}

actual val appPreferencesModule = module {
    single { AppPreferences() }
}

actual val localeModule = module {
    single<Localization> { Localization() }
}

actual fun initKoinIfNeeded(context: Any?) {
    try {
        val koin = KoinPlatformTools.defaultContext().getOrNull()
        if (koin == null) {
            startKoin {
                modules(
                    appModule,
                    databaseProviderModule,
                    appPreferencesModule,
                    localeModule
                )
            }
        }
    } catch (e: Exception) {
        // fallback kalau Koin belum siap
        stopKoin()
        startKoin {
            modules(
                appModule,
                databaseProviderModule,
                appPreferencesModule,
                localeModule
            )
        }
    }
}