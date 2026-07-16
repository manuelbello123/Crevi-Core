package com.example.tienda.feature.configuraciones.data

import com.example.tienda.core.network.NetworkResult

interface AuthRepository {
    suspend fun cambiarPassword(passwordActual: String, passwordNueva: String): NetworkResult<Unit>
}
