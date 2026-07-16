package com.example.tienda.feature.corte.data
import com.example.tienda.feature.corte.data.dto.CorteSucursalDto
import com.example.tienda.feature.corte.data.dto.CorteUsuarioDto

interface CorteApi {

    /** GET /cortes/semanal (por sucursal). anio/semana null = semana ISO actual. */
    suspend fun porSucursal(anio: Int?, semana: Int?, sucursalId: Long?): List<CorteSucursalDto>

    /** GET /cortes/por-usuario (desglose por operador). */
    suspend fun porUsuario(anio: Int?, semana: Int?, sucursalId: Long?): List<CorteUsuarioDto>

    /** GET /cortes/semanal/excel → archivo .xlsx (bytes). */
    suspend fun excel(anio: Int?, semana: Int?, sucursalId: Long?): ByteArray
}
