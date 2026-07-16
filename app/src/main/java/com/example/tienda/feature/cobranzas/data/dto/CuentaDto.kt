package com.example.tienda.feature.cobranzas.data.dto
import com.example.tienda.feature.cobranzas.domain.*
import com.example.tienda.core.util.toMoney

import kotlinx.serialization.Serializable

/** GET /clientes/{id}/cuentas, GET /cuentas/{id}. Dinero como texto. */
@Serializable
data class CuentaDto(
    val id: Long,
    val clienteId: Long,
    val nombre: String? = null,
    val estado: String? = null,
    val saldo: String = "0",
    val abiertaPor: Long? = null,
    val creadaEn: String? = null,
)

fun CuentaDto.toDomain(): Cuenta = Cuenta(
    id = id,
    clienteId = clienteId,
    nombre = nombre,
    estado = CuentaEstado.from(estado),
    saldo = saldo.toMoney(),
    abiertaPor = abiertaPor,
    creadaEn = creadaEn,
)
