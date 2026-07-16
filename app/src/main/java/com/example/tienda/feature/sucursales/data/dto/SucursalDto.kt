package com.example.tienda.feature.sucursales.data.dto

import com.example.tienda.feature.sucursales.domain.Sucursal
import kotlinx.serialization.Serializable

/** Respuesta de GET /sucursales. */
@Serializable
data class SucursalDto(
    val id: Long,
    val nombre: String,
    val direccion: String? = null,
    val telefono: String? = null,
    val esMatriz: Boolean = false,
    val creadaEn: String? = null,
)

fun SucursalDto.toDomain(): Sucursal = Sucursal(
    id = id,
    nombre = nombre,
    direccion = direccion,
    telefono = telefono,
    esMatriz = esMatriz,
)
