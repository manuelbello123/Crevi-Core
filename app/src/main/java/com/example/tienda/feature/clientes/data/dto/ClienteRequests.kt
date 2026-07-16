package com.example.tienda.feature.clientes.data.dto

import kotlinx.serialization.Serializable

/** POST /clientes (gerente ignora sucursalId/gerenteId; se asigna a sí mismo). */
@Serializable
data class CrearClienteRequest(
    val nombreCompleto: String,
    val direccion: String? = null,
    val telefono: String? = null,
    val sucursalId: Long? = null,
    val gerenteId: Long? = null,
    val accesoApp: Boolean = false,
    val password: String? = null,
)

/** PUT /clientes/{id} (datos de contacto). */
@Serializable
data class ActualizarClienteRequest(
    val nombreCompleto: String,
    val direccion: String? = null,
    val telefono: String? = null,
)

/** PATCH /clientes/{id}/asignacion (SOLO admin). */
@Serializable
data class AsignarClienteRequest(
    val sucursalId: Long,
    val gerenteId: Long,
)

/** PATCH /clientes/{id}/acceso-app. */
@Serializable
data class AccesoAppRequest(
    val accesoApp: Boolean,
    val password: String? = null,
)

/** PATCH .../activo (compartido por clientes y usuarios). */
@Serializable
data class CambiarEstadoRequest(
    val activo: Boolean,
)
