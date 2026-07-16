package com.example.tienda.feature.sucursales.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tienda.R
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.error.asUiText
import com.example.tienda.feature.sucursales.data.SucursalRepository
import com.example.tienda.feature.sucursales.domain.Sucursal
import com.example.tienda.core.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Gestión de sucursales (solo admin): listar + buscar + crear/editar.
 */
class SucursalesViewModel(
    private val sucursalRepository: SucursalRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SucursalesUiState())
    val state: StateFlow<SucursalesUiState> = _state.asStateFlow()

    init { load() }

    fun onQueryChange(value: String) = _state.update { it.copy(query = value) }

    fun retry() = load()

    fun refresh() = load(refreshing = true)

    // ── Formulario ──

    fun abrirNueva() = _state.update {
        it.copy(formAbierto = true, editandoId = null, nombre = "", direccion = "", telefono = "", formError = null)
    }

    fun abrirEditar(s: Sucursal) = _state.update {
        it.copy(
            formAbierto = true,
            editandoId = s.id,
            nombre = s.nombre,
            direccion = s.direccion.orEmpty(),
            telefono = s.telefono.orEmpty(),
            formError = null,
        )
    }

    fun cerrarForm() = _state.update { it.copy(formAbierto = false, saving = false, formError = null) }

    fun onNombreChange(v: String) = _state.update { it.copy(nombre = v) }
    fun onDireccionChange(v: String) = _state.update { it.copy(direccion = v) }
    fun onTelefonoChange(v: String) = _state.update { it.copy(telefono = v) }

    fun guardar() {
        val s = _state.value
        if (s.nombre.isBlank()) {
            _state.update { it.copy(formError = UiText.Resource(R.string.error_sucursal_nombre)) }
            return
        }
        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            val result = if (s.editandoId == null) {
                sucursalRepository.crear(s.nombre, s.direccion, s.telefono)
            } else {
                sucursalRepository.editar(s.editandoId, s.nombre, s.direccion, s.telefono)
            }
            when (result) {
                is NetworkResult.Success -> {
                    val msg = if (s.editandoId == null) R.string.sucursal_creada else R.string.sucursal_actualizada
                    _state.update { it.copy(saving = false, formAbierto = false, mensaje = UiText.Resource(msg)) }
                    load()
                }
                is NetworkResult.Error -> _state.update { it.copy(saving = false, formError = result.error.asUiText()) }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearMensaje() = _state.update { it.copy(mensaje = null) }

    private fun load(refreshing: Boolean = false) {
        _state.update { it.copy(isLoading = !refreshing, isRefreshing = refreshing, error = null) }
        viewModelScope.launch {
            when (val r = sucursalRepository.listar()) {
                is NetworkResult.Success -> _state.update { it.copy(isLoading = false, isRefreshing = false, sucursales = r.data) }
                is NetworkResult.Error -> _state.update { it.copy(isLoading = false, isRefreshing = false, error = r.error.asUiText()) }
            }
        }
    }
}
