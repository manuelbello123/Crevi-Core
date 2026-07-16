package com.example.tienda.feature.cobranzas.data.dto
import com.example.tienda.feature.cobranzas.domain.*
import com.example.tienda.core.util.toMoney

import kotlinx.serialization.Serializable

/** POST /movimientos/abono. monto como texto. */
@Serializable
data class AbonoRequest(
    val cuentaId: Long,
    val monto: String,
    val nota: String? = null,
)

/** POST /movimientos/devolucion. ventaId opcional. */
@Serializable
data class DevolucionRequest(
    val cuentaId: Long,
    val monto: String,
    val ventaId: Long? = null,
    val nota: String? = null,
)

/** POST /cuentas (abrir cuenta para un cliente). */
@Serializable
data class AbrirCuentaRequest(
    val clienteId: Long,
    val nombre: String? = null,
)

/** PATCH /movimientos/{id}. Campos null = no se tocan; nota "" la limpia. */
@Serializable
data class EditarMovimientoRequest(
    val monto: String? = null,
    val nota: String? = null,
)

/** PATCH /cuentas/{id}. nombre null o vacío = dejar sin nombre. Cuenta debe estar ACTIVA. */
@Serializable
data class ActualizarCuentaRequest(
    val nombre: String? = null,
)
