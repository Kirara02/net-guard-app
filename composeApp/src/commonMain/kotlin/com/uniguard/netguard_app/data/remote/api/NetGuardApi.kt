package com.uniguard.netguard_app.data.remote.api

import com.uniguard.netguard_app.domain.model.*
import com.uniguard.netguard_app.utils.Constants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
class NetGuardApi(
    httpClient: HttpClient,
    private val baseUrl: String = Constants.API_BASE_URL
) {

    private val client = httpClient

    // Authentication Endpoints
    suspend fun login(request: LoginRequest): ApiResult<AuthData> {
        return try {
            val response = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val authResponse: AuthResponse = response.body()
            if (authResponse.success && authResponse.data != null) {
                ApiResult.Success(authResponse.data)
            } else {
                // For auth endpoints, try to parse error response
                try {
                    val errorResponse: ErrorResponse = response.body()
                    ApiResult.Error(errorResponse.error)
                } catch (e: Exception) {
                    ApiResult.Error(authResponse.error ?: "Login failed")
                }
            }
        } catch (e: ClientException) {
            // Handle 401 for invalid credentials - parse error from the exception response
            try {
                val errorResponse: ErrorResponse = e.response.body()
                ApiResult.Error(errorResponse.error)
            } catch (parseException: Exception) {
                ApiResult.Error("Invalid credentials")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun register(request: RegisterRequest): ApiResult<AuthData> {
        return try {
            val response = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val authResponse: AuthResponse = response.body()
            if (authResponse.success && authResponse.data != null) {
                ApiResult.Success(authResponse.data)
            } else {
                // For auth endpoints, try to parse error response
                try {
                    val errorResponse: ErrorResponse = response.body()
                    ApiResult.Error(errorResponse.error)
                } catch (e: Exception) {
                    ApiResult.Error(authResponse.error ?: "Registration failed")
                }
            }
        } catch (e: ClientException) {
            // Handle 400/409 for validation errors - parse error from the exception response
            try {
                val errorResponse: ErrorResponse = e.response.body()
                ApiResult.Error(errorResponse.error)
            } catch (parseException: Exception) {
                ApiResult.Error("Registration failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getCurrentUser(token: String): ApiResult<User> {
        return try {
            val response = client.get("$baseUrl/auth/me") {
                header("Authorization", "Bearer $token")
            }
            val userResponse: UserResponse = response.body()
            if (userResponse.success && userResponse.data != null) {
                ApiResult.Success(userResponse.data)
            } else {
                ApiResult.Error(userResponse.error ?: "Failed to get user")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    // Server Management Endpoints
    suspend fun getServers(token: String): ApiResult<List<Server>> {
        return try {
            val response = client.get("$baseUrl/servers") {
                header("Authorization", "Bearer $token")
            }
            val serversResponse: ServersResponse = response.body()
            if (serversResponse.success) {
                ApiResult.Success(serversResponse.data)
            } else {
                ApiResult.Error(serversResponse.error ?: "Failed to get servers")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun createServer(token: String, request: CreateServerRequest): ApiResult<Server> {
        return try {
            val response = client.post("$baseUrl/servers") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val serverResponse: ServerResponse = response.body()
            if (serverResponse.success && serverResponse.data != null) {
                ApiResult.Success(serverResponse.data)
            } else {
                ApiResult.Error(serverResponse.error ?: "Failed to create server")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateServer(token: String, serverId: String, request: CreateServerRequest): ApiResult<Server> {
        return try {
            val response = client.put("$baseUrl/servers/$serverId") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val serverResponse: ServerResponse = response.body()
            if (serverResponse.success && serverResponse.data != null) {
                ApiResult.Success(serverResponse.data)
            } else {
                ApiResult.Error(serverResponse.error ?: "Failed to update server")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteServer(token: String, serverId: String): ApiResult<Unit> {
        return try {
            val response = client.delete("$baseUrl/servers/$serverId") {
                header("Authorization", "Bearer $token")
            }
            val serverResponse: ServerResponse = response.body()
            if (serverResponse.success) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(serverResponse.error ?: "Failed to delete server")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateServerStatus(token: String, serverId: String, request: UpdateServerStatusRequest): ApiResult<Server> {
        return try {
            val response = client.patch("$baseUrl/servers/$serverId/status") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val serverResponse: ServerResponse = response.body()
            if (serverResponse.success && serverResponse.data != null) {
                ApiResult.Success(serverResponse.data)
            } else {
                ApiResult.Error(serverResponse.error ?: "Failed to update server status")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    // History/Incident Management Endpoints
    suspend fun createHistory(token: String, request: CreateHistoryRequest): ApiResult<History> {
        return try {
            val response = client.post("$baseUrl/history") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val historyResponse: HistoryResponse = response.body()
            if (historyResponse.success && historyResponse.data != null) {
                ApiResult.Success(historyResponse.data)
            } else {
                ApiResult.Error(historyResponse.error ?: "Failed to create history")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getHistory(token: String, serverId: String? = null, limit: Int = 50): ApiResult<List<History>> {
        return try {
            val response = client.get("$baseUrl/history") {
                header("Authorization", "Bearer $token")
                parameter("server_id", serverId)
                parameter("limit", limit)
            }
            val historiesResponse: HistoriesResponse = response.body()
            if (historiesResponse.success) {
                ApiResult.Success(historiesResponse.data)
            } else {
                ApiResult.Error(historiesResponse.error ?: "Failed to get history")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun resolveHistory(token: String, historyId: String, request: ResolveHistoryRequest): ApiResult<History> {
        return try {
            val response = client.patch("$baseUrl/history/$historyId/resolve") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val historyResponse: HistoryResponse = response.body()
            if (historyResponse.success && historyResponse.data != null) {
                ApiResult.Success(historyResponse.data)
            } else {
                ApiResult.Error(historyResponse.error ?: "Failed to resolve history")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getMonthlyReport(token: String, year: Int, month: Int): ApiResult<MonthlyReportData> {
        return try {
            val response = client.get("$baseUrl/history/report/monthly") {
                header("Authorization", "Bearer $token")
                parameter("year", year)
                parameter("month", month)
            }
            val reportResponse: MonthlyReportResponse = response.body()
            if (reportResponse.success && reportResponse.data != null) {
                ApiResult.Success(reportResponse.data)
            } else {
                ApiResult.Error(reportResponse.error ?: "Failed to get monthly report")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}