package com.example.tienda.feature.usuarios.domain

import com.example.tienda.core.enums.UserRole

/** Operador (administrador/gerente) — modelo de dominio. */
data class Usuario(
    val id: Long,
    val nombre: String,
    val usuario: String,
    val rol: UserRole,
    val sucursalId: Long?,
    val activo: Boolean,
)
