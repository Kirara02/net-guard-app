package com.uniguard.netguard_app.domain.repository

import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Report
import com.uniguard.netguard_app.domain.model.ReportParams

interface ReportRepository {
    suspend fun getReports(params: ReportParams): ApiResult<List<Report>>
    suspend fun exportReport(params: ReportParams): ApiResult<ByteArray>
}
