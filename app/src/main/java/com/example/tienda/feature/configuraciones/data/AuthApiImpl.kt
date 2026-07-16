package com.example.tienda.feature.configuraciones.data

import com.example.tienda.feature.configuraciones.data.dto.CambiarPasswordRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiImpl(
    private val client: HttpClient,
) : AuthApi {

    override suspend fun cambiarPassword(request: CambiarPasswordRequest) {
        client.put("/auth/me/password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
