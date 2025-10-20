package com.uniguard.netguard_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.AuthData
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<ApiResult<AuthData>>(ApiResult.Initial)
    val loginState: StateFlow<ApiResult<AuthData>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<ApiResult<AuthData>>(ApiResult.Initial)
    val registerState: StateFlow<ApiResult<AuthData>> = _registerState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userProfileState = MutableStateFlow<ApiResult<User>>(ApiResult.Initial)
    val userProfileState: StateFlow<ApiResult<User>> = _userProfileState.asStateFlow()

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
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        division: String,
        phone: String,
        role: String = "USER"
    ) {
        viewModelScope.launch {
            _registerState.value = ApiResult.Loading
            val registerRequest = com.uniguard.netguard_app.domain.model.RegisterRequest(
                name = name,
                email = email,
                password = password,
                division = division,
                phone = phone,
                role = role
            )
            val result = authRepository.register(registerRequest)
            _registerState.value = result

            if (result is ApiResult.Success) {
                _currentUser.value = result.data.user
                _isLoggedIn.value = true
            }
        }
    }

    fun logout() {
        authRepository.clearAuthData()
        _currentUser.value = null
        _isLoggedIn.value = false
        _loginState.value = ApiResult.Initial
        _registerState.value = ApiResult.Initial
        _userProfileState.value = ApiResult.Initial
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