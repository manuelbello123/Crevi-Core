package com.example.tienda.feature.cobranzas.data.dto
import com.example.tienda.feature.cobranzas.domain.*
import com.example.tienda.core.util.toMoney

import kotlinx.serialization.Serializable

/** GET /cuentas/{id}/historial. */
@Serializable
data class HistorialCuentaDto(
    val cuentaId: Long,
    val saldo: String = "0",
    val movimientos: List<HistorialItemDto> = emptyList(),
)

@Serializable
data class HistorialItemDto(
    val movimientoId: Long? = null,
    val fecha: String = "",
    val hora: String = "",
    val concepto: String? = null,
    val monto: String = "0",
    val efectoSaldo: String = "0",
    val ventaId: Long? = null,
    val registradoPor: String = "",
)

fun HistorialCuentaDto.toDomain(): HistorialCuenta = HistorialCuenta(
    cuentaId = cuentaId,
    saldo = saldo.toMoney(),
    movimientos = movimientos.map { it.toDomain() },
)

fun HistorialItemDto.toDomain(): HistorialItem = HistorialItem(
    movimientoId = movimientoId,
    fecha = fecha,
    hora = hora,
    concepto = TipoMovimiento.from(concepto),
    monto = monto.toMoney(),
    efectoSaldo = efectoSaldo.toMoney(),
    ventaId = ventaId,
    registradoPor = registradoPor,
)
