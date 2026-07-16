package com.example.tienda.feature.sucursales.domain

/** Sucursal (modelo de dominio). */
data class Sucursal(
    val id: Long,
    val nombre: String,
    val direccion: String?,
    val telefono: String?,
    val esMatriz: Boolean,
)
