package com.example.tienda.feature.sucursales.ui

import com.example.tienda.feature.sucursales.domain.Sucursal
import com.example.tienda.core.util.UiText

/**
 * Gestión de sucursales (solo admin): lista + búsqueda, y alta/edición
 * (nombre, dirección, teléfono) en un formulario inferior. La sucursal no tiene
 * estado activo/cerrado en el backend, por eso no hay activar/desactivar.
 */
data class SucursalesUiState(
    val sucursales: List<Sucursal> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,

    val query: String = "",

    // Formulario (alta/edición)
    val formAbierto: Boolean = false,
    val editandoId: Long? = null, // null = nueva
    val nombre: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val saving: Boolean = false,
    val formError: UiText? = null,

    val mensaje: UiText? = null,
) {
    val esEdicion: Boolean get() = editandoId != null

    val sucursalesVisibles: List<Sucursal>
        get() = if (query.isBlank()) sucursales
        else sucursales.filter { it.nombre.contains(query.trim(), ignoreCase = true) }
}
