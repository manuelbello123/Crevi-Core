package com.example.tienda.feature.cobranzas.domain

import com.example.tienda.feature.clientes.domain.Cliente
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Item de la lista de cobranza: cliente + saldo global + fecha del último pago
 * (abono/anticipo) + bandera de "falta semanal".
 *
 * [faltaSemana] = tiene deuda y NO registró abono/anticipo en la semana ISO actual
 * (lunes–domingo). Se calcula al armar la lista en el ViewModel.
 */
data class ClienteCobranza(
    val cliente: Cliente,
    val saldoGlobal: BigDecimal,
    val ultimoPago: LocalDate?,
    val faltaSemana: Boolean,
) {
    val conDeuda: Boolean get() = saldoGlobal > BigDecimal.ZERO
}
