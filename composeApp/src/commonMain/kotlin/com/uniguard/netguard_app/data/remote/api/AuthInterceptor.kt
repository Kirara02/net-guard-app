package com.uniguard.netguard_app.data.remote.api

import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.di.getKoinInstance
import com.uniguard.netguard_app.firebase.FirebaseTopicManager
import com.uniguard.netguard_app.worker.ServerMonitoringScheduler
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AuthInterceptor(
    private val appPreferences: AppPreferences
) {

    private val _unauthorizedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorizedEvent = _unauthorizedEvent.asSharedFlow()

    val plugin = createClientPlugin("AuthInterceptor") {
        on(Send) { request ->
            // Add authorization header if token exists
            val token = appPreferences.getToken()
            if (token != null && !request.headers.contains("Authorization")) {
                request.headers["Authorization"] = "Bearer $token"
            }

            // Proceed with the request
            val call = proceed(request)

            // Handle 401 responses
            if (call.response.status == HttpStatusCode.Unauthorized) {
                // Check if this is not an auth endpoint (login/register)
                val isAuthEndpoint = call.request.url.encodedPath.let { path ->
                    path.contains("/auth/login") || path.contains("/auth/register")
                }

                // Only clear auth data if it's not an auth endpoint
                if (!isAuthEndpoint) {
                    appPreferences.clearAll()
                    getKoinInstance<ServerMonitoringScheduler>().cancelServerMonitoring()
                    // Unsubscribe from Firebase topics
                    FirebaseTopicManager.unsubscribe("serverdown")
                    // Emit event to trigger navigation to login
                    _unauthorizedEvent.tryEmit(Unit)
                }
            }

            call
        }
    }
}