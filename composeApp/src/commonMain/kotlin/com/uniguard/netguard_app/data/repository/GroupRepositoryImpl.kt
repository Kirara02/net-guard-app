package com.uniguard.netguard_app.data.repository

import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Group
import com.uniguard.netguard_app.domain.model.GroupRequest
import com.uniguard.netguard_app.domain.repository.GroupRepository

class GroupRepositoryImpl (
    private val api: NetGuardApi,
    private val prefs: AppPreferences
) : GroupRepository {

    override suspend fun getGroups(): ApiResult<List<Group>> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.getGroups(token)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun getGroupById(id: String): ApiResult<Group> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.getGroupById(token, id)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun createGroup(request: GroupRequest): ApiResult<Group> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.createGroup(token, request)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun updateGroup(
        id: String,
        request: GroupRequest
    ): ApiResult<Group> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.updateGroup(token, id, request)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun deleteById(id: String): ApiResult<String> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.deleteGroupById(token, id)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }
}