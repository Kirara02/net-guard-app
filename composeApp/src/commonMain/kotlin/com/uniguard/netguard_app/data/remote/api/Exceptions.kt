package com.uniguard.netguard_app.data.remote.api

import io.ktor.client.statement.*

class TokenExpiredException(message: String) : Exception(message)
class ClientException(val response: HttpResponse, cachedCause: String) : Exception(cachedCause)
class ServerException(val response: HttpResponse, cachedCause: String) : Exception(cachedCause)