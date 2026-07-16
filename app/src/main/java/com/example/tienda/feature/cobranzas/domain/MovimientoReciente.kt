package com.example.tienda.feature.cobranzas.domain

import java.math.BigDecimal

/** Movimiento para el feed de actividad reciente (por sucursal). */
data class MovimientoReciente(
    val tipo: TipoMovimiento,
    val monto: BigDecimal,
    val creadoEn: String,
    val nombreCliente: String?,
    val registradoPor: String?,
)
