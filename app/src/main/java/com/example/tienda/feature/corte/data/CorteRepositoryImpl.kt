package com.example.tienda.feature.corte.data
import com.example.tienda.feature.corte.data.dto.toDomain
import com.example.tienda.feature.corte.domain.CorteSucursal
import com.example.tienda.feature.corte.domain.CorteUsuario

import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.safeApiCall

class CorteRepositoryImpl(
    private val api: CorteApi,
) : CorteRepository {

    override suspend fun porSucursal(anio: Int?, semana: Int?, sucursalId: Long?): NetworkResult<List<CorteSucursal>> =
        safeApiCall { api.porSucursal(anio, semana, sucursalId).map { it.toDomain() } }

    override suspend fun porUsuario(anio: Int?, semana: Int?, sucursalId: Long?): NetworkResult<List<CorteUsuario>> =
        safeApiCall { api.porUsuario(anio, semana, sucursalId).map { it.toDomain() } }

    override suspend fun excel(anio: Int?, semana: Int?, sucursalId: Long?): NetworkResult<ByteArray> =
        safeApiCall { api.excel(anio, semana, sucursalId) }
}
