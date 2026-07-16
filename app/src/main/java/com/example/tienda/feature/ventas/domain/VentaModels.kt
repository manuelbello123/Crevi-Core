package com.example.tienda.feature.ventas.domain

import java.math.BigDecimal

enum class EstadoVenta {
    PENDIENTE, CONSOLIDADA, CANCELADA, DESCONOCIDO;

    companion object {
        fun from(value: String?): EstadoVenta =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: DESCONOCIDO
    }
}

enum class TipoRenglon {
    CONTADO, CREDITO;

    /** El backend espera el tipo en minúsculas. */
    val valorBd: String get() = name.lowercase()

    companion object {
        fun from(value: String?): TipoRenglon =
            if (value?.trim()?.lowercase() == "credito") CREDITO else CONTADO
    }
}

/** Renglón de una venta (respuesta). */
data class RenglonVenta(
    val id: Long,
    val descripcion: String,
    val cantidad: Int,
    val precioUnit: BigDecimal,
    val importe: BigDecimal,
    val tipo: TipoRenglon,
)

/** Venta completa (con renglones). */
data class Venta(
    val id: Long,
    val cuentaId: Long?,
    val nombreMostrador: String?,
    val estado: EstadoVenta,
    val total: BigDecimal,
    val fechaCompra: String,
    val horaCompra: String,
    val fechaConsolidacion: String?,
    val registradaPor: Long?,
    val sucursalId: Long?,
    // Nombres para mostrar (null en mostrador / sin cuenta).
    val nombreCliente: String?,
    val nombreCuenta: String?,
    val registradaPorNombre: String?,
    val renglones: List<RenglonVenta>,
)

/** Resumen de venta para listados. */
data class VentaResumen(
    val id: Long,
    val cuentaId: Long?,
    val nombreMostrador: String?,
    val estado: EstadoVenta,
    val total: BigDecimal,
    val fechaCompra: String,
    val horaCompra: String,
    val registradaPor: Long?,
    val sucursalId: Long?,
    val nombreCliente: String?,
    val nombreCuenta: String?,
    val registradaPorNombre: String?,
    /** "contado" | "credito" | "mixto" según los renglones (mostrador se detecta por cuentaId null). */
    val tipo: String?,
)

/** Venta pendiente con su fecha límite. */
data class VentaPendiente(
    val ventaId: Long,
    val cuentaId: Long?,
    val total: BigDecimal,
    val fechaCompra: String,
    val venceEl: String,
    val registradaPor: Long?,
    val nombreCliente: String?,
    val nombreCuenta: String?,
)

/** Renglón en construcción para crear/editar una venta. */
data class RenglonVentaInput(
    val descripcion: String,
    val cantidad: Int,
    val precioUnit: BigDecimal,
    val tipo: TipoRenglon,
) {
    val importe: BigDecimal get() = precioUnit.multiply(BigDecimal(cantidad))
}
