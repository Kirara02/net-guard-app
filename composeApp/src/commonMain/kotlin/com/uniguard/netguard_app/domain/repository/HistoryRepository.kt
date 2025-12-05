package com.uniguard.netguard_app.domain.repository

import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.CreateHistoryRequest
import com.uniguard.netguard_app.domain.model.History
import com.uniguard.netguard_app.domain.model.ResolveHistoryRequest

interface HistoryRepository {

    suspend fun getHistories(): ApiResult<List<History>>

    suspend fun createHistory(
        request: CreateHistoryRequest
    ): ApiResult<History>

    suspend fun resolveHistory(
        historyId: String,
        request: ResolveHistoryRequest
    ): ApiResult<History>

}