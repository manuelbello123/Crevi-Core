package com.example.tienda.core.enums

/**
 * Rol del operador. El backend lo devuelve en MAYÚSCULAS ("ADMINISTRADOR",
 * "GERENTE"). Al CREAR un usuario se manda en minúsculas (eso es otro DTO).
 */
enum class UserRole {
    ADMINISTRADOR,
    GERENTE;

    companion object {
        /** Tolerante a mayúsculas/minúsculas; por seguridad cae a [GERENTE] (menor privilegio). */
        fun from(value: String?): UserRole =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: GERENTE
    }
}
