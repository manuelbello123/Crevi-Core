package com.example.tienda.feature.sucursales.data.dto

import kotlinx.serialization.Serializable

/** POST /sucursales. */
@Serializable
data class CrearSucursalRequest(
    val nombre: String,
    val direccion: String? = null,
    val telefono: String? = null,
)

/** PUT /sucursales/{id}. */
@Serializable
data class ActualizarSucursalRequest(
    val nombre: String,
    val direccion: String? = null,
    val telefono: String? = null,
)
