package com.uniguard.netguard_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.data.local.preferences.AuthPreferences
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.domain.repository.AuthRepository
import com.uniguard.netguard_app.utils.getCurrentTimestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Instant

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading)
    val splashState: StateFlow<SplashState> = _splashState.asStateFlow()

    init {
        checkAuthentication()
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            // Minimum splash screen duration
            val startTime = Instant.parse(getCurrentTimestamp()).toEpochMilliseconds()


            // Check if user has stored token
            val token = authPreferences.getToken()
            if (token.isNullOrEmpty()) {
                // No token stored, go to login
                ensureMinimumSplashTime(startTime)
                _splashState.value = SplashState.NavigateToLogin
                return@launch
            }

            // Validate token with server
            when (val result = authRepository.getCurrentUser()) {
                is ApiResult.Success -> {
                    // Token is valid, go to dashboard
                    ensureMinimumSplashTime(startTime)
                    _splashState.value = SplashState.NavigateToDashboard(result.data)
                }
                is ApiResult.Error -> {
                    // Token is invalid/expired, clear it and go to login
                    authPreferences.clearAll()
                    ensureMinimumSplashTime(startTime)
                    _splashState.value = SplashState.NavigateToLogin
                }
                is ApiResult.Loading -> {
                    // Should not happen for synchronous token check
                }

                else -> {}
            }
        }
    }

    private suspend fun ensureMinimumSplashTime(startTime: Long, minDuration: Long = 2000) {
        val elapsed = Instant.parse(getCurrentTimestamp()).toEpochMilliseconds() - startTime
        if (elapsed < minDuration) {
            delay(minDuration - elapsed)
        }
    }
}

sealed class SplashState {
    data object Loading : SplashState()
    data object NavigateToLogin : SplashState()
    data class NavigateToDashboard(val user: User) : SplashState()
}