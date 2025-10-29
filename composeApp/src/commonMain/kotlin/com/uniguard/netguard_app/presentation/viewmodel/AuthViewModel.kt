package com.uniguard.netguard_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.AuthData
import com.uniguard.netguard_app.domain.model.RegisterRequest
import com.uniguard.netguard_app.domain.model.UpdateProfileRequest
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.domain.repository.AuthRepository
import com.uniguard.netguard_app.firebase.FirebaseTopicManager
import com.uniguard.netguard_app.worker.ServerMonitoringScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel(), KoinComponent {

    private val serverMonitoringScheduler: ServerMonitoringScheduler by inject()
    private val appPreferences: AppPreferences by inject()

    private val _loginState = MutableStateFlow<ApiResult<AuthData>>(ApiResult.Initial)
    val loginState: StateFlow<ApiResult<AuthData>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<ApiResult<AuthData>>(ApiResult.Initial)
    val registerState: StateFlow<ApiResult<AuthData>> = _registerState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userProfileState = MutableStateFlow<ApiResult<User>>(ApiResult.Initial)
    val userProfileState: StateFlow<ApiResult<User>> = _userProfileState.asStateFlow()

    private val _updateProfileState = MutableStateFlow<ApiResult<User>>(ApiResult.Initial)
    val updateProfileState: StateFlow<ApiResult<User>> = _updateProfileState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        checkAuthStatus()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = ApiResult.Loading
            val result = authRepository.login(email, password)
            _loginState.value = result

            if (result is ApiResult.Success) {
                _currentUser.value = result.data.user
                _isLoggedIn.value = true

                FirebaseTopicManager.subscribe("serverdown")

                // Start server monitoring when user logs in
                serverMonitoringScheduler.scheduleServerMonitoring(appPreferences.getMonitoringInterval())
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        division: String,
        phone: String
    ) {
        viewModelScope.launch {
            _registerState.value = ApiResult.Loading
            val registerRequest = RegisterRequest(
                name = name,
                email = email,
                password = password,
                division = division,
                phone = phone,
            )
            val result = authRepository.register(registerRequest)
            _registerState.value = result

            if (result is ApiResult.Success) {
                _currentUser.value = result.data.user
                _isLoggedIn.value = true

                FirebaseTopicManager.subscribe("serverdown")

                // Start server monitoring when user registers
                serverMonitoringScheduler.scheduleServerMonitoring(appPreferences.getMonitoringInterval())
            }
        }
    }

    fun logout() {
        // Stop server monitoring when user logs out
        authRepository.clearAuthData()
        _currentUser.value = null
        _isLoggedIn.value = false
        _loginState.value = ApiResult.Initial
        _registerState.value = ApiResult.Initial
        _userProfileState.value = ApiResult.Initial
    }

    fun cleanupServices() {
        serverMonitoringScheduler.cancelServerMonitoring()
        FirebaseTopicManager.unsubscribe("serverdown")
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _userProfileState.value = ApiResult.Loading
            val result = authRepository.getCurrentUser()
            _userProfileState.value = result

            if (result is ApiResult.Success) {
                _currentUser.value = result.data
            }
        }
    }

    fun updateProfile(name: String, division: String, phone: String) {
        viewModelScope.launch {
            _updateProfileState.value = ApiResult.Loading
            val updateProfileRequest = UpdateProfileRequest(
                name = name,
                division = division,
                phone = phone
            )
            val result = authRepository.updateProfile(updateProfileRequest)
            _updateProfileState.value = result

            if (result is ApiResult.Success) {
                _currentUser.value = result.data
            }
        }
    }

    private fun checkAuthStatus() {
        _isLoggedIn.value = authRepository.isLoggedIn()
        if (_isLoggedIn.value) {
            _currentUser.value = authRepository.getSavedUser()
        }
    }

    fun resetLoginState() {
        _loginState.value = ApiResult.Initial
    }

    fun resetRegisterState() {
        _registerState.value = ApiResult.Initial
    }
}