package com.example.tienda.feature.login.data

import com.example.tienda.core.session.SessionData
import com.example.tienda.core.network.NetworkResult

/**
 * Resultado de una autenticación exitosa: la sesión (ya persistida) y el
 * refresh token rotado para guardar (ingreso biométrico sin contraseña).
 */
data class AuthResult(
    val session: SessionData,
    val refreshToken: String,
)

interface LoginRepository {

    /**
     * Login con contraseña. Nunca lanza: devuelve [NetworkResult].
     * En caso de éxito la sesión ya quedó persistida (DataStore + StateFlow).
     */
    suspend fun login(usuario: String, password: String): NetworkResult<AuthResult>

    /**
     * Canjea un refresh token por una sesión nueva (POST /auth/refresh). El
     * backend rota el token: el [AuthResult.refreshToken] devuelto es el NUEVO.
     */
    suspend fun refresh(refreshToken: String): NetworkResult<AuthResult>

    /** Revoca el refresh token en el servidor (best-effort; ignora errores de red). */
    suspend fun logout(refreshToken: String?)
}
