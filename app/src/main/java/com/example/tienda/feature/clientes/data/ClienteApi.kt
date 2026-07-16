package com.example.tienda.feature.clientes.data
import com.example.tienda.feature.clientes.data.dto.*

interface ClienteApi {

    /** GET /clientes (admin: todas o filtrada; gerente: forzado a su sucursal). */
    suspend fun listar(
        q: String?,
        pagina: Int,
        tamano: Int,
        sucursalId: Long?,
    ): PaginaClienteDto

    /** GET /clientes/{id}. */
    suspend fun obtener(id: Long): ClienteDto

    /** POST /clientes. */
    suspend fun crear(request: CrearClienteRequest): ClienteDto

    /** PUT /clientes/{id} (datos de contacto). */
    suspend fun actualizar(id: Long, request: ActualizarClienteRequest): ClienteDto

    /** PATCH /clientes/{id}/activo. */
    suspend fun cambiarActivo(id: Long, request: CambiarEstadoRequest)

    /** PATCH /clientes/{id}/asignacion (SOLO admin). */
    suspend fun reasignar(id: Long, request: AsignarClienteRequest): ClienteDto

    /** PATCH /clientes/{id}/acceso-app. */
    suspend fun cambiarAccesoApp(id: Long, request: AccesoAppRequest)
}
