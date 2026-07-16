package com.example.tienda.feature.sucursales.data

import com.example.tienda.feature.sucursales.data.dto.ActualizarSucursalRequest
import com.example.tienda.feature.sucursales.data.dto.CrearSucursalRequest
import com.example.tienda.feature.sucursales.data.dto.SucursalDto

interface SucursalApi {
    /** GET /sucursales (admin, gerente). */
    suspend fun listar(): List<SucursalDto>

    /** POST /sucursales (admin). */
    suspend fun crear(request: CrearSucursalRequest): SucursalDto

    /** PUT /sucursales/{id} (admin). */
    suspend fun editar(id: Long, request: ActualizarSucursalRequest): SucursalDto
}
