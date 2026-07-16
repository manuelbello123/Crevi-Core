package com.example.tienda.core.network

object NetworkConstants {
    // Hostname TEMPORAL vía sslip.io (resuelve "18-222-150-9.sslip.io" a la IP
    // 18.222.150.9). Se usa como sustituto mientras no haya dominio propio, para
    // que el certificado de Let's Encrypt (nginx en EC2) valide por hostname y la
    // app hable HTTPS. Reemplazar por el dominio real cuando exista (igual que Cliente).
    const val BASE_URL = "https://18-222-150-9.sslip.io"

    const val REQUEST_TIMEOUT_MS = 30_000L
    const val CONNECT_TIMEOUT_MS = 15_000L
    const val SOCKET_TIMEOUT_MS  = 30_000L

    const val BEARER_PREFIX = "Bearer "

    // Paginación (query params ?pagina=&tamano=).
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE     = 100
}
