package com.example.tienda.feature.home.domain

import java.math.BigDecimal

/** Tipo de ítem en el feed de actividad reciente del resumen. */
enum class ActividadTipo { VENTA, PENDIENTE, CANCELADA, ABONO, ANTICIPO, DEVOLUCION }

/**
 * Ítem unificado del feed de actividad (ventas + movimientos), listo para mostrar.
 * [orden] es la clave cronológica descendente; [cuando] es el texto a mostrar.
 */
data class ActividadItem(
    val tipo: ActividadTipo,
    val titulo: String,
    val subtitulo: String,
    val monto: BigDecimal,
    val cuando: String,
    val orden: String,
)
