package com.example.tienda.feature.cobranzas.data
import com.example.tienda.feature.cobranzas.data.dto.*
import com.example.tienda.feature.cobranzas.domain.*

interface CobranzaApi {

    /** GET /cuentas/por-cobrar (saldos de cuentas con deuda, por sucursal). */
    suspend fun porCobrar(sucursalId: Long?): List<CuentaPorCobrarDto>

    /** GET /clientes/{id}/cuentas. */
    suspend fun cuentasDeCliente(clienteId: Long): List<CuentaDto>

    /** POST /cuentas (abrir cuenta). */
    suspend fun abrirCuenta(request: AbrirCuentaRequest): CuentaDto

    /** PATCH /cuentas/{id} (renombrar cuenta activa). */
    suspend fun editarCuenta(id: Long, request: ActualizarCuentaRequest): CuentaDto

    /** GET /cuentas/{id}/historial. */
    suspend fun historial(cuentaId: Long): HistorialCuentaDto

    /** POST /movimientos/abono. */
    suspend fun abono(request: AbonoRequest): MovimientoDto

    /** POST /movimientos/anticipo (mismo body que abono; requiere deuda existente). */
    suspend fun anticipo(request: AbonoRequest): MovimientoDto

    /** POST /movimientos/devolucion. */
    suspend fun devolucion(request: DevolucionRequest): MovimientoDto

    /** PATCH /movimientos/{id} (solo abono/anticipo/devolucion). */
    suspend fun editarMovimiento(id: Long, request: EditarMovimientoRequest): MovimientoDto

    /** DELETE /movimientos/{id} (solo abono/anticipo/devolucion). */
    suspend fun eliminarMovimiento(id: Long)

    /** GET /movimientos?sucursalId= (feed de actividad reciente, más nuevos primero). */
    suspend fun movimientosRecientes(sucursalId: Long?, pagina: Int, tamano: Int): List<MovimientoRecienteDto>
}
