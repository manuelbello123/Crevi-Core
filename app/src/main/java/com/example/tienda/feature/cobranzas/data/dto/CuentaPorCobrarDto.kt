package com.example.tienda.feature.cobranzas.data.dto
import com.example.tienda.feature.cobranzas.domain.*
import com.example.tienda.core.util.toMoney

import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.serialization.Serializable

/** GET /cuentas/por-cobrar (cuentas con saldo > 0; agrupable por cliente). */
@Serializable
data class CuentaPorCobrarDto(
    val cuentaId: Long,
    val clienteId: Long,
    val numeroCliente: String? = null,
    val nombreCompleto: String? = null,
    val sucursalId: Long? = null,
    val saldo: String = "0",
    /** Fecha (YYYY-MM-DD) del último abono/anticipo de la cuenta; null si nunca. */
    val ultimoPago: String? = null,
)

/** Saldo de una cuenta con deuda (modelo de dominio). */
data class CuentaPorCobrar(
    val cuentaId: Long,
    val clienteId: Long,
    val saldo: BigDecimal,
    val ultimoPago: LocalDate?,
)

fun CuentaPorCobrarDto.toDomain(): CuentaPorCobrar = CuentaPorCobrar(
    cuentaId = cuentaId,
    clienteId = clienteId,
    saldo = saldo.toMoney(),
    ultimoPago = ultimoPago?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
)
