package com.uniguard.netguard_app.data.repository

import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Dashboard
import com.uniguard.netguard_app.domain.repository.DashboardRepository

class DashboardRepositoryImpl(
    private val api: NetGuardApi,
    private val prefs: AppPreferences
): DashboardRepository {

    override suspend fun getAdminDashboard(): ApiResult<Dashboard> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.getAdminDashboard(token)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

}