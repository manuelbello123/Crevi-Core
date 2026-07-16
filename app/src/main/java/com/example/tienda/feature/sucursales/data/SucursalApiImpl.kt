package com.example.tienda.feature.sucursales.data

import com.example.tienda.feature.sucursales.data.dto.ActualizarSucursalRequest
import com.example.tienda.feature.sucursales.data.dto.CrearSucursalRequest
import com.example.tienda.feature.sucursales.data.dto.SucursalDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SucursalApiImpl(
    private val client: HttpClient,
) : SucursalApi {

    override suspend fun listar(): List<SucursalDto> =
        client.get("/sucursales").body()

    override suspend fun crear(request: CrearSucursalRequest): SucursalDto =
        client.post("/sucursales") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun editar(id: Long, request: ActualizarSucursalRequest): SucursalDto =
        client.put("/sucursales/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
