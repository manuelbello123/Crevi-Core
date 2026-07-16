package com.example.tienda.feature.configuraciones.data.dto

import kotlinx.serialization.Serializable

/** PUT /auth/me/password. */
@Serializable
data class CambiarPasswordRequest(
    val passwordActual: String,
    val passwordNueva: String,
)
