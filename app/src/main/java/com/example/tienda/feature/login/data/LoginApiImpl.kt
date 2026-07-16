package com.example.tienda.feature.login.data
import com.example.tienda.feature.login.data.dto.LoginRequest
import com.example.tienda.feature.login.data.dto.LoginResponseDto
import com.example.tienda.feature.login.data.dto.LogoutRequest
import com.example.tienda.feature.login.data.dto.RefreshRequest

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Implementación real de [LoginApi] sobre el HttpClient compartido, que ya trae
 * BASE_URL, JSON (ignoreUnknownKeys) y validateResponse (4xx/5xx → NetworkError).
 */
class LoginApiImpl(
    private val client: HttpClient,
) : LoginApi {

    override suspend fun login(request: LoginRequest): LoginResponseDto =
        client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun refresh(request: RefreshRequest): LoginResponseDto =
        client.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun logout(request: LogoutRequest) {
        client.post("/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
