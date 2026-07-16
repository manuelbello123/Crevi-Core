package com.example.tienda.feature.clientes.data
import com.example.tienda.feature.clientes.data.dto.*

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ClienteApiImpl(
    private val client: HttpClient,
) : ClienteApi {

    override suspend fun listar(
        q: String?,
        pagina: Int,
        tamano: Int,
        sucursalId: Long?,
    ): PaginaClienteDto =
        client.get("/clientes") {
            parameter("pagina", pagina)
            parameter("tamano", tamano)
            parameter("sucursalId", sucursalId)
            parameter("q", q?.ifBlank { null })
        }.body()

    override suspend fun obtener(id: Long): ClienteDto =
        client.get("/clientes/$id").body()

    override suspend fun crear(request: CrearClienteRequest): ClienteDto =
        client.post("/clientes") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun actualizar(id: Long, request: ActualizarClienteRequest): ClienteDto =
        client.put("/clientes/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun cambiarActivo(id: Long, request: CambiarEstadoRequest) {
        client.patch("/clientes/$id/activo") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun reasignar(id: Long, request: AsignarClienteRequest): ClienteDto =
        client.patch("/clientes/$id/asignacion") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun cambiarAccesoApp(id: Long, request: AccesoAppRequest) {
        client.patch("/clientes/$id/acceso-app") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
