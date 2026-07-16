package com.example.tienda.feature.cobranzas.domain

import java.math.BigDecimal

/** Cuenta de un cliente (modelo de dominio). El saldo es la fuente de verdad del backend. */
data class Cuenta(
    val id: Long,
    val clienteId: Long,
    val nombre: String?,
    val estado: CuentaEstado,
    val saldo: BigDecimal,
    val abiertaPor: Long?,
    val creadaEn: String?,
)

enum class CuentaEstado {
    ACTIVA, CERRADA;

    companion object {
        fun from(value: String?): CuentaEstado =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: ACTIVA
    }
}
