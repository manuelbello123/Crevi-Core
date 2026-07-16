package com.example.tienda.feature.configuraciones.data

import com.example.tienda.feature.configuraciones.data.dto.ActualizarConfigLoginClientesRequest
import com.example.tienda.feature.configuraciones.data.dto.ConfigLoginClientesDto

interface ConfigApi {
    /** GET /config/login-clientes (solo admin). */
    suspend fun obtenerLoginClientes(): ConfigLoginClientesDto

    /** PUT /config/login-clientes (solo admin). */
    suspend fun actualizarLoginClientes(request: ActualizarConfigLoginClientesRequest): ConfigLoginClientesDto
}
