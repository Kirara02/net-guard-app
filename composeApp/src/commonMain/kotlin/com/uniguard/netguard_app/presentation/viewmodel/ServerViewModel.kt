package com.uniguard.netguard_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.data.remote.api.TokenExpiredException
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Server
import com.uniguard.netguard_app.domain.repository.AuthRepository
import com.uniguard.netguard_app.domain.repository.ServerRepository
import com.uniguard.netguard_app.domain.repository.ServerStatusRepository
import com.uniguard.netguardapp.db.ServerStatusEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServerViewModel(
    private val serverRepository: ServerRepository,
    private val serverStatusRepository: ServerStatusRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers: StateFlow<List<Server>> = _servers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedServer = MutableStateFlow<Server?>(null)
    val selectedServer: StateFlow<Server?> = _selectedServer.asStateFlow()

    private val _serverStatuses = MutableStateFlow<Map<String, ServerStatusEntity>>(emptyMap())
    val serverStatuses: StateFlow<Map<String, ServerStatusEntity>> = _serverStatuses.asStateFlow()

    init {
        loadServers()
        loadServerStatuses()
    }

    fun loadServers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                serverRepository.getAllServers().collect { serverList ->
                    _servers.value = serverList
                }
            } catch (e: TokenExpiredException) {
                // Token expired - trigger logout
                authRepository.clearAuthData()
                _error.value = "Session expired. Please login again."
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load servers"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshServers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Sync from remote first
                val syncResult = serverRepository.syncServersFromRemote()
                when (syncResult) {
                    is ApiResult.Success -> {
                        // Reload local data
                        serverRepository.getAllServers().collect { serverList ->
                            _servers.value = serverList
                        }
                    }
                    is ApiResult.Error -> {
                        _error.value = syncResult.message
                        // Still load local data even if sync fails
                        serverRepository.getAllServers().collect { serverList ->
                            _servers.value = serverList
                        }
                    }
                    else -> {
                        // Load local data
                        serverRepository.getAllServers().collect { serverList ->
                            _servers.value = serverList
                        }
                    }
                }
            } catch (e: TokenExpiredException) {
                // Token expired - trigger logout
                authRepository.clearAuthData()
                _error.value = "Session expired. Please login again."
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to refresh servers"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addServer(name: String, url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = serverRepository.createServer(name, url)

                when (result) {
                    is ApiResult.Success -> {
                        // Refresh the list
                        loadServers()
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                    }
                    else -> {}
                }
            } catch (e: TokenExpiredException) {
                // Token expired - trigger logout
                authRepository.clearAuthData()
                _error.value = "Session expired. Please login again."
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateServer(serverId: String, name: String, url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = serverRepository.updateServer(serverId, name, url)

                when (result) {
                    is ApiResult.Success -> {
                        // Refresh the list
                        loadServers()
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                    }
                    else -> {}
                }
            } catch (e: TokenExpiredException) {
                // Token expired - trigger logout
                authRepository.clearAuthData()
                _error.value = "Session expired. Please login again."
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteServer(serverId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = serverRepository.deleteServer(serverId)

                when (result) {
                    is ApiResult.Success -> {
                        // Refresh the list
                        loadServers()
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                    }
                    else -> {}
                }
            } catch (e: TokenExpiredException) {
                // Token expired - trigger logout
                authRepository.clearAuthData()
                _error.value = "Session expired. Please login again."
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectServer(server: Server?) {
        _selectedServer.value = server
    }

    fun clearError() {
        _error.value = null
    }

    private fun loadServerStatuses() {
        viewModelScope.launch {
            serverStatusRepository.getAllServerStatuses().collect { statuses ->
                _serverStatuses.value = statuses.associateBy { it.server_id }
            }
        }
    }

    // Computed properties for stats
    val totalServers: Int
        get() = _servers.value.size

}