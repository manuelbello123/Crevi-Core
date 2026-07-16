package com.example.tienda.feature.configuraciones.data

import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.safeApiCall
import com.example.tienda.feature.configuraciones.data.dto.CambiarPasswordRequest

class AuthRepositoryImpl(
    private val api: AuthApi,
) : AuthRepository {

    override suspend fun cambiarPassword(passwordActual: String, passwordNueva: String): NetworkResult<Unit> =
        safeApiCall { api.cambiarPassword(CambiarPasswordRequest(passwordActual, passwordNueva)) }
}
