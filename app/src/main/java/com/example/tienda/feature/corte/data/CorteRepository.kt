package com.example.tienda.feature.corte.data
import com.example.tienda.feature.corte.domain.CorteSucursal
import com.example.tienda.feature.corte.domain.CorteUsuario

import com.example.tienda.core.network.NetworkResult

interface CorteRepository {

    suspend fun porSucursal(anio: Int?, semana: Int?, sucursalId: Long?): NetworkResult<List<CorteSucursal>>

    suspend fun porUsuario(anio: Int?, semana: Int?, sucursalId: Long?): NetworkResult<List<CorteUsuario>>

    /** Devuelve los bytes del .xlsx del corte semanal. */
    suspend fun excel(anio: Int?, semana: Int?, sucursalId: Long?): NetworkResult<ByteArray>
}
