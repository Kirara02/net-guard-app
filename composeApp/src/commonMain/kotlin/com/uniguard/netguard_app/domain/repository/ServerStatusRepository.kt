package com.uniguard.netguard_app.domain.repository

import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguardapp.db.ServerStatusEntity
import kotlinx.coroutines.flow.Flow

interface ServerStatusRepository {
    fun getAllServerStatuses(): Flow<List<ServerStatusEntity>>
    suspend fun getServerStatus(serverId: String): ServerStatusEntity?
    suspend fun updateServerStatus(
        serverId: String,
        status: String,
        lastChecked: String,
        responseTime: Long? = null,
        updatedAt: String
    ): ApiResult<Unit>
    suspend fun deleteServerStatus(serverId: String): ApiResult<Unit>
    suspend fun clearAllStatuses(): ApiResult<Unit>
}