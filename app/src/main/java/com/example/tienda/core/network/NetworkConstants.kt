package com.example.tienda.core.network

object NetworkConstants {
    const val BASE_URL = "http://18.222.150.9"

    const val REQUEST_TIMEOUT_MS = 30_000L
    const val CONNECT_TIMEOUT_MS = 15_000L
    const val SOCKET_TIMEOUT_MS  = 30_000L

    const val BEARER_PREFIX = "Bearer "

    // Paginación (query params ?pagina=&tamano=).
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE     = 100
}
