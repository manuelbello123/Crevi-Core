package com.example.tienda.feature.clientes.data
import com.example.tienda.feature.clientes.data.dto.*

import com.example.tienda.feature.clientes.domain.Cliente
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.safeApiCall

class ClienteRepositoryImpl(
    private val api: ClienteApi,
) : ClienteRepository {

    override suspend fun listar(
        q: String?,
        pagina: Int,
        tamano: Int,
        sucursalId: Long?,
    ): NetworkResult<List<Cliente>> =
        safeApiCall { api.listar(q, pagina, tamano, sucursalId).datos.map { it.toDomain() } }

    override suspend fun obtener(id: Long): NetworkResult<Cliente> =
        safeApiCall { api.obtener(id).toDomain() }

    override suspend fun crear(
        nombreCompleto: String,
        direccion: String?,
        telefono: String?,
        sucursalId: Long?,
        gerenteId: Long?,
        accesoApp: Boolean,
        password: String?,
    ): NetworkResult<Cliente> =
        safeApiCall {
            api.crear(
                CrearClienteRequest(
                    nombreCompleto = nombreCompleto.trim(),
                    direccion = direccion?.trim()?.ifBlank { null },
                    telefono = telefono?.trim()?.ifBlank { null },
                    sucursalId = sucursalId,
                    gerenteId = gerenteId,
                    accesoApp = accesoApp,
                    password = password?.ifBlank { null },
                )
            ).toDomain()
        }

    override suspend fun actualizar(
        id: Long,
        nombreCompleto: String,
        direccion: String?,
        telefono: String?,
    ): NetworkResult<Cliente> =
        safeApiCall {
            api.actualizar(
                id,
                ActualizarClienteRequest(
                    nombreCompleto = nombreCompleto.trim(),
                    direccion = direccion?.trim()?.ifBlank { null },
                    telefono = telefono?.trim()?.ifBlank { null },
                )
            ).toDomain()
        }

    override suspend fun cambiarActivo(id: Long, activo: Boolean): NetworkResult<Unit> =
        safeApiCall { api.cambiarActivo(id, CambiarEstadoRequest(activo)) }

    override suspend fun reasignar(id: Long, sucursalId: Long, gerenteId: Long): NetworkResult<Cliente> =
        safeApiCall { api.reasignar(id, AsignarClienteRequest(sucursalId, gerenteId)).toDomain() }

    override suspend fun cambiarAccesoApp(id: Long, accesoApp: Boolean, password: String?): NetworkResult<Unit> =
        safeApiCall { api.cambiarAccesoApp(id, AccesoAppRequest(accesoApp, password?.ifBlank { null })) }
}
