package com.example.tienda.core.session

import com.example.tienda.core.enums.UserRole

/**
 * Sesión activa del operador. Se arma con la respuesta de POST /auth/login
 * { token, id, nombre, rol, sucursalId }. `usuario` se completa luego con
 * GET /auth/me (la respuesta de login no lo trae).
 */
data class SessionData(
    val token: String,
    val refreshToken: String,
    val id: Long,
    val nombre: String,
    val rol: UserRole,
    val sucursalId: Long?,
    val usuario: String? = null,
) {
    val esAdministrador: Boolean get() = rol == UserRole.ADMINISTRADOR
}
