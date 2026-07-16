package com.example.tienda.feature.cobranzas.data
import com.example.tienda.feature.cobranzas.data.dto.*
import com.example.tienda.feature.cobranzas.domain.*

import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.safeApiCall
import java.math.BigDecimal

class CobranzaRepositoryImpl(
    private val api: CobranzaApi,
) : CobranzaRepository {

    override suspend fun porCobrar(sucursalId: Long?): NetworkResult<List<CuentaPorCobrar>> =
        safeApiCall { api.porCobrar(sucursalId).map { it.toDomain() } }

    override suspend fun cuentasDeCliente(clienteId: Long): NetworkResult<List<Cuenta>> =
        safeApiCall { api.cuentasDeCliente(clienteId).map { it.toDomain() } }

    override suspend fun abrirCuenta(clienteId: Long, nombre: String?): NetworkResult<Cuenta> =
        safeApiCall { api.abrirCuenta(AbrirCuentaRequest(clienteId, nombre?.ifBlank { null })).toDomain() }

    override suspend fun editarCuenta(cuentaId: Long, nombre: String?): NetworkResult<Cuenta> =
        safeApiCall { api.editarCuenta(cuentaId, ActualizarCuentaRequest(nombre?.ifBlank { null })).toDomain() }

    override suspend fun historial(cuentaId: Long): NetworkResult<HistorialCuenta> =
        safeApiCall { api.historial(cuentaId).toDomain() }

    override suspend fun registrarAbono(cuentaId: Long, monto: BigDecimal, nota: String?): NetworkResult<Movimiento> =
        safeApiCall {
            api.abono(AbonoRequest(cuentaId = cuentaId, monto = monto.toPlainString(), nota = nota?.ifBlank { null }))
                .toDomain()
        }

    override suspend fun registrarAnticipo(cuentaId: Long, monto: BigDecimal, nota: String?): NetworkResult<Movimiento> =
        safeApiCall {
            api.anticipo(AbonoRequest(cuentaId = cuentaId, monto = monto.toPlainString(), nota = nota?.ifBlank { null }))
                .toDomain()
        }

    override suspend fun registrarDevolucion(
        cuentaId: Long,
        monto: BigDecimal,
        ventaId: Long?,
        nota: String?,
    ): NetworkResult<Movimiento> =
        safeApiCall {
            api.devolucion(
                DevolucionRequest(
                    cuentaId = cuentaId,
                    monto = monto.toPlainString(),
                    ventaId = ventaId,
                    nota = nota?.ifBlank { null },
                )
            ).toDomain()
        }

    override suspend fun editarMovimiento(movimientoId: Long, monto: BigDecimal, nota: String?): NetworkResult<Movimiento> =
        safeApiCall {
            api.editarMovimiento(
                movimientoId,
                EditarMovimientoRequest(monto = monto.toPlainString(), nota = nota?.ifBlank { null }),
            ).toDomain()
        }

    override suspend fun eliminarMovimiento(movimientoId: Long): NetworkResult<Unit> =
        safeApiCall { api.eliminarMovimiento(movimientoId) }

    override suspend fun movimientosRecientes(sucursalId: Long?, tamano: Int): NetworkResult<List<MovimientoReciente>> =
        safeApiCall { api.movimientosRecientes(sucursalId, pagina = 1, tamano = tamano).map { it.toDomain() } }
}
