package com.uniguard.netguard_app.data.repository

import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Report
import com.uniguard.netguard_app.domain.model.ReportParams
import com.uniguard.netguard_app.domain.repository.ReportRepository

class ReportRepositoryImpl (
    private val api: NetGuardApi,
    private val prefs: AppPreferences
) : ReportRepository {
    override suspend fun getReports(params: ReportParams): ApiResult<List<Report>> {
        val token = prefs.getToken()
        if (token != null) {
            return api.getReports(token, params)
        } else {
            return ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun exportReport(params: ReportParams): ApiResult<ByteArray> {
        val token = prefs.getToken()
        if (token != null) {
            // For export, use the export endpoint
            return api.exportReports(token, params)
        } else {
            return ApiResult.Error("No authentication token found")
        }
    }
}