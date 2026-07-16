package com.example.tienda.feature.cobranzas.data
import com.example.tienda.feature.cobranzas.data.dto.*
import com.example.tienda.feature.cobranzas.domain.*

import com.example.tienda.core.network.NetworkResult
import java.math.BigDecimal

interface CobranzaRepository {

    suspend fun porCobrar(sucursalId: Long?): NetworkResult<List<CuentaPorCobrar>>

    suspend fun cuentasDeCliente(clienteId: Long): NetworkResult<List<Cuenta>>

    suspend fun abrirCuenta(clienteId: Long, nombre: String?): NetworkResult<Cuenta>

    /** Renombra una cuenta activa. [nombre] en blanco o null la deja sin nombre. */
    suspend fun editarCuenta(cuentaId: Long, nombre: String?): NetworkResult<Cuenta>

    suspend fun historial(cuentaId: Long): NetworkResult<HistorialCuenta>

    suspend fun registrarAbono(cuentaId: Long, monto: BigDecimal, nota: String?): NetworkResult<Movimiento>

    suspend fun registrarAnticipo(cuentaId: Long, monto: BigDecimal, nota: String?): NetworkResult<Movimiento>

    suspend fun registrarDevolucion(
        cuentaId: Long,
        monto: BigDecimal,
        ventaId: Long?,
        nota: String?,
    ): NetworkResult<Movimiento>

    /** Edita un movimiento manual. [nota] en blanco/null no se toca. */
    suspend fun editarMovimiento(movimientoId: Long, monto: BigDecimal, nota: String?): NetworkResult<Movimiento>

    suspend fun eliminarMovimiento(movimientoId: Long): NetworkResult<Unit>

    /** Feed de actividad reciente de una sucursal (movimientos, más nuevos primero). */
    suspend fun movimientosRecientes(sucursalId: Long?, tamano: Int): NetworkResult<List<MovimientoReciente>>
}
