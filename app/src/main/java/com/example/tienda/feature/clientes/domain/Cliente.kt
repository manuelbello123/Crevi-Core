package com.example.tienda.feature.clientes.domain

/** Cliente (modelo de dominio). */
data class Cliente(
    val id: Long,
    val numeroCliente: String,
    val nombreCompleto: String,
    val telefono: String?,
    val direccion: String?,
    val sucursalId: Long?,
    val gerenteId: Long?,
    val accesoApp: Boolean,
    val activo: Boolean,
)
