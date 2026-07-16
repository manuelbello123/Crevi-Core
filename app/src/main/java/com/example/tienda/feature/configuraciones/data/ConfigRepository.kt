package com.example.tienda.feature.configuraciones.data

import com.example.tienda.core.network.NetworkResult

interface ConfigRepository {
    suspend fun obtenerLoginClientesDeshabilitado(): NetworkResult<Boolean>
    suspend fun setLoginClientesDeshabilitado(deshabilitado: Boolean): NetworkResult<Boolean>
}
