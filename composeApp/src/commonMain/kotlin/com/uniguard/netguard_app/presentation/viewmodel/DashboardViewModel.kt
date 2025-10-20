package com.uniguard.netguard_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.data.remote.api.TokenExpiredException
import com.uniguard.netguard_app.di.AppModule
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.History
import com.uniguard.netguard_app.domain.model.Server
import com.uniguard.netguard_app.domain.repository.HistoryRepository
import com.uniguard.netguard_app.domain.repository.ServerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val serverRepository: ServerRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers: StateFlow<List<Server>> = _servers.asStateFlow()

    private val _recentIncidents = MutableStateFlow<List<History>>(emptyList())
    val recentIncidents: StateFlow<List<History>> = _recentIncidents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
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

                // Load recent incidents
                val incidentsResult = historyRepository.getRecentIncidents(limit = 5)
                when (incidentsResult) {
                    is ApiResult.Success -> {
                        _recentIncidents.value = incidentsResult.data
                    }
                    is ApiResult.Error -> {
                        _error.value = incidentsResult.message
                    }
                    is ApiResult.Loading -> {
                        // Handle loading state if needed
                    }
                    is ApiResult.Initial -> {
                        // Handle initial state if needed
                    }
                }
            } catch (e: TokenExpiredException) {
                // Token expired - trigger logout
                AppModule.authViewModel.logout()
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
            historyRepository.syncHistoryFromRemote(limit = 20)
        } catch (e: Exception) {
            // Handle sync errors silently for now
            // In production, you might want to show a toast or log this
        }
    }

    // Computed properties for stats
    val totalServers: Int
        get() = _servers.value.size

    val onlineServers: Int
        get() = _servers.value.count { it.status == com.uniguard.netguard_app.domain.model.ServerStatus.UP }

    val downServers: Int
        get() = _servers.value.count { it.status == com.uniguard.netguard_app.domain.model.ServerStatus.DOWN }

    val totalIncidents: Int
        get() = _recentIncidents.value.size
}