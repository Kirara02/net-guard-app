package com.uniguard.netguard_app.data.remote.api

import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.di.getKoinInstance
import com.uniguard.netguard_app.domain.service.UserSessionService
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
            val token = appPreferences.getToken()
            if (token != null) {
                request.headers["Authorization"] = "Bearer $token"
            }

            val call = proceed(request)

            if (call.response.status == HttpStatusCode.Unauthorized) {
                val isAuthEndpoint = call.request.url.encodedPath.contains("/auth/login")
                if (!isAuthEndpoint) {
                    _unauthorizedEvent.tryEmit(Unit)
                }
            }

            call
        }
    }

    fun emitUnauthorized() {
        _unauthorizedEvent.tryEmit(Unit)
    }
}
