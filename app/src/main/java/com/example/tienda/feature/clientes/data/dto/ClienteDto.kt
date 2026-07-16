package com.example.tienda.feature.clientes.data.dto

import com.example.tienda.feature.clientes.domain.Cliente
import kotlinx.serialization.Serializable

/** Respuesta de GET /clientes/{id} y elementos de la página. */
@Serializable
data class ClienteDto(
    val id: Long,
    val numeroCliente: String,
    val sucursalId: Long? = null,
    val gerenteId: Long? = null,
    val nombreCompleto: String,
    val direccion: String? = null,
    val telefono: String? = null,
    val accesoApp: Boolean = false,
    val activo: Boolean = true,
    val creadoEn: String? = null,
)

/** Página paginada de GET /clientes. */
@Serializable
data class PaginaClienteDto(
    val datos: List<ClienteDto> = emptyList(),
    val pagina: Int = 1,
    val tamano: Int = 20,
    val total: Long = 0,
)

fun ClienteDto.toDomain(): Cliente = Cliente(
    id = id,
    numeroCliente = numeroCliente,
    nombreCompleto = nombreCompleto,
    telefono = telefono,
    direccion = direccion,
    sucursalId = sucursalId,
    gerenteId = gerenteId,
    accesoApp = accesoApp,
    activo = activo,
)
