package com.uniguard.netguard_app.di

import com.uniguard.netguard_app.data.remote.api.ClientException
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.data.remote.api.ServerException
import com.uniguard.netguard_app.data.remote.api.TokenExpiredException
import com.uniguard.netguard_app.data.repository.AuthRepositoryImpl
import com.uniguard.netguard_app.data.repository.HistoryRepositoryImpl
import com.uniguard.netguard_app.data.repository.ServerRepositoryImpl
import com.uniguard.netguard_app.domain.repository.AuthRepository
import com.uniguard.netguard_app.domain.repository.HistoryRepository
import com.uniguard.netguard_app.domain.repository.ServerRepository
import com.uniguard.netguard_app.presentation.viewmodel.AuthViewModel
import com.uniguard.netguard_app.presentation.viewmodel.DashboardViewModel
import com.uniguard.netguard_app.presentation.viewmodel.ServerViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module


// Dependency injection container
//object AppModule {
//
//    // API
//    val api: NetGuardApi by lazy { NetGuardApi() }
//
//    // Database
//    val databaseProvider: DatabaseProvider by lazy {
//        // This will be platform-specific implementation
//        getDatabaseProvider()
//    }
//
//    // Preferences
//    val authPreferences: AuthPreferences by lazy {
//        // This will be platform-specific implementation
//        getAuthPreferences()
//    }
//
//    // Repositories
//    val authRepository: AuthRepository by lazy {
//        AuthRepositoryImpl(api, authPreferences)
//    }
//
//    val serverRepository: ServerRepository by lazy {
//        ServerRepositoryImpl(api, databaseProvider, authPreferences)
//    }
//
//    val historyRepository: HistoryRepository by lazy {
//        HistoryRepositoryImpl(api, databaseProvider, authPreferences)
//    }
//
//    // ViewModels
//    val authViewModel: AuthViewModel by lazy {
//        AuthViewModel(authRepository)
//    }
//
//    val dashboardViewModel: DashboardViewModel by lazy {
//        DashboardViewModel(serverRepository, historyRepository)
//    }
//
//    val serverViewModel: ServerViewModel by lazy {
//        ServerViewModel(serverRepository)
//    }
//}

fun KoinApplication.init() {
    modules(appModule, databaseProviderModule, authPreferencesModule)
}

val appModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(HttpTimeout) {
                val timeout = 30_000L
                connectTimeoutMillis = timeout
                requestTimeoutMillis = timeout
                socketTimeoutMillis = timeout
            }
            HttpResponseValidator {
                validateResponse { response ->
                    when (response.status.value) {
                        401 -> {
                            val isAuthEndpoint = response.call.request.url.encodedPath.let { path ->
                                path.contains("/auth/login") || path.contains("/auth/register")
                            }
                            if (!isAuthEndpoint) {
                                throw TokenExpiredException("Token expired")
                            }
                        }
                        in 400..499 -> throw ClientException(response, response.status.description)
                        in 500..599 -> throw ServerException(response, response.status.description)
                    }
                }
            }
        }
    }

    single { NetGuardApi(get()) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<HistoryRepository> { HistoryRepositoryImpl(get(), get(), get()) }
    single<ServerRepository> { ServerRepositoryImpl(get(), get(), get()) }

    factory { AuthViewModel(get()) }
    factory { DashboardViewModel(get(), get(),get()) }
    factory { ServerViewModel(get(), get()) }
}

expect val databaseProviderModule: Module
expect val authPreferencesModule: Module