package com.example.tienda.core.network.error

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerializationException
import java.nio.channels.UnresolvedAddressException

/**
 * Normaliza cualquier excepción a un [NetworkError].
 *
 * Los errores HTTP con cuerpo {error:{codigo,mensaje}} ya se transforman en
 * [NetworkError] dentro del HttpResponseValidator (ver NetworkUtils); por eso
 * el primer caso respeta los [NetworkError] ya lanzados. Aquí cubrimos sobre
 * todo fallos de transporte/serialización y, como red de seguridad, los HTTP
 * que no pasaron por el validador.
 */
fun mapToNetworkError(throwable: Throwable): NetworkError = when (throwable) {
    is NetworkError -> throwable

    is ClientRequestException  -> throwable.response.status.toNetworkError(throwable.message)
    is ServerResponseException -> NetworkError.ServerError(throwable.message)

    is HttpRequestTimeoutException -> NetworkError.Timeout
    is ConnectTimeoutException     -> NetworkError.Timeout
    is SocketTimeoutException      -> NetworkError.Timeout

    is UnresolvedAddressException -> NetworkError.NoInternet
    is SerializationException     -> NetworkError.Serialization

    else -> NetworkError.Unknown(throwable.message)
}

private fun HttpStatusCode.toNetworkError(message: String?): NetworkError = when (this) {
    HttpStatusCode.BadRequest          -> NetworkError.Validation(message)
    HttpStatusCode.Unauthorized        -> NetworkError.Unauthorized
    HttpStatusCode.Forbidden           -> NetworkError.Forbidden(message)
    HttpStatusCode.NotFound            -> NetworkError.NotFound
    HttpStatusCode.Conflict            -> NetworkError.Conflict(message)
    HttpStatusCode.UnprocessableEntity -> NetworkError.BusinessRule(message)
    HttpStatusCode.TooManyRequests     -> NetworkError.TooManyRequests
    else -> NetworkError.Unknown(message)
}
