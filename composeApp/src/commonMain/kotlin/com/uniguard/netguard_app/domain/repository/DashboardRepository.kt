package com.uniguard.netguard_app.domain.repository

import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Dashboard

interface DashboardRepository {
    suspend fun getAdminDashboard(): ApiResult<Dashboard>
}