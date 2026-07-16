package com.example.tienda.feature.usuarios.data
import com.example.tienda.feature.usuarios.data.dto.*

import com.example.tienda.core.enums.UserRole
import com.example.tienda.feature.usuarios.domain.Usuario
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.safeApiCall

class UsuarioRepositoryImpl(
    private val api: UsuarioApi,
) : UsuarioRepository {

    override suspend fun listar(q: String?, pagina: Int, tamano: Int): NetworkResult<List<Usuario>> =
        safeApiCall { api.listar(q, pagina, tamano).datos.map { it.toDomain() } }

    override suspend fun obtener(id: Long): NetworkResult<Usuario> =
        safeApiCall { api.obtener(id).toDomain() }

    override suspend fun crear(
        nombre: String,
        usuario: String,
        password: String,
        rol: UserRole,
        sucursalId: Long?,
    ): NetworkResult<Usuario> =
        safeApiCall {
            api.crear(
                CrearUsuarioRequest(
                    nombre = nombre.trim(),
                    usuario = usuario.trim(),
                    password = password,
                    rol = rol.name.lowercase(),   // backend espera minúsculas
                    sucursalId = sucursalId,
                )
            ).toDomain()
        }

    override suspend fun actualizar(id: Long, nombre: String, usuario: String): NetworkResult<Usuario> =
        safeApiCall { api.actualizar(id, ActualizarUsuarioRequest(nombre.trim(), usuario.trim())).toDomain() }

    override suspend fun cambiarActivo(id: Long, activo: Boolean): NetworkResult<Unit> =
        safeApiCall { api.cambiarActivo(id, CambiarEstadoRequest(activo)) }

    override suspend fun reasignarSucursal(id: Long, sucursalId: Long): NetworkResult<Usuario> =
        safeApiCall { api.reasignarSucursal(id, AsignarSucursalRequest(sucursalId)).toDomain() }

    override suspend fun resetPassword(id: Long, passwordNueva: String): NetworkResult<Unit> =
        safeApiCall { api.resetPassword(id, ResetPasswordRequest(passwordNueva)) }
}
