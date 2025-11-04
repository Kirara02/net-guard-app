package com.uniguard.netguard_app.di


import android.app.Application
import android.content.Context
import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.localization.Localization
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
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

actual val appPreferencesModule = module {
    single { AppPreferences(androidContext()) }
}
actual val localeModule = module {
    single<Localization> { Localization() }
}

actual fun initKoinIfNeeded(context: Any?) {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            androidContext(context as Context)
            modules(
                appModule,
                databaseProviderModule,
                appPreferencesModule,
                localeModule
            )
        }
    }
}