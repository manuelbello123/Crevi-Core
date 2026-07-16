package com.example.tienda.feature.ventas.data
import com.example.tienda.feature.ventas.domain.*
import com.example.tienda.feature.ventas.data.dto.*

interface VentaApi {

    /** GET /ventas (compacto, paginado). Rango fechaDesde..fechaHasta (YYYY-MM-DD); estado en minúsculas. */
    suspend fun listar(
        sucursalId: Long?,
        fechaDesde: String?,
        fechaHasta: String?,
        estado: String?,
        pagina: Int,
        tamano: Int,
    ): PaginaVentaResumenDto

    /** GET /ventas/pendientes. */
    suspend fun pendientes(): List<VentaPendienteDto>

    /** GET /ventas/{id}. */
    suspend fun obtener(id: Long): VentaDto

    /** GET /cuentas/{id}/ventas (ventas de la cuenta, con renglones). */
    suspend fun ventasDeCuenta(cuentaId: Long): List<VentaDto>

    /** POST /ventas. */
    suspend fun crear(request: RegistrarVentaRequest): VentaDto

    /** PUT /ventas/{id}/renglones (solo pendientes). */
    suspend fun editarRenglones(id: Long, request: EditarRenglonesRequest): VentaDto

    /** POST /ventas/{id}/consolidar. */
    suspend fun consolidar(id: Long): VentaDto

    /** POST /ventas/{id}/cancelar. */
    suspend fun cancelar(id: Long): VentaDto
}
