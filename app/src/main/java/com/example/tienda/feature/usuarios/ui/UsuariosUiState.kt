package com.example.tienda.feature.usuarios.ui

import com.example.tienda.feature.sucursales.domain.Sucursal
import com.example.tienda.feature.usuarios.domain.Usuario
import com.example.tienda.core.util.UiText

/** Sub-navegación del módulo Usuarios. */
enum class UsuariosMode { LISTA, NUEVO, EDITAR }

/** Diálogo activo sobre la lista (acciones cortas). */
sealed interface UsuariosDialog {
    val usuario: Usuario
    data class ReasignarSucursal(override val usuario: Usuario) : UsuariosDialog
    data class ResetPassword(override val usuario: Usuario) : UsuariosDialog
    data class ConfirmarEstado(override val usuario: Usuario) : UsuariosDialog
}

data class UsuariosUiState(
    // Lista / búsqueda
    val query: String = "",
    val usuarios: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,

    // Formulario (alta/edición)
    val mode: UsuariosMode = UsuariosMode.LISTA,
    val editando: Usuario? = null,
    val saving: Boolean = false,
    val formError: UiText? = null,

    // Diálogo activo
    val dialogo: UsuariosDialog? = null,

    // Selección de sucursal (para gerentes)
    val sucursales: List<Sucursal> = emptyList(),

    // Id del operador en sesión (no puede desactivarse a sí mismo)
    val miId: Long? = null,

    // Mensaje efímero de éxito
    val mensaje: UiText? = null,
)
