package com.example.tienda.core.network

import com.example.tienda.core.network.error.NetworkError
import com.example.tienda.core.network.error.mapToNetworkError
import kotlinx.coroutines.CancellationException

/** Resultado de una llamada de red: éxito con dato o error ya normalizado. */
sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class Error(val error: NetworkError) : NetworkResult<Nothing>
}

/**
 * Envuelve una llamada suspend, atrapa cualquier excepción y la normaliza a
 * [NetworkError]. Todo apiImpl/repositoryImpl debería devolver [NetworkResult]
 * a través de aquí.
 *
 * Re-lanza [CancellationException] para no romper la cancelación de corrutinas.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> =
    try {
        NetworkResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        NetworkResult.Error(mapToNetworkError(e))
    }

/** Helpers de consumo cómodo en repositorios/viewmodels. */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (NetworkError) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) action(error)
    return this
}
