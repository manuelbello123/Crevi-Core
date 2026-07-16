package com.example.tienda.feature.configuraciones.data.dto

import kotlinx.serialization.Serializable

/** GET/PUT /config/login-clientes. */
@Serializable
data class ConfigLoginClientesDto(
    val deshabilitado: Boolean,
)

/** PUT /config/login-clientes. */
@Serializable
data class ActualizarConfigLoginClientesRequest(
    val deshabilitado: Boolean,
)
