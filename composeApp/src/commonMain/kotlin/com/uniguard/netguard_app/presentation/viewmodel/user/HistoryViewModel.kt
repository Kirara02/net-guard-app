package com.uniguard.netguard_app.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.History
import com.uniguard.netguard_app.domain.model.ResolveHistoryRequest
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

    private val _histories = MutableStateFlow<ApiResult<List<History>>>(ApiResult.Initial)
    val histories: StateFlow<ApiResult<List<History>>> = _histories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadHistories() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = historyRepository.getHistories()
                _histories.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load histories"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun resolveHistory(historyId: String, resolveNote: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = historyRepository.resolveHistory(
                historyId = historyId,
                request = ResolveHistoryRequest(resolveNote)
            )) {
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

}