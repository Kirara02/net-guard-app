package com.uniguard.netguard_app.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.data.remote.api.TokenExpiredException
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.History
import com.uniguard.netguard_app.domain.model.Server
import com.uniguard.netguard_app.domain.model.UserRole
import com.uniguard.netguard_app.domain.repository.AuthRepository
import com.uniguard.netguard_app.domain.repository.HistoryRepository
import com.uniguard.netguard_app.domain.repository.ServerRepository
import com.uniguard.netguard_app.domain.repository.ServerStatusRepository
import com.uniguard.netguard_app.domain.repository.UserRepository
import com.uniguard.netguard_app.domain.service.UserSessionService
import com.uniguard.netguardapp.db.ServerStatusEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DashboardViewModel(
    private val serverRepository: ServerRepository,
    private val historyRepository: HistoryRepository,
    private val authRepository: AuthRepository,
    private val serverStatusRepository: ServerStatusRepository,
    private val userRepository: UserRepository,
    private val userSessionService: UserSessionService
) : ViewModel(), KoinComponent {
    private val appPreferences: AppPreferences by inject()

    private val _servers = MutableStateFlow<List<Server>>(emptyList())

    private val _recentIncidents = MutableStateFlow<List<History>>(emptyList())
    val recentIncidents: StateFlow<List<History>> = _recentIncidents.asStateFlow()

    private val _serverStatuses = MutableStateFlow<List<ServerStatusEntity>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _totalUsers = MutableStateFlow(0)
    val totalUsers: StateFlow<Int> = _totalUsers.asStateFlow()

    private var isMonitoringScheduled = false

    init {
        // Check if monitoring is already scheduled from persistent storage
        isMonitoringScheduled = appPreferences.isMonitoringScheduled()
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Sync data from remote first
                syncDataFromRemote()

                // Load servers
                serverRepository.getAllServers().collect { serverList ->
                    _servers.value = serverList
                }

                // Load server statuses
                serverStatusRepository.getAllServerStatuses().collect { statusList ->
                    _serverStatuses.value = statusList
                }

                // Load recent incidents
                val incidentsResult = historyRepository.getHistories()
                when (incidentsResult) {
                    is ApiResult.Success -> {
                        _recentIncidents.value = incidentsResult.data
                    }
                    is ApiResult.Error -> {
                        _error.value = incidentsResult.message
                    }
                    else -> {}
                }

                // Load user count for admin
                if (isAdmin()) {
                    when (val usersResult = userRepository.getUsers()) {
                        is ApiResult.Success -> {
                            _totalUsers.value = usersResult.data.size
                        }
                        else -> {}
                    }
                }
                // Schedule server monitoring after data is loaded (only once)
                // This ensures servers are available in local database before scheduling
                if (!isMonitoringScheduled) {
                    userSessionService.startMonitoring()
                    isMonitoringScheduled = true
                    appPreferences.setMonitoringScheduled(true)
                }

            } catch (e: TokenExpiredException) {
                // Token expired - trigger logout
                authRepository.clearAuthData()
                _error.value = "Session expired. Please login again."
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load dashboard data"
            } finally {
                _isLoading.value = false
            }
        }
    }


    private suspend fun syncDataFromRemote() {
        try {
            // Sync servers from remote
            serverRepository.syncServersFromRemote()

            // Sync recent history
            historyRepository.getHistories()
        } catch (e: Exception) {
            // Handle sync errors silently for now
            // In production, you might want to show a toast or log this
        }
    }

    // Computed properties for stats
    val totalServers: Int
        get() = _servers.value.size

    val onlineServers: Int
        get() = _serverStatuses.value.count { it.status == "UP" }

    val downServers: Int
        get() = _serverStatuses.value.count { it.status == "DOWN" }

    val totalIncidents: Int
        get() = _recentIncidents.value.size

    val downIncidents: Int
        get() = _recentIncidents.value.count { it.status.equals("DOWN", ignoreCase = true) }

    fun isAdmin(): Boolean =
        userSessionService.getUserRole() == UserRole.ADMIN

}