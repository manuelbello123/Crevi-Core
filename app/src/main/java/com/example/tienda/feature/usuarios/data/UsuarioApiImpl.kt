package com.example.tienda.feature.usuarios.data
import com.example.tienda.feature.usuarios.data.dto.*

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

class UsuarioApiImpl(
    private val client: HttpClient,
) : UsuarioApi {

    override suspend fun listar(q: String?, pagina: Int, tamano: Int): PaginaUsuarioDto =
        client.get("/usuarios") {
            parameter("pagina", pagina)
            parameter("tamano", tamano)
            parameter("q", q?.ifBlank { null })
        }.body()

    override suspend fun obtener(id: Long): UsuarioDto =
        client.get("/usuarios/$id").body()

    override suspend fun crear(request: CrearUsuarioRequest): UsuarioDto =
        client.post("/usuarios") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun actualizar(id: Long, request: ActualizarUsuarioRequest): UsuarioDto =
        client.put("/usuarios/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun cambiarActivo(id: Long, request: CambiarEstadoRequest) {
        client.patch("/usuarios/$id/activo") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun reasignarSucursal(id: Long, request: AsignarSucursalRequest): UsuarioDto =
        client.patch("/usuarios/$id/sucursal") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun resetPassword(id: Long, request: ResetPasswordRequest) {
        client.patch("/usuarios/$id/password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
