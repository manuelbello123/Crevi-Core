package com.example.tienda.feature.ventas.data
import com.example.tienda.feature.ventas.domain.*
import com.example.tienda.feature.ventas.data.dto.*

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class VentaApiImpl(
    private val client: HttpClient,
) : VentaApi {

    override suspend fun listar(
        sucursalId: Long?,
        fechaDesde: String?,
        fechaHasta: String?,
        estado: String?,
        pagina: Int,
        tamano: Int,
    ): PaginaVentaResumenDto =
        client.get("/ventas") {
            parameter("sucursalId", sucursalId)
            parameter("fechaDesde", fechaDesde)
            parameter("fechaHasta", fechaHasta)
            parameter("estado", estado)
            parameter("pagina", pagina)
            parameter("tamano", tamano)
        }.body()

    override suspend fun pendientes(): List<VentaPendienteDto> =
        client.get("/ventas/pendientes").body()

    override suspend fun obtener(id: Long): VentaDto =
        client.get("/ventas/$id").body()

    override suspend fun ventasDeCuenta(cuentaId: Long): List<VentaDto> =
        client.get("/cuentas/$cuentaId/ventas").body()

    override suspend fun crear(request: RegistrarVentaRequest): VentaDto =
        client.post("/ventas") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun editarRenglones(id: Long, request: EditarRenglonesRequest): VentaDto =
        client.put("/ventas/$id/renglones") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun consolidar(id: Long): VentaDto =
        client.post("/ventas/$id/consolidar").body()

    override suspend fun cancelar(id: Long): VentaDto =
        client.post("/ventas/$id/cancelar").body()
}
