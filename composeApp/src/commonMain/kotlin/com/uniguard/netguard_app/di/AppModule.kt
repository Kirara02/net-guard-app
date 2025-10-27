package com.uniguard.netguard_app.di

import com.uniguard.netguard_app.BuildKonfig
import com.uniguard.netguard_app.data.remote.api.AuthInterceptor
import com.uniguard.netguard_app.data.remote.api.ClientException
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.data.remote.api.ServerException
import com.uniguard.netguard_app.data.remote.api.TokenExpiredException
import com.uniguard.netguard_app.data.repository.AuthRepositoryImpl
import com.uniguard.netguard_app.data.repository.HistoryRepositoryImpl
import com.uniguard.netguard_app.data.repository.ServerRepositoryImpl
import com.uniguard.netguard_app.data.repository.ServerStatusRepositoryImpl
import com.uniguard.netguard_app.domain.repository.AuthRepository
import com.uniguard.netguard_app.domain.repository.HistoryRepository
import com.uniguard.netguard_app.domain.repository.ServerRepository
import com.uniguard.netguard_app.domain.repository.ServerStatusRepository
import com.uniguard.netguard_app.presentation.viewmodel.AuthViewModel
import com.uniguard.netguard_app.presentation.viewmodel.DashboardViewModel
import com.uniguard.netguard_app.presentation.viewmodel.HistoryViewModel
import com.uniguard.netguard_app.presentation.viewmodel.ServerViewModel
import com.uniguard.netguard_app.presentation.viewmodel.SplashViewModel
import com.uniguard.netguard_app.utils.KtorNapierLogger
import com.uniguard.netguard_app.worker.ServerMonitoringScheduler
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatform


fun KoinApplication.init() {
    modules(appModule, databaseProviderModule, authPreferencesModule)
}

inline fun <reified T> getKoinInstance(): T = KoinPlatform.getKoin().get()

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
                logger = KtorNapierLogger()
                level = if(BuildKonfig.DEBUG) LogLevel.ALL else LogLevel.NONE
            }
            install(HttpTimeout) {
                val timeout = 30_000L
                connectTimeoutMillis = timeout
                requestTimeoutMillis = timeout
                socketTimeoutMillis = timeout
            }
            // Install AuthInterceptor for automatic token handling
            install(AuthInterceptor(get()).plugin)
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

    single { AuthInterceptor(get()) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<HistoryRepository> { HistoryRepositoryImpl(get(), get(), get()) }
    single<ServerRepository> { ServerRepositoryImpl(get(), get(), get()) }
    single<ServerStatusRepository> { ServerStatusRepositoryImpl(get()) }

    single { ServerMonitoringScheduler() }

    factory { AuthViewModel(get()) }
    factory { DashboardViewModel(get(), get(), get(), get()) }
    factory { HistoryViewModel(get()) }
    factory { ServerViewModel(get(), get()) }
    factory { SplashViewModel(get(), get()) }
}

expect val databaseProviderModule: Module
expect val authPreferencesModule: Module