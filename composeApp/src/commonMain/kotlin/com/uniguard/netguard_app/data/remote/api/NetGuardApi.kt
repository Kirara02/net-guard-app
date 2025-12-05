package com.uniguard.netguard_app.data.remote.api

import com.uniguard.netguard_app.BuildKonfig
import com.uniguard.netguard_app.data.remote.dto.ApiResponse
import com.uniguard.netguard_app.domain.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class NetGuardApi(
    httpClient: HttpClient,
    private val baseUrl: String = BuildKonfig.BASEURL
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

    suspend fun logout(token: String) : ApiResult<String> {
        return try {
            val response = client.post("$baseUrl/auth/logout") {
                header("Authorization", "Bearer $token")
            }

            val groupResponse: LogoutResponse = response.body()
            if (groupResponse.success) {
                ApiResult.Success(groupResponse.message)
            } else {
                ApiResult.Error(groupResponse.error ?: "Failed to logout")
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

    suspend fun updateProfile(token: String, request: UpdateProfileRequest): ApiResult<User> {
        return try {
            val response = client.put("$baseUrl/auth/profile") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val updateProfileResponse: UpdateProfileResponse = response.body()
            if (updateProfileResponse.success && updateProfileResponse.data != null) {
                ApiResult.Success(updateProfileResponse.data)
            } else {
                ApiResult.Error(updateProfileResponse.error ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun changePassword(token: String, request: ChangePasswordRequest): ApiResult<Unit> {
        return try {
            val response = client.put("$baseUrl/auth/change-password") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse: ApiResponse<Unit> = response.body()
            if (apiResponse.success) {
                ApiResult.Success(Unit)
            } else {
                // For auth endpoints, try to parse error response
                try {
                    val errorResponse: ErrorResponse = response.body()
                    ApiResult.Error(errorResponse.error)
                } catch (e: Exception) {
                    ApiResult.Error(apiResponse.error ?: "Failed to change password")
                }
            }
        } catch (e: ClientException) {
            // Handle 400 for validation errors - parse error from the exception response
            try {
                val errorResponse: ErrorResponse = e.response.body()
                ApiResult.Error(errorResponse.error)
            } catch (parseException: Exception) {
                ApiResult.Error("Failed to change password")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    // Group Management Endpoints
    suspend fun getGroups(token: String) : ApiResult<List<Group>> {
        return try {
            val response = client.get("$baseUrl/admin/groups") {
                header("Authorization", "Bearer $token")
            }
            val groupsResponse: GroupsResponse = response.body()
            if(groupsResponse.success) {
                ApiResult.Success(groupsResponse.data)
            } else {
                ApiResult.Error(groupsResponse.error ?: "Failed to get users")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network Error")
        }
    }

    suspend fun getGroupById(token: String, id: String) : ApiResult<Group> {
        return try {
            val response = client.get("$baseUrl/admin/group/$id") {
                header("Authorization", "Bearer $token")
            }
            val groupResponse: GroupResponse = response.body()
            if(groupResponse.success && groupResponse.data != null) {
                ApiResult.Success(groupResponse.data)
            } else {
                ApiResult.Error(groupResponse.error ?: "Failed to get group")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network Error")
        }
    }

    suspend fun createGroup(token: String, request: GroupRequest) : ApiResult<Group> {
        return try {
            val response = client.post("$baseUrl/admin/groups") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val groupResponse: GroupResponse = response.body()
            if(groupResponse.success && groupResponse.data != null) {
                ApiResult.Success(groupResponse.data)
            } else {
                ApiResult.Error(groupResponse.error ?: "Failed to create group")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network Error")
        }
    }

    suspend fun updateGroup(token: String, id: String, request: GroupRequest) : ApiResult<Group>{
        return try {
            val response = client.put("$baseUrl/admin/groups/$id") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val groupResponse: GroupResponse = response.body()
            if(groupResponse.success && groupResponse.data != null) {
                ApiResult.Success(groupResponse.data)
            } else {
                ApiResult.Error(groupResponse.error ?: "Failed to create group")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network Error")
        }
    }

    suspend fun deleteGroupById(token: String, id: String) : ApiResult<String> {
        return try {
            val response = client.delete("$baseUrl/admin/groups/$id") {
                header("Authorization", "Bearer $token")
            }
            val groupResponse: UserResponse = response.body()
            if (groupResponse.success) {
                ApiResult.Success(groupResponse.message!!)
            } else {
                ApiResult.Error(groupResponse.error ?: "Failed to create user")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network Error")
        }
    }

    // User Management Endpoints
    suspend fun getUsers(token: String) : ApiResult<List<User>> {
        return try {
            val response = client.get("$baseUrl/admin/users") {
                header("Authorization", "Bearer $token")
            }
            val usersResponse: UsersResponse = response.body()
            if(usersResponse.success) {
                ApiResult.Success(usersResponse.data)
            } else {
                ApiResult.Error(usersResponse.error ?: "Failed to get users")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getUserById(token: String, userId: String): ApiResult<User> {
        return try {
            val response = client.get("$baseUrl/admin/users/$userId") {
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

    suspend fun createUser(token: String, request: CreateUserRequest): ApiResult<User> {
        return try {
            val response = client.post("$baseUrl/admin/users") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val userResponse: UserResponse = response.body()
            if (userResponse.success && userResponse.data != null) {
                ApiResult.Success(userResponse.data)
            } else {
                ApiResult.Error(userResponse.error ?: "Failed to create user")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateUser(token: String, userId: String, request: UpdateUserRequest): ApiResult<User> {
        return try {
            val response = client.put("$baseUrl/admin/users/$userId") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val userResponse: UserResponse = response.body()
            if (userResponse.success && userResponse.data != null) {
                ApiResult.Success(userResponse.data)
            } else {
                ApiResult.Error(userResponse.error ?: "Failed to create user")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteUserById(token: String, userId: String): ApiResult<String> {
        return try {
            val response = client.delete("$baseUrl/admin/users/$userId") {
                header("Authorization", "Bearer $token")
            }
            val userResponse: UserResponse = response.body()
            if (userResponse.success) {
                ApiResult.Success(userResponse.message!!)
            } else {
                ApiResult.Error(userResponse.error ?: "Failed to create user")
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

    suspend fun createServer(token: String, request: ServerRequest): ApiResult<Server> {
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

    suspend fun updateServer(token: String, serverId: String, request: ServerRequest): ApiResult<Server> {
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
    suspend fun getHistories(token: String, serverId: String? = null, limit: Int? = 50): ApiResult<List<History>> {
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

    // Report Management Endpoints
    suspend fun getReports(token: String, params: ReportParams): ApiResult<List<Report>> {
        return try {
            val response = client.get("$baseUrl/report") {
                header("Authorization", "Bearer $token")
                params.status?.let { parameter("status", it.name) }
                params.serverName?.let { parameter("server_name", it) }
                params.limit?.let { parameter("limit", it) }
                params.startDate?.let { parameter("start_date", it) }
                params.endDate?.let { parameter("end_date", it) }
            }
            val reportsResponse: ReportsResponse = response.body()
            if (reportsResponse.success) {
                ApiResult.Success(reportsResponse.data)
            } else {
                ApiResult.Error(reportsResponse.error ?: "Failed to get reports")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }


    suspend fun exportReports(token: String, params: ReportParams): ApiResult<ByteArray> {
        return try {
            val response = client.get("$baseUrl/report/export") {
                header("Authorization", "Bearer $token")
                params.status?.let { parameter("status", it.name) }
                params.serverName?.let { parameter("server_name", it) }
                params.limit?.let { parameter("limit", it) }
                params.startDate?.let { parameter("start_date", it) }
                params.endDate?.let { parameter("end_date", it) }
                params.format?.let { parameter("format", it.value) }
            }
            val bytes = response.body<ByteArray>()
            ApiResult.Success(bytes)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    // Dashboard Admin Endpoints
    suspend fun getAdminDashboard(token: String): ApiResult<Dashboard> {
        return try {
            val response = client.get("$baseUrl/admin/dashboard") {
                header("Authorization", "Bearer $token")
            }

            val result: DashboardResponse = response.body()
            if(result.success && result.data != null) {
                ApiResult.Success(result.data)
            } else {
                ApiResult.Error(result.error ?: "Failed to get admin dashboard data")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}