package com.uniguard.netguard_app.data.repository

import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.CreateHistoryRequest
import com.uniguard.netguard_app.domain.model.History
import com.uniguard.netguard_app.domain.model.ResolveHistoryRequest
import com.uniguard.netguard_app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HistoryRepositoryImpl(
    private val api: NetGuardApi,
    private val prefs: AppPreferences
) : HistoryRepository {

    override suspend fun getHistories(): ApiResult<List<History>> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.getHistories(token)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun createHistory(
        request: CreateHistoryRequest
    ): ApiResult<History> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.createHistory(token, request)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun resolveHistory(
        historyId: String,
        request: ResolveHistoryRequest
    ): ApiResult<History> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.resolveHistory(token, historyId, request)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

}
