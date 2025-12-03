package com.uniguard.netguard_app.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Server
import com.uniguard.netguard_app.domain.model.ServerLoadSource
import com.uniguard.netguard_app.domain.model.UserRole
import com.uniguard.netguard_app.domain.repository.ServerRepository
import com.uniguard.netguard_app.domain.repository.ServerStatusRepository
import com.uniguard.netguard_app.domain.service.UserSessionService
import com.uniguard.netguardapp.db.ServerStatusEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServerViewModel(
    private val serverRepository: ServerRepository,
    private val serverStatusRepository: ServerStatusRepository,
    private val appPreferences: AppPreferences,
    private val userSessionService: UserSessionService,
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

    private val _createServerState = MutableStateFlow<ApiResult<Server>>(ApiResult.Initial)
    val createServerState: StateFlow<ApiResult<Server>> = _createServerState.asStateFlow()

    private val _updateServerState = MutableStateFlow<ApiResult<Server>>(ApiResult.Initial)
    val updateServerState: StateFlow<ApiResult<Server>> = _updateServerState.asStateFlow()

    private val _deleteServerState = MutableStateFlow<ApiResult<String>>(ApiResult.Initial)
    val deleteServerState: StateFlow<ApiResult<String>> = _deleteServerState.asStateFlow()

    fun loadServers(source: ServerLoadSource = ServerLoadSource.LOCAL) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                when (source) {
                    // ✅ DEFAULT
                    ServerLoadSource.LOCAL -> {
                        serverRepository.getAllServers().collect {
                            _servers.value = it
                        }
                    }

                    // ✅ FORCE REMOTE
                    ServerLoadSource.REMOTE -> {
                        when (
                            val result = serverRepository.syncServersFromRemote(
                                withLocal = shouldWriteLocal()
                            )
                        ) {
                            is ApiResult.Success -> {
                                _servers.value = result.data
                            }
                            is ApiResult.Error -> {
                                _error.value = result.message
                            }
                            else -> Unit
                        }
                    }
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load servers"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addServer(name: String, url: String, groupId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = serverRepository.createServer(
                    name,
                    url,
                    groupId,
                    withLocal = shouldWriteLocal()
                )

                when (result) {
                    is ApiResult.Success -> {
                        if(userSessionService.getUserRole() != UserRole.SUPER_ADMIN) {
                            loadServers()

                            userSessionService.stopMonitoring()
                            userSessionService.startMonitoring(appPreferences.getMonitoringInterval())
                        }

                        _createServerState.value = ApiResult.Success(result.data)
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                        _createServerState.value = ApiResult.Error(result.message)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to add server"
                _error.value = msg
                _createServerState.value = ApiResult.Error(msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateServer(serverId: String, name: String, url: String, groupId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _updateServerState.value = ApiResult.Loading

            try {
                val result = serverRepository.updateServer(
                    serverId,
                    name,
                    url,
                    groupId,
                    withLocal = shouldWriteLocal()
                )

                when (result) {
                    is ApiResult.Success -> {
                        if (userSessionService.getUserRole() != UserRole.SUPER_ADMIN) {
                            loadServers()
                            userSessionService.stopMonitoring()
                            userSessionService.startMonitoring(appPreferences.getMonitoringInterval())
                        }

                        _updateServerState.value = ApiResult.Success(result.data)
                    }

                    is ApiResult.Error -> {
                        _error.value = result.message
                        _updateServerState.value = ApiResult.Error(result.message)
                    }

                    else -> {}
                }

            } catch (e: Exception) {
                val msg = e.message ?: "Failed to update server"
                _error.value = msg
                _updateServerState.value = ApiResult.Error(msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteServer(serverId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _deleteServerState.value = ApiResult.Loading

            try {
                val result = serverRepository.deleteServer(
                    serverId,
                    withLocal = shouldWriteLocal()
                )

                when (result) {
                    is ApiResult.Success -> {
                        _deleteServerState.value = ApiResult.Success(serverId)
                        if (userSessionService.getUserRole() != UserRole.SUPER_ADMIN) {
                            loadServers()
                        }
                    }

                    is ApiResult.Error -> {
                        _error.value = result.message
                        _deleteServerState.value = ApiResult.Error(result.message)
                    }

                    else -> {}
                }

            } catch (e: Exception) {
                val msg = e.message ?: "Failed to delete server"
                _error.value = msg
                _deleteServerState.value = ApiResult.Error(msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadServersByRole() {
        val isSuperAdmin = userSessionService.getUserRole() == UserRole.SUPER_ADMIN

        val source = if (isSuperAdmin) {
            ServerLoadSource.REMOTE
        } else {
            ServerLoadSource.LOCAL
        }

        loadServers(source)

        // ✅ Load statuses ONLY for non-superadmin
        if (!isSuperAdmin) {
            loadServerStatuses()
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

    private fun shouldWriteLocal(): Boolean {
        return userSessionService.getUserRole() != UserRole.SUPER_ADMIN
    }

    fun isSuperAdmin(): Boolean =
        userSessionService.getUserRole() == UserRole.SUPER_ADMIN

    // Computed properties for stats
    val totalServers: Int get() = _servers.value.size

    fun resetCreateServerState() {
        _createServerState.value = ApiResult.Initial
    }

    fun resetUpdateServerState() {
        _updateServerState.value = ApiResult.Initial
    }

    fun resetDeleteServerState() {
        _deleteServerState.value = ApiResult.Initial
    }

}