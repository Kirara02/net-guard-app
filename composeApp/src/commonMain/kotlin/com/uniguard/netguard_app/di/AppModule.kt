package com.uniguard.netguard_app.di

import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguard_app.data.local.preferences.AuthPreferences
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.data.repository.AuthRepositoryImpl
import com.uniguard.netguard_app.data.repository.HistoryRepositoryImpl
import com.uniguard.netguard_app.data.repository.ServerRepositoryImpl
import com.uniguard.netguard_app.domain.repository.AuthRepository
import com.uniguard.netguard_app.domain.repository.HistoryRepository
import com.uniguard.netguard_app.domain.repository.ServerRepository
import com.uniguard.netguard_app.presentation.viewmodel.AuthViewModel
import com.uniguard.netguard_app.presentation.viewmodel.DashboardViewModel
import com.uniguard.netguard_app.presentation.viewmodel.ServerViewModel

// Dependency injection container
object AppModule {

    // API
    val api: NetGuardApi by lazy { NetGuardApi() }

    // Database
    val databaseProvider: DatabaseProvider by lazy {
        // This will be platform-specific implementation
        getDatabaseProvider()
    }

    // Preferences
    val authPreferences: AuthPreferences by lazy {
        // This will be platform-specific implementation
        getAuthPreferences()
    }

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(api, authPreferences)
    }

    val serverRepository: ServerRepository by lazy {
        ServerRepositoryImpl(api, databaseProvider, authPreferences)
    }

    val historyRepository: HistoryRepository by lazy {
        HistoryRepositoryImpl(api, databaseProvider, authPreferences)
    }

    // ViewModels
    val authViewModel: AuthViewModel by lazy {
        AuthViewModel(authRepository)
    }

    val dashboardViewModel: DashboardViewModel by lazy {
        DashboardViewModel(serverRepository, historyRepository)
    }

    val serverViewModel: ServerViewModel by lazy {
        ServerViewModel(serverRepository)
    }
}

// Platform-specific implementations
expect fun getDatabaseProvider(): DatabaseProvider
expect fun getAuthPreferences(): AuthPreferences