package com.uniguard.netguard_app.domain.repository

import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Group
import com.uniguard.netguard_app.domain.model.GroupRequest

interface GroupRepository {
    suspend fun getGroups() : ApiResult<List<Group>>
    suspend fun getGroupById(id: String) : ApiResult<Group>
    suspend fun createGroup(request: GroupRequest) : ApiResult<Group>
    suspend fun updateGroup(id: String, request: GroupRequest) : ApiResult<Group>
    suspend fun deleteById(id: String) : ApiResult<String>
}