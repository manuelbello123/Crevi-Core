package com.example.tienda.feature.configuraciones.data

import com.example.tienda.feature.configuraciones.data.dto.ActualizarConfigLoginClientesRequest
import com.example.tienda.feature.configuraciones.data.dto.ConfigLoginClientesDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ConfigApiImpl(
    private val client: HttpClient,
) : ConfigApi {

    override suspend fun obtenerLoginClientes(): ConfigLoginClientesDto =
        client.get("/config/login-clientes").body()

    override suspend fun actualizarLoginClientes(request: ActualizarConfigLoginClientesRequest): ConfigLoginClientesDto =
        client.put("/config/login-clientes") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
