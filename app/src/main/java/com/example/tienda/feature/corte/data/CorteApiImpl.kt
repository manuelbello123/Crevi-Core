package com.example.tienda.feature.corte.data
import com.example.tienda.feature.corte.data.dto.CorteSucursalDto
import com.example.tienda.feature.corte.data.dto.CorteUsuarioDto

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.readRawBytes

class CorteApiImpl(
    private val client: HttpClient,
) : CorteApi {

    override suspend fun porSucursal(anio: Int?, semana: Int?, sucursalId: Long?): List<CorteSucursalDto> =
        client.get("/cortes/semanal") {
            parameter("anio", anio)
            parameter("semana", semana)
            parameter("sucursalId", sucursalId)
        }.body()

    override suspend fun porUsuario(anio: Int?, semana: Int?, sucursalId: Long?): List<CorteUsuarioDto> =
        client.get("/cortes/por-usuario") {
            parameter("anio", anio)
            parameter("semana", semana)
            parameter("sucursalId", sucursalId)
        }.body()

    override suspend fun excel(anio: Int?, semana: Int?, sucursalId: Long?): ByteArray =
        client.get("/cortes/semanal/excel") {
            parameter("anio", anio)
            parameter("semana", semana)
            parameter("sucursalId", sucursalId)
        }.readRawBytes()
}
