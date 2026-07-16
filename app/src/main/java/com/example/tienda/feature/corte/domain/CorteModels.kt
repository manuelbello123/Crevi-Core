package com.example.tienda.feature.corte.domain

import java.math.BigDecimal

/** Corte semanal agregado por sucursal (dinero como BigDecimal). */
data class CorteSucursal(
    val sucursalId: Long,
    val sucursal: String,
    val anioIso: Int,
    val semanaIso: Int,
    val desde: String,
    val hasta: String,
    val totalContado: BigDecimal,
    val totalAbonos: BigDecimal,
    val totalAnticipos: BigDecimal,
    val totalIngresos: BigDecimal,
)

/** Corte semanal desglosado por usuario/operador. */
data class CorteUsuario(
    val sucursalId: Long,
    val sucursal: String,
    val anioIso: Int,
    val semanaIso: Int,
    val desde: String,
    val hasta: String,
    val usuarioId: Long,
    val usuario: String,
    val rol: String,
    val contadoVendido: BigDecimal,
    val cobradoAbonos: BigDecimal,
    val anticipos: BigDecimal,
    val creditoColocado: BigDecimal,
    val devoluciones: BigDecimal,
    val numMovimientos: Int,
)
