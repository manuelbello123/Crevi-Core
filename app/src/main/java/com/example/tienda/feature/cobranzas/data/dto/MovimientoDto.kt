package com.example.tienda.feature.cobranzas.data.dto
import com.example.tienda.feature.cobranzas.domain.*
import com.example.tienda.core.util.toMoney

import kotlinx.serialization.Serializable

/** Respuesta de POST /movimientos/abono y /movimientos/devolucion. */
@Serializable
data class MovimientoDto(
    val id: Long,
    val cuentaId: Long? = null,
    val ventaId: Long? = null,
    val tipo: String? = null,
    val monto: String = "0",
    val creadoEn: String? = null,
    val registradoPor: Long? = null,
    val sucursalId: Long? = null,
    val nota: String? = null,
)

fun MovimientoDto.toDomain(): Movimiento = Movimiento(
    id = id,
    cuentaId = cuentaId,
    ventaId = ventaId,
    tipo = TipoMovimiento.from(tipo),
    monto = monto.toMoney(),
    creadoEn = creadoEn,
    registradoPor = registradoPor,
    nota = nota,
)
