package com.example.tienda.feature.sucursales.data

import com.example.tienda.feature.sucursales.domain.Sucursal
import com.example.tienda.core.network.NetworkResult

interface SucursalRepository {
    suspend fun listar(): NetworkResult<List<Sucursal>>

    suspend fun crear(nombre: String, direccion: String?, telefono: String?): NetworkResult<Sucursal>

    suspend fun editar(id: Long, nombre: String, direccion: String?, telefono: String?): NetworkResult<Sucursal>
}
