package com.example.tienda.feature.usuarios.data
import com.example.tienda.feature.usuarios.data.dto.*

interface UsuarioApi {

    /** GET /usuarios (admin). */
    suspend fun listar(q: String?, pagina: Int, tamano: Int): PaginaUsuarioDto

    /** GET /usuarios/{id}. */
    suspend fun obtener(id: Long): UsuarioDto

    /** POST /usuarios. */
    suspend fun crear(request: CrearUsuarioRequest): UsuarioDto

    /** PUT /usuarios/{id} (nombre y usuario). */
    suspend fun actualizar(id: Long, request: ActualizarUsuarioRequest): UsuarioDto

    /** PATCH /usuarios/{id}/activo (no a sí mismo). */
    suspend fun cambiarActivo(id: Long, request: CambiarEstadoRequest)

    /** PATCH /usuarios/{id}/sucursal (reasignar gerente). */
    suspend fun reasignarSucursal(id: Long, request: AsignarSucursalRequest): UsuarioDto

    /** PATCH /usuarios/{id}/password (resetear). */
    suspend fun resetPassword(id: Long, request: ResetPasswordRequest)
}
