package com.example.tienda.feature.cobranzas.data.dto

import com.example.tienda.core.util.toMoney
import com.example.tienda.feature.cobranzas.domain.MovimientoReciente
import com.example.tienda.feature.cobranzas.domain.TipoMovimiento
import kotlinx.serialization.Serializable

/** GET /movimientos?sucursalId= (feed de actividad). */
@Serializable
data class MovimientoRecienteDto(
    val tipo: String? = null,
    val monto: String = "0",
    val creadoEn: String = "",
    val cuentaId: Long? = null,
    val ventaId: Long? = null,
    val nombreCliente: String? = null,
    val registradoPorNombre: String? = null,
    val sucursalId: Long? = null,
)

fun MovimientoRecienteDto.toDomain(): MovimientoReciente = MovimientoReciente(
    tipo = TipoMovimiento.from(tipo),
    monto = monto.toMoney(),
    creadoEn = creadoEn,
    nombreCliente = nombreCliente,
    registradoPor = registradoPorNombre,
)
