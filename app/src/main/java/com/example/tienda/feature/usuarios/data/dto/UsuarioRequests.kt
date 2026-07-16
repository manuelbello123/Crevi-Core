package com.example.tienda.feature.usuarios.data.dto

import kotlinx.serialization.Serializable

/** POST /usuarios. `rol` se manda en minúsculas: "administrador" | "gerente". */
@Serializable
data class CrearUsuarioRequest(
    val nombre: String,
    val usuario: String,
    val password: String,
    val rol: String,
    val sucursalId: Long? = null,
)

/** PUT /usuarios/{id} (nombre y usuario). */
@Serializable
data class ActualizarUsuarioRequest(
    val nombre: String,
    val usuario: String,
)

/** PATCH /usuarios/{id}/sucursal (reasignar gerente). */
@Serializable
data class AsignarSucursalRequest(
    val sucursalId: Long,
)

/** PATCH /usuarios/{id}/password (admin resetea). */
@Serializable
data class ResetPasswordRequest(
    val passwordNueva: String,
)

/** PATCH /usuarios/{id}/activo. */
@Serializable
data class CambiarEstadoRequest(
    val activo: Boolean,
)
