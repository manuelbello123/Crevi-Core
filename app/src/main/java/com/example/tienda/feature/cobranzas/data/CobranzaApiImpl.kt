package com.example.tienda.feature.cobranzas.data
import com.example.tienda.feature.cobranzas.data.dto.*
import com.example.tienda.feature.cobranzas.domain.*

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class CobranzaApiImpl(
    private val client: HttpClient,
) : CobranzaApi {

    override suspend fun porCobrar(sucursalId: Long?): List<CuentaPorCobrarDto> =
        client.get("/cuentas/por-cobrar") {
            parameter("sucursalId", sucursalId)
        }.body()

    override suspend fun cuentasDeCliente(clienteId: Long): List<CuentaDto> =
        client.get("/clientes/$clienteId/cuentas").body()

    override suspend fun abrirCuenta(request: AbrirCuentaRequest): CuentaDto =
        client.post("/cuentas") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun editarCuenta(id: Long, request: ActualizarCuentaRequest): CuentaDto =
        client.patch("/cuentas/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun historial(cuentaId: Long): HistorialCuentaDto =
        client.get("/cuentas/$cuentaId/historial").body()

    override suspend fun abono(request: AbonoRequest): MovimientoDto =
        client.post("/movimientos/abono") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun anticipo(request: AbonoRequest): MovimientoDto =
        client.post("/movimientos/anticipo") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun devolucion(request: DevolucionRequest): MovimientoDto =
        client.post("/movimientos/devolucion") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun editarMovimiento(id: Long, request: EditarMovimientoRequest): MovimientoDto =
        client.patch("/movimientos/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun eliminarMovimiento(id: Long) {
        client.delete("/movimientos/$id")
    }

    override suspend fun movimientosRecientes(sucursalId: Long?, pagina: Int, tamano: Int): List<MovimientoRecienteDto> =
        client.get("/movimientos") {
            parameter("sucursalId", sucursalId)
            parameter("pagina", pagina)
            parameter("tamano", tamano)
        }.body()
}
