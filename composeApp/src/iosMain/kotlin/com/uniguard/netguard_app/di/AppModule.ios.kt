package com.uniguard.netguard_app.di

import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguard_app.data.local.preferences.AuthPreferences

actual fun getDatabaseProvider(): DatabaseProvider {
    return DatabaseProvider()
}

actual fun getAuthPreferences(): AuthPreferences {
    return AuthPreferences()
}