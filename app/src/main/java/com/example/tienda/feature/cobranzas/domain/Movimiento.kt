package com.example.tienda.feature.cobranzas.domain

import java.math.BigDecimal

/** Movimiento de dinero sobre una cuenta (modelo de dominio). */
data class Movimiento(
    val id: Long,
    val cuentaId: Long?,
    val ventaId: Long?,
    val tipo: TipoMovimiento,
    val monto: BigDecimal,
    val creadoEn: String?,
    val registradoPor: Long?,
    val nota: String?,
)

/**
 * Tipo de movimiento / concepto. El backend lo manda en minúsculas (valorBd).
 * cargo_credito sube deuda; abono/anticipo/devolucion la bajan; venta_contado
 * no afecta la deuda (solo corte).
 */
enum class TipoMovimiento {
    CARGO_CREDITO, ABONO, ANTICIPO, DEVOLUCION, VENTA_CONTADO, DESCONOCIDO;

    companion object {
        fun from(value: String?): TipoMovimiento = when (value?.trim()?.lowercase()) {
            "cargo_credito" -> CARGO_CREDITO
            "abono" -> ABONO
            "anticipo" -> ANTICIPO
            "devolucion" -> DEVOLUCION
            "venta_contado" -> VENTA_CONTADO
            else -> DESCONOCIDO
        }
    }
}
