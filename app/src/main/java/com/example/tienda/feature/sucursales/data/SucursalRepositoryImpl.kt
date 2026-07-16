package com.example.tienda.feature.sucursales.data

import com.example.tienda.feature.sucursales.data.dto.ActualizarSucursalRequest
import com.example.tienda.feature.sucursales.data.dto.CrearSucursalRequest
import com.example.tienda.feature.sucursales.data.dto.toDomain
import com.example.tienda.feature.sucursales.domain.Sucursal
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.safeApiCall

class SucursalRepositoryImpl(
    private val api: SucursalApi,
) : SucursalRepository {

    override suspend fun listar(): NetworkResult<List<Sucursal>> =
        safeApiCall { api.listar().map { it.toDomain() } }

    override suspend fun crear(nombre: String, direccion: String?, telefono: String?): NetworkResult<Sucursal> =
        safeApiCall {
            api.crear(CrearSucursalRequest(nombre.trim(), direccion?.ifBlank { null }, telefono?.ifBlank { null })).toDomain()
        }

    override suspend fun editar(id: Long, nombre: String, direccion: String?, telefono: String?): NetworkResult<Sucursal> =
        safeApiCall {
            api.editar(id, ActualizarSucursalRequest(nombre.trim(), direccion?.ifBlank { null }, telefono?.ifBlank { null })).toDomain()
        }
}
