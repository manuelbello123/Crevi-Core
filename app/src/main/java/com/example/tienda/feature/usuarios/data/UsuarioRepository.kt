package com.example.tienda.feature.usuarios.data

import com.example.tienda.core.enums.UserRole
import com.example.tienda.feature.usuarios.domain.Usuario
import com.example.tienda.core.network.NetworkResult

interface UsuarioRepository {

    suspend fun listar(q: String?, pagina: Int, tamano: Int): NetworkResult<List<Usuario>>

    suspend fun obtener(id: Long): NetworkResult<Usuario>

    suspend fun crear(
        nombre: String,
        usuario: String,
        password: String,
        rol: UserRole,
        sucursalId: Long?,
    ): NetworkResult<Usuario>

    suspend fun actualizar(id: Long, nombre: String, usuario: String): NetworkResult<Usuario>

    suspend fun cambiarActivo(id: Long, activo: Boolean): NetworkResult<Unit>

    suspend fun reasignarSucursal(id: Long, sucursalId: Long): NetworkResult<Usuario>

    suspend fun resetPassword(id: Long, passwordNueva: String): NetworkResult<Unit>
}
