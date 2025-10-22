package com.uniguard.netguard_app.data.remote.api

import com.uniguard.netguard_app.data.local.preferences.AuthPreferences
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AuthInterceptor(
    private val authPreferences: AuthPreferences
) {

    private val _unauthorizedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorizedEvent = _unauthorizedEvent.asSharedFlow()

    val plugin = createClientPlugin("AuthInterceptor") {
        on(Send) { request ->
            // Add authorization header if token exists
            val token = authPreferences.getToken()
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
                    authPreferences.clearAll()
                    // Emit event to trigger navigation to login
                    _unauthorizedEvent.tryEmit(Unit)
                }
            }

            call
        }
    }
}