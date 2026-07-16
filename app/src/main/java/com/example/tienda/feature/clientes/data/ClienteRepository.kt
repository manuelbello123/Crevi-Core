package com.example.tienda.feature.clientes.data

import com.example.tienda.feature.clientes.domain.Cliente
import com.example.tienda.core.network.NetworkResult

interface ClienteRepository {

    suspend fun listar(
        q: String?,
        pagina: Int,
        tamano: Int,
        sucursalId: Long?,
    ): NetworkResult<List<Cliente>>

    suspend fun obtener(id: Long): NetworkResult<Cliente>

    suspend fun crear(
        nombreCompleto: String,
        direccion: String?,
        telefono: String?,
        sucursalId: Long?,
        gerenteId: Long?,
        accesoApp: Boolean,
        password: String?,
    ): NetworkResult<Cliente>

    suspend fun actualizar(
        id: Long,
        nombreCompleto: String,
        direccion: String?,
        telefono: String?,
    ): NetworkResult<Cliente>

    suspend fun cambiarActivo(id: Long, activo: Boolean): NetworkResult<Unit>

    /** SOLO admin. */
    suspend fun reasignar(id: Long, sucursalId: Long, gerenteId: Long): NetworkResult<Cliente>

    suspend fun cambiarAccesoApp(id: Long, accesoApp: Boolean, password: String?): NetworkResult<Unit>
}
