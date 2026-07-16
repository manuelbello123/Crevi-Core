package com.example.tienda.core.network

import com.example.tienda.core.network.ApiErrorEnvelope
import com.example.tienda.core.network.error.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object NetworkUtils {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient         = true
        encodeDefaults    = true
    }

    /**
     * Construye el HttpClient de la app.
     *
     * @param tokenProvider  devuelve el token de la sesión activa (o null). Se
     *        inyecta en cada request, sin cacheo entre sesiones.
     * @param onUnauthorized se invoca al recibir un 401 (token expirado o
     *        usuario desactivado) para cerrar la sesión globalmente.
     */
    fun createHttpClient(
        engine: HttpClientEngine,
        isDebug: Boolean,
        tokenProvider: () -> String?,
        onUnauthorized: suspend () -> Unit,
    ): HttpClient = HttpClient(engine) {

        // Manejamos los errores nosotros en el validador de abajo.
        expectSuccess = false

        install(ContentNegotiation) { json(json) }

        install(HttpTimeout) {
            requestTimeoutMillis = NetworkConstants.REQUEST_TIMEOUT_MS
            connectTimeoutMillis = NetworkConstants.CONNECT_TIMEOUT_MS
            socketTimeoutMillis  = NetworkConstants.SOCKET_TIMEOUT_MS
        }

        install(Logging) {
            level = if (isDebug) LogLevel.ALL else LogLevel.NONE
        }

        defaultRequest {
            url { takeFrom(NetworkConstants.BASE_URL) }
            // Token fresco de la sesión activa en cada request.
            tokenProvider()?.let { token ->
                headers.append(HttpHeaders.Authorization, NetworkConstants.BEARER_PREFIX + token)
            }
        }

        HttpResponseValidator {
            validateResponse { response ->
                // Solo inspeccionamos el cuerpo en respuestas de error, para no
                // consumir el body de las exitosas (lo necesita el caller).
                if (response.status.isSuccess()) return@validateResponse

                val serverMessage = runCatching { response.bodyAsText() }
                    .getOrNull()
                    ?.let(::extractServerMessage)

                when (response.status) {
                    HttpStatusCode.Unauthorized -> {
                        onUnauthorized()
                        throw NetworkError.Unauthorized
                    }
                    HttpStatusCode.Forbidden           -> throw NetworkError.Forbidden(serverMessage)
                    HttpStatusCode.NotFound            -> throw NetworkError.NotFound
                    HttpStatusCode.BadRequest          -> throw NetworkError.Validation(serverMessage)
                    HttpStatusCode.Conflict            -> throw NetworkError.Conflict(serverMessage)
                    HttpStatusCode.UnprocessableEntity -> throw NetworkError.BusinessRule(serverMessage)
                    HttpStatusCode.TooManyRequests     -> throw NetworkError.TooManyRequests
                    else -> if (response.status.value >= 500) {
                        throw NetworkError.ServerError(serverMessage)
                    } else {
                        throw NetworkError.Unknown(serverMessage)
                    }
                }
            }
        }
    }

    /** Extrae el "mensaje" del envelope {error:{codigo,mensaje}} del backend. */
    private fun extractServerMessage(body: String): String? = runCatching {
        json.decodeFromString<ApiErrorEnvelope>(body).error?.mensaje?.takeIf { it.isNotBlank() }
    }.getOrNull()
}
