package com.uniguard.netguard_app.domain.repository

import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.History
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    // Remote operations
    suspend fun syncHistoryFromRemote(serverId: String? = null, limit: Int = 50): ApiResult<List<History>>
    suspend fun createHistory(serverId: String, serverName: String, url: String, status: String): ApiResult<History>
    suspend fun resolveHistory(historyId: String, resolveNote: String): ApiResult<History>

    // Local operations
    fun getAllHistory(): Flow<List<History>>
    fun getHistoryByServer(serverId: String): Flow<List<History>>
    suspend fun getHistoryById(historyId: String): History?
    suspend fun insertOrUpdateHistory(history: History)
    suspend fun insertOrUpdateHistories(histories: List<History>)
    suspend fun deleteHistory(historyId: String)
    suspend fun clearAllHistory()

    // Sync operations
    suspend fun syncPendingHistoriesToRemote(): ApiResult<List<History>>
    suspend fun getRecentServerStatus(serverId: String): History?

    // Dashboard specific
    suspend fun getRecentIncidents(limit: Int? = null): ApiResult<List<History>>
}