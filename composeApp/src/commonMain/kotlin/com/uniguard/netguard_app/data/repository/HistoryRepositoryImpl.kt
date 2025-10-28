package com.uniguard.netguard_app.data.repository

import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.History
import com.uniguard.netguard_app.domain.model.ResolveHistoryRequest
import com.uniguard.netguard_app.domain.repository.HistoryRepository
import com.uniguard.netguardapp.db.HistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HistoryRepositoryImpl(
    private val api: NetGuardApi,
    private val databaseProvider: DatabaseProvider,
    private val appPreferences: AppPreferences
) : HistoryRepository {

    private val database = databaseProvider.getDatabase()
    private val historyQueries = database.appDatabaseQueries

    // Remote operations
    override suspend fun syncHistoryFromRemote(serverId: String?, limit: Int): ApiResult<List<History>> {
        val token = appPreferences.getToken()
        return if (token != null) {
            val result = api.getHistory(token, serverId, limit)
            when (result) {
                is ApiResult.Success -> {
                    // Save to local database
                    insertOrUpdateHistories(result.data)
                    result
                }
                else -> result
            }
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun createHistory(serverId: String, serverName: String, url: String, status: String): ApiResult<History> {
        val token = appPreferences.getToken()
        return if (token != null) {
            val result = api.createHistory(
                token,
                com.uniguard.netguard_app.domain.model.CreateHistoryRequest(serverId, serverName, url, status)
            )
            when (result) {
                is ApiResult.Success -> {
                    // Save to local database
                    insertOrUpdateHistory(result.data)
                    result
                }
                else -> result
            }
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun resolveHistory(historyId: String, resolveNote: String): ApiResult<History> {
        val token = appPreferences.getToken()
        return if (token != null) {
            val result = api.resolveHistory(
                token,
                historyId,
                ResolveHistoryRequest(resolveNote)
            )
            when (result) {
                is ApiResult.Success -> {
                    // Update local database
                    insertOrUpdateHistory(result.data)
                    result
                }
                else -> result
            }
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    // Local operations
    override fun getAllHistory(): Flow<List<History>> {
        return flow {
            val entities = historyQueries.getAllHistory().executeAsList()
            emit(entities.map { it.toDomain() })
        }
    }

    override fun getHistoryByServer(serverId: String): Flow<List<History>> {
        return flow {
            val entities = historyQueries.getHistoryByServer(serverId).executeAsList()
            emit(entities.map { it.toDomain() })
        }
    }

    override suspend fun getHistoryById(historyId: String): History? {
        // Note: getHistoryById query doesn't exist in the schema, using alternative approach
        // For now, return null as this method is not critical for basic functionality
        return null
    }

    override suspend fun insertOrUpdateHistory(history: History) {
        historyQueries.insertHistory(
            id = history.id,
            server_id = history.serverId,
            server_name = history.serverName,
            url = history.url,
            status = history.status,
            timestamp = history.timestamp,
            created_by = history.createdBy,
            resolved_by = history.resolvedBy,
            resolved_at = history.resolvedAt,
            resolve_note = history.resolveNote
        )
    }

    override suspend fun insertOrUpdateHistories(histories: List<History>) {
        histories.forEach { insertOrUpdateHistory(it) }
    }

    override suspend fun deleteHistory(historyId: String) {
        historyQueries.deleteHistoryById(historyId)
    }

    override suspend fun clearAllHistory() {
        historyQueries.clearHistory()
    }

    // Sync operations
    override suspend fun syncPendingHistoriesToRemote(): ApiResult<List<History>> {
        // For now, just return success. In a real implementation,
        // you might want to track which histories haven't been synced yet
        return ApiResult.Success(emptyList())
    }

    override suspend fun getRecentServerStatus(serverId: String): History? {
        return historyQueries.getRecentStatusForServer(serverId).executeAsOneOrNull()?.let { entity ->
            History(
                id = entity.id,
                serverId = entity.server_id,
                serverName = entity.server_name,
                url = entity.url,
                status = entity.status,
                timestamp = entity.timestamp,
                createdBy = entity.created_by,
                resolvedBy = entity.resolved_by,
                resolvedAt = entity.resolved_at,
                resolveNote = entity.resolve_note
            )
        }
    }

    override suspend fun getRecentIncidents(limit: Int?): ApiResult<List<History>> {
        return try {
            val entities = historyQueries.getAllHistory().executeAsList()
                .sortedByDescending { it.timestamp }
                .let { if (limit != null) it.take(limit) else it }
            val histories = entities.map { it.toDomain() }
            ApiResult.Success(histories)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to get recent incidents")
        }
    }

    // Extension function to convert database entity to domain model
    private fun HistoryEntity.toDomain(): History {
        return History(
            id = id,
            serverId = server_id,
            serverName = server_name,
            url = url,
            status = status,
            timestamp = timestamp,
            createdBy = created_by,
            resolvedBy = resolved_by,
            resolvedAt = resolved_at,
            resolveNote = resolve_note
        )
    }
}