package com.uniguard.netguard_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Report
import com.uniguard.netguard_app.domain.model.ReportParams
import com.uniguard.netguard_app.domain.model.Server
import com.uniguard.netguard_app.domain.repository.ReportRepository
import com.uniguard.netguard_app.domain.repository.ServerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReportViewModel(
    private val reportRepository: ReportRepository,
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _reportsState = MutableStateFlow<ApiResult<List<Report>>>(ApiResult.Initial)
    val reportsState: StateFlow<ApiResult<List<Report>>> = _reportsState.asStateFlow()

    private val _exportState = MutableStateFlow<ApiResult<ByteArray>>(ApiResult.Initial)
    val exportState: StateFlow<ApiResult<ByteArray>> = _exportState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers: StateFlow<List<Server>> = _servers.asStateFlow()

    fun loadReports(params: ReportParams = ReportParams()) {
        viewModelScope.launch {
            _isLoading.value = true
            _reportsState.value = ApiResult.Loading

            try {
                val result = reportRepository.getReports(params)
                _reportsState.value = result
                if (result is ApiResult.Success) {
                    _reports.value = result.data
                }
            } catch (e: Exception) {
                _reportsState.value = ApiResult.Error(e.message ?: "Failed to load reports")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadServers() {
        viewModelScope.launch {
            try {
                serverRepository.getAllServers().collect { servers ->
                    _servers.value = servers
                }
            } catch (e: Exception) {
                // Handle error silently for server options
                _servers.value = emptyList()
            }
        }
    }

    fun exportReports(params: ReportParams = ReportParams()) {
        viewModelScope.launch {
            _exportState.value = ApiResult.Loading

            try {
                val result = reportRepository.exportReport(params)
                _exportState.value = result
            } catch (e: Exception) {
                _exportState.value = ApiResult.Error(e.message ?: "Failed to export reports")
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ApiResult.Initial
    }
}