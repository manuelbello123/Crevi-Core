package com.example.tienda.feature.configuraciones.data

import com.example.tienda.feature.configuraciones.data.dto.CambiarPasswordRequest

interface AuthApi {
    /** PUT /auth/me/password (el usuario cambia su propia contraseña). */
    suspend fun cambiarPassword(request: CambiarPasswordRequest)
}
