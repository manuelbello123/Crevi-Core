package com.example.tienda.feature.login.data.dto

import kotlinx.serialization.Serializable

/** Body de POST /auth/refresh → canjea el refresh token por un par nuevo (access + refresh rotado). */
@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

/** Body de POST /auth/logout → revoca el refresh token en el servidor (opcional). */
@Serializable
data class LogoutRequest(
    val refreshToken: String? = null,
)
