package com.example.tienda.feature.ventas.data.dto
import com.example.tienda.feature.ventas.domain.*

import kotlinx.serialization.Serializable

/** Renglón para POST /ventas y PUT /ventas/{id}/renglones. precioUnit como texto, tipo en minúsculas. */
@Serializable
data class RenglonRequest(
    val descripcion: String,
    val cantidad: Int,
    val precioUnit: String,
    val tipo: String,
)

/** POST /ventas. cuentaId null = mostrador (solo contado, nombreMostrador obligatorio). */
@Serializable
data class RegistrarVentaRequest(
    val cuentaId: Long? = null,
    val nombreMostrador: String? = null,
    val pendiente: Boolean = false,
    val renglones: List<RenglonRequest>,
)

/** PUT /ventas/{id}/renglones (solo ventas pendientes). */
@Serializable
data class EditarRenglonesRequest(
    val renglones: List<RenglonRequest>,
)

fun RenglonVentaInput.toRequest(): RenglonRequest = RenglonRequest(
    descripcion = descripcion.trim(),
    cantidad = cantidad,
    precioUnit = precioUnit.toPlainString(),
    tipo = tipo.valorBd,
)
