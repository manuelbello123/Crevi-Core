package com.example.tienda.feature.clientes.ui

import com.example.tienda.feature.clientes.domain.Cliente
import com.example.tienda.feature.sucursales.domain.Sucursal
import com.example.tienda.feature.usuarios.domain.Usuario
import com.example.tienda.core.util.UiText

/** Sub-navegación del módulo Clientes (sin rutas; estado dentro de la sección). */
enum class ClientesMode { LISTA, NUEVO, EDITAR }

/** Diálogo activo sobre la lista (acciones cortas). */
sealed interface ClientesDialog {
    val cliente: Cliente
    data class Reasignar(override val cliente: Cliente) : ClientesDialog
    data class AccesoApp(override val cliente: Cliente) : ClientesDialog
    data class ConfirmarEstado(override val cliente: Cliente) : ClientesDialog
}

data class ClientesUiState(
    val esAdministrador: Boolean = false,
    // Sucursal en contexto (la fija el switch del Home). null = todas (admin sin seleccionar).
    val selectedSucursalId: Long? = null,

    // Lista / búsqueda
    val query: String = "",
    val clientes: List<Cliente> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,

    // Formulario (alta/edición)
    val mode: ClientesMode = ClientesMode.LISTA,
    val editando: Cliente? = null,
    val saving: Boolean = false,
    val formError: UiText? = null,

    // Diálogo activo (reasignar / acceso-app / confirmar estado)
    val dialogo: ClientesDialog? = null,

    // Datos para selección en formularios (admin)
    val sucursales: List<Sucursal> = emptyList(),
    val operadores: List<Usuario> = emptyList(),

    // Mensaje efímero de éxito (snackbar)
    val mensaje: UiText? = null,
) {
    /** Operadores de una sucursal (para elegir gerente responsable). */
    fun operadoresDeSucursal(sucursalId: Long?): List<Usuario> =
        operadores.filter { it.sucursalId == sucursalId }
}
