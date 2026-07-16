package com.example.tienda.feature.login.data
import com.example.tienda.feature.login.data.dto.LoginRequest
import com.example.tienda.feature.login.data.dto.LoginResponseDto
import com.example.tienda.feature.login.data.dto.LogoutRequest
import com.example.tienda.feature.login.data.dto.RefreshRequest

import com.example.tienda.core.enums.UserRole
import com.example.tienda.core.session.SessionData
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.error.NetworkError
import com.example.tienda.core.network.safeApiCall
import com.example.tienda.core.session.SessionManager

class LoginRepositoryImpl(
    private val api: LoginApi,
    private val sessionManager: SessionManager,
) : LoginRepository {

    override suspend fun login(usuario: String, password: String): NetworkResult<AuthResult> {
        // Validación local: evita un round-trip inútil al servidor.
        if (usuario.isBlank() || password.isBlank()) {
            return NetworkResult.Error(NetworkError.EmptyFields)
        }
        return when (val result = safeApiCall { api.login(LoginRequest(usuario.trim(), password)) }) {
            is NetworkResult.Success -> NetworkResult.Success(persistir(result.data))
            is NetworkResult.Error -> NetworkResult.Error(result.error)
        }
    }

    override suspend fun refresh(refreshToken: String): NetworkResult<AuthResult> =
        when (val result = safeApiCall { api.refresh(RefreshRequest(refreshToken)) }) {
            is NetworkResult.Success -> NetworkResult.Success(persistir(result.data))
            is NetworkResult.Error -> NetworkResult.Error(result.error)
        }

    override suspend fun logout(refreshToken: String?) {
        // Best-effort: si no hay red o falla, seguimos cerrando sesión localmente.
        safeApiCall { api.logout(LogoutRequest(refreshToken)) }
    }

    /** Persiste la sesión y devuelve el AuthResult (sesión + refresh token). */
    private suspend fun persistir(dto: LoginResponseDto): AuthResult {
        val session = dto.toSessionData()
        sessionManager.startSession(session) // persiste + emite en sessionFlow
        return AuthResult(session, dto.refreshToken)
    }
}

/** LoginResponseDto → modelo de sesión persistible. */
private fun LoginResponseDto.toSessionData(): SessionData =
    SessionData(
        token = token,
        refreshToken = refreshToken,
        id = id,
        nombre = nombre,
        rol = UserRole.from(rol),
        sucursalId = sucursalId,
    )
