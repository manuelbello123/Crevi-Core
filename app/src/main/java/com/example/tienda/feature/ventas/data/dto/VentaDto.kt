package com.example.tienda.feature.ventas.data.dto
import com.example.tienda.feature.ventas.domain.*

import com.example.tienda.core.util.toMoney
import kotlinx.serialization.Serializable

@Serializable
data class VentaDetalleDto(
    val id: Long,
    val descripcion: String,
    val cantidad: Int = 1,
    val precioUnit: String = "0",
    val importe: String = "0",
    val tipo: String? = null,
)

@Serializable
data class VentaDto(
    val id: Long,
    val cuentaId: Long? = null,
    val nombreMostrador: String? = null,
    val estado: String? = null,
    val total: String = "0",
    val fechaCompra: String = "",
    val horaCompra: String = "",
    val fechaConsolidacion: String? = null,
    val registradaPor: Long? = null,
    val sucursalId: Long? = null,
    val nombreCliente: String? = null,
    val nombreCuenta: String? = null,
    val registradaPorNombre: String? = null,
    val renglones: List<VentaDetalleDto> = emptyList(),
)

@Serializable
data class VentaResumenDto(
    val id: Long,
    val cuentaId: Long? = null,
    val nombreMostrador: String? = null,
    val estado: String? = null,
    val total: String = "0",
    val fechaCompra: String = "",
    val horaCompra: String = "",
    val registradaPor: Long? = null,
    val sucursalId: Long? = null,
    val nombreCliente: String? = null,
    val nombreCuenta: String? = null,
    val registradaPorNombre: String? = null,
    val tipo: String? = null,
)

@Serializable
data class PaginaVentaResumenDto(
    val datos: List<VentaResumenDto> = emptyList(),
    val pagina: Int = 1,
    val tamano: Int = 20,
    val total: Long = 0,
)

@Serializable
data class VentaPendienteDto(
    val ventaId: Long,
    val cuentaId: Long? = null,
    val total: String = "0",
    val fechaCompra: String = "",
    val venceEl: String = "",
    val registradaPor: Long? = null,
    val nombreCliente: String? = null,
    val nombreCuenta: String? = null,
)

fun VentaDetalleDto.toDomain(): RenglonVenta = RenglonVenta(
    id = id,
    descripcion = descripcion,
    cantidad = cantidad,
    precioUnit = precioUnit.toMoney(),
    importe = importe.toMoney(),
    tipo = TipoRenglon.from(tipo),
)

fun VentaDto.toDomain(): Venta = Venta(
    id = id,
    cuentaId = cuentaId,
    nombreMostrador = nombreMostrador,
    estado = EstadoVenta.from(estado),
    total = total.toMoney(),
    fechaCompra = fechaCompra,
    horaCompra = horaCompra,
    fechaConsolidacion = fechaConsolidacion,
    registradaPor = registradaPor,
    sucursalId = sucursalId,
    nombreCliente = nombreCliente,
    nombreCuenta = nombreCuenta,
    registradaPorNombre = registradaPorNombre,
    renglones = renglones.map { it.toDomain() },
)

fun VentaResumenDto.toDomain(): VentaResumen = VentaResumen(
    id = id,
    cuentaId = cuentaId,
    nombreMostrador = nombreMostrador,
    estado = EstadoVenta.from(estado),
    total = total.toMoney(),
    fechaCompra = fechaCompra,
    horaCompra = horaCompra,
    registradaPor = registradaPor,
    sucursalId = sucursalId,
    nombreCliente = nombreCliente,
    nombreCuenta = nombreCuenta,
    registradaPorNombre = registradaPorNombre,
    tipo = tipo,
)

fun VentaPendienteDto.toDomain(): VentaPendiente = VentaPendiente(
    ventaId = ventaId,
    cuentaId = cuentaId,
    total = total.toMoney(),
    fechaCompra = fechaCompra,
    venceEl = venceEl,
    registradaPor = registradaPor,
    nombreCliente = nombreCliente,
    nombreCuenta = nombreCuenta,
)
