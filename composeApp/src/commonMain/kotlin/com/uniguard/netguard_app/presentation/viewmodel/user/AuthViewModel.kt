package com.uniguard.netguard_app.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.AuthData
import com.uniguard.netguard_app.domain.model.ChangePasswordRequest
import com.uniguard.netguard_app.domain.model.UpdateProfileRequest
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.domain.repository.AuthRepository
import com.uniguard.netguard_app.domain.service.UserSessionService
import com.uniguard.netguard_app.log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userSessionService: UserSessionService
) : ViewModel(), KoinComponent {
    private val appPreferences: AppPreferences by inject()

    private val _loginState = MutableStateFlow<ApiResult<AuthData>>(ApiResult.Initial)
    val loginState: StateFlow<ApiResult<AuthData>> = _loginState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userProfileState = MutableStateFlow<ApiResult<User>>(ApiResult.Initial)
    val userProfileState: StateFlow<ApiResult<User>> = _userProfileState.asStateFlow()

    private val _updateProfileState = MutableStateFlow<ApiResult<User>>(ApiResult.Initial)
    val updateProfileState: StateFlow<ApiResult<User>> = _updateProfileState.asStateFlow()

    private val _changePasswordState = MutableStateFlow<ApiResult<Unit>>(ApiResult.Initial)
    val changePasswordState: StateFlow<ApiResult<Unit>> = _changePasswordState.asStateFlow()

    private val _logoutState = MutableStateFlow<ApiResult<String>>(ApiResult.Initial)
    val logoutState: StateFlow<ApiResult<String>> = _logoutState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isUserChecked = MutableStateFlow(false)
    val isUserChecked = _isUserChecked.asStateFlow()


    init {
        viewModelScope.launch {
            // Cek ada token atau tidak
            val token = authRepository.getSavedToken()

            if (token.isNullOrEmpty()) {
                // Tidak ada token → bukan login
                _isLoggedIn.value = false
                _currentUser.value = null
                _isUserChecked.value = true
                return@launch
            }

            // Ada token → validasi ke server
            _userProfileState.value = ApiResult.Loading
            val result = authRepository.getCurrentUser()

            when (result) {
                is ApiResult.Success -> {
                    _currentUser.value = result.data
                    _isLoggedIn.value = true
                }

                is ApiResult.Error -> {
                    // Token invalid / expired
                    if (result.code == 401) {
                        clearLocalSession()
                    } else {
                        // server error tapi token mungkin valid
                        // tetap biarkan user dianggap login
                        _currentUser.value = authRepository.getSavedUser()
                        _isLoggedIn.value = _currentUser.value != null
                    }
                }

                else -> {}
            }

            _isUserChecked.value = true
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = ApiResult.Loading
            val result = authRepository.login(email, password)
            _loginState.value = result

            if (result is ApiResult.Success) {
                val user = result.data.user
                _currentUser.value = user
                _isLoggedIn.value = true

                userSessionService.subscribeTopic()
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            _logoutState.value = ApiResult.Loading

            val result = authRepository.logout()
            _logoutState.value = result

            when (result) {
                is ApiResult.Success -> {
                    delay(300)
                    clearLocalSession()
                }
                is ApiResult.Error -> {
                    if (result.code == 401) {
                        clearLocalSession()
                    }
                }
                else -> {}
            }

            log { "ViewModel logout success triggered" }
        }
    }

    fun forceLocalLogout() {
        clearLocalSession()
    }

    private fun clearLocalSession() {
        userSessionService.endSession()
        authRepository.clearAuthData()
        resetAllStates()
        appPreferences.setMonitoringScheduled(false)
    }

    fun resetLogoutState() {
        _logoutState.value = ApiResult.Initial
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

            val result = authRepository.updateProfile(
                UpdateProfileRequest(name, division, phone)
            )

            _updateProfileState.value = result

            if (result is ApiResult.Success) {
                _currentUser.value = result.data
            }
        }
    }


    fun changePassword(request: ChangePasswordRequest) {
        viewModelScope.launch {
            _changePasswordState.value = ApiResult.Loading
            _changePasswordState.value = authRepository.changePassword(request)
        }
    }

    fun resetAllStates() {
        _currentUser.value = null
        _isLoggedIn.value = false
        _loginState.value = ApiResult.Initial
        _userProfileState.value = ApiResult.Initial
        _updateProfileState.value = ApiResult.Initial
        _changePasswordState.value = ApiResult.Initial
    }
}

