package com.example.tienda.feature.cobranzas.domain

import java.math.BigDecimal

/** Línea de tiempo de una cuenta + su saldo (modelo de dominio). */
data class HistorialCuenta(
    val cuentaId: Long,
    val saldo: BigDecimal,
    val movimientos: List<HistorialItem>,
)

data class HistorialItem(
    val movimientoId: Long?,
    val fecha: String,
    val hora: String,
    val concepto: TipoMovimiento,
    val monto: BigDecimal,
    /** Con signo: + sube deuda, - baja, 0 no afecta. */
    val efectoSaldo: BigDecimal,
    val ventaId: Long?,
    val registradoPor: String,
) {
    /** Movimiento manual editable/eliminable (no derivado de una venta). */
    val esManual: Boolean
        get() = movimientoId != null &&
            (concepto == TipoMovimiento.ABONO || concepto == TipoMovimiento.ANTICIPO || concepto == TipoMovimiento.DEVOLUCION)
}
