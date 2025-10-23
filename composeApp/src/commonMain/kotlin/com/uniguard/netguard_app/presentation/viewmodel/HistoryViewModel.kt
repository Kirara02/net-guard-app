package com.uniguard.netguard_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.History
import com.uniguard.netguard_app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _histories = MutableStateFlow<List<History>>(emptyList())
    val histories: StateFlow<List<History>> = _histories.asStateFlow()

    private val _filteredHistories = MutableStateFlow<List<History>>(emptyList())
    val filteredHistories: StateFlow<List<History>> = _filteredHistories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Filter states
    private val _serverFilter = MutableStateFlow("")
    val serverFilter: StateFlow<String> = _serverFilter.asStateFlow()

    private val _statusFilter = MutableStateFlow("")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    // Dropdown options
    val serverOptions: StateFlow<List<String>> = _histories.map { histories ->
        histories.map { it.serverName }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statusOptions: StateFlow<List<String>> = _histories.map { histories ->
        histories.map { it.status }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadHistories()
        setupFiltering()
    }

    fun loadHistories() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                historyRepository.getAllHistory().collect { historyList ->
                    _histories.value = historyList
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load histories"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun syncHistoriesFromRemote(serverId: String? = null, limit: Int = 50) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = historyRepository.syncHistoryFromRemote(serverId, limit)) {
                is ApiResult.Success -> {
                    // Data will be automatically updated through the flow
                    loadHistories()
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
                is ApiResult.Loading -> {
                    // Handle loading state if needed
                }
                is ApiResult.Initial -> {
                    // Handle initial state if needed
                }
            }

            _isLoading.value = false
        }
    }

    fun resolveHistory(historyId: String, resolveNote: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = historyRepository.resolveHistory(historyId, resolveNote)) {
                is ApiResult.Success -> {
                    // Data will be automatically updated through the flow
                    loadHistories()
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
                is ApiResult.Loading -> {
                    // Handle loading state if needed
                }
                is ApiResult.Initial -> {
                    // Handle initial state if needed
                }
            }

            _isLoading.value = false
        }
    }

    fun updateServerFilter(filter: String) {
        _serverFilter.value = filter
    }

    fun updateStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun clearFilters() {
        _serverFilter.value = ""
        _statusFilter.value = ""
    }

    private fun setupFiltering() {
        viewModelScope.launch {
            combine(
                _histories,
                _serverFilter,
                _statusFilter
            ) { histories, serverFilter, statusFilter ->
                histories.filter { history ->
                    val matchesServer = serverFilter.isBlank() ||
                        history.serverName.contains(serverFilter, ignoreCase = true)
                    val matchesStatus = statusFilter.isBlank() ||
                        history.status.equals(statusFilter, ignoreCase = true)
                    matchesServer && matchesStatus
                }
            }.collect { filtered ->
                _filteredHistories.value = filtered
            }
        }
    }

    fun refreshData() {
        loadHistories()
    }
}