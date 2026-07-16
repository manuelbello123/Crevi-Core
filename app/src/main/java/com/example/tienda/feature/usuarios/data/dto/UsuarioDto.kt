package com.example.tienda.feature.usuarios.data.dto

import com.example.tienda.core.enums.UserRole
import com.example.tienda.feature.usuarios.domain.Usuario
import kotlinx.serialization.Serializable

/** Elemento de GET /usuarios. */
@Serializable
data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val usuario: String,
    val rol: String,
    val sucursalId: Long? = null,
    val activo: Boolean = true,
    val creadoEn: String? = null,
)

@Serializable
data class PaginaUsuarioDto(
    val datos: List<UsuarioDto> = emptyList(),
    val pagina: Int = 1,
    val tamano: Int = 20,
    val total: Long = 0,
)

fun UsuarioDto.toDomain(): Usuario = Usuario(
    id = id,
    nombre = nombre,
    usuario = usuario,
    rol = UserRole.from(rol),
    sucursalId = sucursalId,
    activo = activo,
)
