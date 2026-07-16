package com.example.tienda.feature.login.data.dto

import kotlinx.serialization.Serializable

/**
 * Respuesta de POST /auth/login y POST /auth/refresh.
 * `token` = access token (vida corta, 1h). `refreshToken` = token opaco de larga
 * vida para re-autenticar sin la contraseña (se guarda para el ingreso biométrico).
 * `rol` llega en MAYÚSCULAS ("ADMINISTRADOR"|"GERENTE"); se tipa con UserRole en el mapper.
 */
@Serializable
data class LoginResponseDto(
    val token: String,
    val refreshToken: String,
    val id: Long,
    val nombre: String,
    val rol: String,
    val sucursalId: Long? = null,
)
