package com.example.tienda.feature.login.data
import com.example.tienda.feature.login.data.dto.LoginRequest
import com.example.tienda.feature.login.data.dto.LoginResponseDto
import com.example.tienda.feature.login.data.dto.LogoutRequest
import com.example.tienda.feature.login.data.dto.RefreshRequest

interface LoginApi {

    /**
     * POST /auth/login. No atrapa errores: un 4xx/5xx se convierte en
     * [com.example.tienda.core.network.error.NetworkError] dentro del HttpClient
     * (validateResponse) y el repositorio lo normaliza con safeApiCall.
     */
    suspend fun login(request: LoginRequest): LoginResponseDto

    /** POST /auth/refresh (público): canjea el refresh token por un par nuevo (access + refresh rotado). */
    suspend fun refresh(request: RefreshRequest): LoginResponseDto

    /** POST /auth/logout (autenticado): revoca el refresh token en el servidor. */
    suspend fun logout(request: LogoutRequest)
}
