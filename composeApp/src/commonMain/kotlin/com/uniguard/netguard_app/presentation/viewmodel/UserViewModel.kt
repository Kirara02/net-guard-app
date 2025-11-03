package com.uniguard.netguard_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.domain.model.CreateUserRequest
import com.uniguard.netguard_app.domain.model.UpdateUserRequest
import com.uniguard.netguard_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _usersState = MutableStateFlow<ApiResult<List<User>>>(ApiResult.Initial)
    val usersState: StateFlow<ApiResult<List<User>>> = _usersState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _createUserState = MutableStateFlow<ApiResult<User>>(ApiResult.Initial)
    val createUserState: StateFlow<ApiResult<User>> = _createUserState.asStateFlow()

    private val _updateUserState = MutableStateFlow<ApiResult<User>>(ApiResult.Initial)
    val updateUserState: StateFlow<ApiResult<User>> = _updateUserState.asStateFlow()

    private val _deleteUserState = MutableStateFlow<ApiResult<String>>(ApiResult.Initial)
    val deleteUserState: StateFlow<ApiResult<String>> = _deleteUserState.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.getUsers()
                _usersState.value = result
            } catch (e: Exception) {
                _usersState.value = ApiResult.Error(e.message ?: "Failed to load users")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createUser(name: String, email: String, password: String?, division: String, phone: String, role: String) {
        viewModelScope.launch {
            _createUserState.value = ApiResult.Loading

            try {
                val request = CreateUserRequest(name, email, password ?: "", division, phone, role.uppercase())
                val result = repository.createUser(request)
                _createUserState.value = result

                // Reload users list if creation successful
                if (result is ApiResult.Success) {
                    loadUsers()
                }
            } catch (e: Exception) {
                _createUserState.value = ApiResult.Error(e.message ?: "Failed to create user")
            }
        }
    }

    fun updateUser(userId: String, name: String, email: String, password: String?, division: String, phone: String, role: String) {
        viewModelScope.launch {
            _updateUserState.value = ApiResult.Loading

            try {
                val request = UpdateUserRequest(name, email, password, division, phone, role.uppercase())
                val result = repository.updateUser(userId, request)
                _updateUserState.value = result

                // Reload users list if update successful
                if (result is ApiResult.Success) {
                    loadUsers()
                }
            } catch (e: Exception) {
                _updateUserState.value = ApiResult.Error(e.message ?: "Failed to update user")
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _deleteUserState.value = ApiResult.Loading

            try {
                val result = repository.deleteById(userId)
                _deleteUserState.value = result

                // Reload users list if deletion successful
                if (result is ApiResult.Success) {
                    loadUsers()
                }
            } catch (e: Exception) {
                _deleteUserState.value = ApiResult.Error(e.message ?: "Failed to delete user")
            }
        }
    }

    fun resetCreateUserState() {
        _createUserState.value = ApiResult.Initial
    }

    fun resetUpdateUserState() {
        _updateUserState.value = ApiResult.Initial
    }

    fun resetDeleteUserState() {
        _deleteUserState.value = ApiResult.Initial
    }

    fun resetAllState() {
        resetCreateUserState()
        resetUpdateUserState()
        resetDeleteUserState()
    }
}