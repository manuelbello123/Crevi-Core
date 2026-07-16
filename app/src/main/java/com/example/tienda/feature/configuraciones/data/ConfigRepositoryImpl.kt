package com.example.tienda.feature.configuraciones.data

import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.safeApiCall
import com.example.tienda.feature.configuraciones.data.dto.ActualizarConfigLoginClientesRequest

class ConfigRepositoryImpl(
    private val api: ConfigApi,
) : ConfigRepository {

    override suspend fun obtenerLoginClientesDeshabilitado(): NetworkResult<Boolean> =
        safeApiCall { api.obtenerLoginClientes().deshabilitado }

    override suspend fun setLoginClientesDeshabilitado(deshabilitado: Boolean): NetworkResult<Boolean> =
        safeApiCall { api.actualizarLoginClientes(ActualizarConfigLoginClientesRequest(deshabilitado)).deshabilitado }
}
