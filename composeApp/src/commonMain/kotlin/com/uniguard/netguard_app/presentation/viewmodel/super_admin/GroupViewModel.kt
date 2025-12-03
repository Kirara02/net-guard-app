package com.uniguard.netguard_app.presentation.viewmodel.super_admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Group
import com.uniguard.netguard_app.domain.model.GroupRequest
import com.uniguard.netguard_app.domain.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroupViewModel (
    private val repository: GroupRepository
) : ViewModel() {

    private val _groupsState = MutableStateFlow<ApiResult<List<Group>>>(ApiResult.Initial)
    val groupsState: StateFlow<ApiResult<List<Group>>> = _groupsState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _createGroupState = MutableStateFlow<ApiResult<Group>>(ApiResult.Initial)
    val createGroupState: StateFlow<ApiResult<Group>> = _createGroupState.asStateFlow()

    private val _updateGroupState = MutableStateFlow<ApiResult<Group>>(ApiResult.Initial)
    val updateGroupState: StateFlow<ApiResult<Group>> = _updateGroupState.asStateFlow()

    private val _deleteGroupState = MutableStateFlow<ApiResult<String>>(ApiResult.Initial)
    val deleteGroupState: StateFlow<ApiResult<String>> = _deleteGroupState.asStateFlow()

    fun loadGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.getGroups()
                _groupsState.value = result
            } catch (e: Exception) {
                _groupsState.value = ApiResult.Error(e.message ?: "Failed to load groups")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createGroup(name: String, description: String?, maxMembers: Int) {
        viewModelScope.launch {
            _createGroupState.value = ApiResult.Loading

            try {
                val request = GroupRequest(
                    name,
                    description,
                    maxMembers
                )

                val result = repository.createGroup(request)
                _createGroupState.value = result

                if (result is ApiResult.Success) {
                    loadGroups()
                }
            } catch (e: Exception) {
                _createGroupState.value = ApiResult.Error(e.message ?: "Failed to create user")
            }
        }
    }

    fun updateGroup(groupId: String, name: String, description: String?, maxMembers: Int) {
        viewModelScope.launch {
            _updateGroupState.value = ApiResult.Loading

            try {
                val request = GroupRequest(name,description, maxMembers)
                val result = repository.updateGroup(groupId, request)
                _updateGroupState.value = result

                if (result is ApiResult.Success) {
                    loadGroups()
                }
            } catch (e: Exception) {
                _updateGroupState.value = ApiResult.Error(e.message ?: "Failed to update user")
            }
        }
    }

    fun deleteGroup(userId: String) {
        viewModelScope.launch {
            _deleteGroupState.value = ApiResult.Loading

            try {
                val result = repository.deleteById(userId)
                _deleteGroupState.value = result

                // Reload users list if deletion successful
                if (result is ApiResult.Success) {
                    loadGroups()
                }
            } catch (e: Exception) {
                _deleteGroupState.value = ApiResult.Error(e.message ?: "Failed to delete user")
            }
        }
    }

    fun resetCreateGroupState() {
        _createGroupState.value = ApiResult.Initial
    }

    fun resetUpdateGroupState() {
        _updateGroupState.value = ApiResult.Initial
    }

    fun resetDeleteGroupState() {
        _deleteGroupState.value = ApiResult.Initial
    }

    fun resetAllState() {
        resetCreateGroupState()
        resetUpdateGroupState()
        resetDeleteGroupState()
    }

}