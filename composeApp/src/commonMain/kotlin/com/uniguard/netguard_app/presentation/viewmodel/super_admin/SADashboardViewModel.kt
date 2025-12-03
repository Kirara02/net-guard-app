package com.uniguard.netguard_app.presentation.viewmodel.super_admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Dashboard
import com.uniguard.netguard_app.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SADashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _dashboard = MutableStateFlow<Dashboard?>(null)
    val dashboard: StateFlow<Dashboard?> = _dashboard

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                when (val result = repository.getAdminDashboard()) {

                    is ApiResult.Success -> {
                        _dashboard.value = result.data
                    }

                    is ApiResult.Error -> {
                        _error.value = result.message
                    }

                    else -> {
                        _error.value = "Unknown error"
                    }
                }
            }
            catch (e: Exception) {
                _error.value = e.message ?: "Unexpected error"
            }
            finally {
                _isLoading.value = false
            }
        }
    }
}