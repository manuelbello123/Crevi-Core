package com.example.tienda.feature.clientes.ui
import com.example.tienda.feature.clientes.data.ClienteRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tienda.R
import com.example.tienda.feature.clientes.domain.Cliente
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.error.asUiText
import com.example.tienda.core.session.SessionManager
import com.example.tienda.feature.sucursales.data.SucursalRepository
import com.example.tienda.feature.usuarios.data.UsuarioRepository
import com.example.tienda.core.util.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Módulo Clientes: lista/búsqueda (debounce) + alta, edición, activar/desactivar,
 * reasignación (admin) y acceso a la app. La sub-navegación lista↔formulario se
 * modela con [ClientesUiState.mode]; las acciones cortas con [ClientesUiState.dialogo].
 */
class ClientesViewModel(
    private val clienteRepository: ClienteRepository,
    private val sucursalRepository: SucursalRepository,
    private val usuarioRepository: UsuarioRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val esAdmin: Boolean = sessionManager.getSession()?.esAdministrador == true

    private val _state = MutableStateFlow(ClientesUiState(esAdministrador = esAdmin))
    val state: StateFlow<ClientesUiState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var sucursalCargada = false

    init {
        if (esAdmin) {
            loadSucursales()
            loadOperadores()
        }
        // La primera carga la dispara setSucursal() desde el Home (igual que Cobranzas),
        // para que la lista quede acotada a la sucursal en contexto.
    }

    /** El switch de sucursal del Home fija el alcance de la lista. */
    fun setSucursal(sucursalId: Long?) {
        if (sucursalCargada && sucursalId == _state.value.selectedSucursalId) return
        sucursalCargada = true
        _state.update { it.copy(selectedSucursalId = sucursalId) }
        loadClientes()
    }

    // ── Lista / búsqueda ──

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadClientes()
        }
    }

    fun limpiarBusqueda() = onQueryChange("")

    fun retry() = loadClientes()

    /** Pull-to-refresh: recarga sin el spinner central. */
    fun refresh() = loadClientes(refreshing = true)

    // ── Sub-navegación (formulario) ──

    fun abrirNuevo() = _state.update { it.copy(mode = ClientesMode.NUEVO, editando = null, formError = null) }

    fun abrirEditar(cliente: Cliente) =
        _state.update { it.copy(mode = ClientesMode.EDITAR, editando = cliente, formError = null) }

    fun cerrarForm() =
        _state.update { it.copy(mode = ClientesMode.LISTA, editando = null, formError = null, saving = false) }

    // ── Diálogos (acciones cortas) ──

    fun abrirReasignar(cliente: Cliente) =
        _state.update { it.copy(dialogo = ClientesDialog.Reasignar(cliente), formError = null) }

    fun abrirAccesoApp(cliente: Cliente) =
        _state.update { it.copy(dialogo = ClientesDialog.AccesoApp(cliente), formError = null) }

    fun abrirConfirmarEstado(cliente: Cliente) =
        _state.update { it.copy(dialogo = ClientesDialog.ConfirmarEstado(cliente)) }

    fun cerrarDialogo() = _state.update { it.copy(dialogo = null, saving = false, formError = null) }

    // ── Alta ──

    fun crear(
        nombreCompleto: String,
        direccion: String?,
        telefono: String?,
        sucursalId: Long?,
        gerenteId: Long?,
        accesoApp: Boolean,
        password: String?,
    ) {
        if (nombreCompleto.isBlank()) return formError(R.string.error_nombre_requerido)
        if (accesoApp && password.isNullOrBlank()) return formError(R.string.error_password_requerido)

        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            onFormResult(
                clienteRepository.crear(nombreCompleto, direccion, telefono, sucursalId, gerenteId, accesoApp, password),
                R.string.cliente_creado,
            )
        }
    }

    // ── Edición ──

    fun guardarEdicion(id: Long, nombreCompleto: String, direccion: String?, telefono: String?) {
        if (nombreCompleto.isBlank()) return formError(R.string.error_nombre_requerido)

        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            onFormResult(clienteRepository.actualizar(id, nombreCompleto, direccion, telefono), R.string.cliente_actualizado)
        }
    }

    // ── Reasignar (admin) ──

    fun reasignar(sucursalId: Long, gerenteId: Long) {
        val cliente = (_state.value.dialogo as? ClientesDialog.Reasignar)?.cliente ?: return
        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            when (val result = clienteRepository.reasignar(cliente.id, sucursalId, gerenteId)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(saving = false, dialogo = null, mensaje = UiText.Resource(R.string.cliente_reasignado)) }
                    loadClientes()
                }
                is NetworkResult.Error -> _state.update { it.copy(saving = false, formError = result.error.asUiText()) }
            }
        }
    }

    // ── Acceso a la app ──

    fun cambiarAccesoApp(accesoApp: Boolean, password: String?) {
        val cliente = (_state.value.dialogo as? ClientesDialog.AccesoApp)?.cliente ?: return
        if (accesoApp && password.isNullOrBlank()) return formError(R.string.error_password_requerido)

        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            when (val result = clienteRepository.cambiarAccesoApp(cliente.id, accesoApp, password)) {
                is NetworkResult.Success ->
                    _state.update { it.copy(saving = false, dialogo = null, mensaje = UiText.Resource(R.string.acceso_app_actualizado)) }
                is NetworkResult.Error -> _state.update { it.copy(saving = false, formError = result.error.asUiText()) }
            }
        }
    }

    // ── Activar / desactivar (desde confirmación) ──

    fun confirmarEstado() {
        val cliente = (_state.value.dialogo as? ClientesDialog.ConfirmarEstado)?.cliente ?: return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            when (val result = clienteRepository.cambiarActivo(cliente.id, !cliente.activo)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(saving = false, dialogo = null, mensaje = UiText.Resource(R.string.cliente_estado_actualizado)) }
                    loadClientes()
                }
                is NetworkResult.Error -> _state.update { it.copy(saving = false, dialogo = null, error = result.error.asUiText()) }
            }
        }
    }

    // ── Limpieza de eventos ──

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearMensaje() = _state.update { it.copy(mensaje = null) }

    // ── Privados ──

    private fun formError(res: Int) {
        _state.update { it.copy(formError = UiText.Resource(res)) }
    }

    private fun onFormResult(result: NetworkResult<Cliente>, successRes: Int) {
        when (result) {
            is NetworkResult.Success -> {
                _state.update {
                    it.copy(saving = false, mode = ClientesMode.LISTA, editando = null, mensaje = UiText.Resource(successRes))
                }
                loadClientes()
            }
            is NetworkResult.Error -> _state.update { it.copy(saving = false, formError = result.error.asUiText()) }
        }
    }

    private fun loadClientes(refreshing: Boolean = false) {
        val q = _state.value.query
        val sucursalId = _state.value.selectedSucursalId
        _state.update { it.copy(isLoading = !refreshing, isRefreshing = refreshing, error = null) }
        viewModelScope.launch {
            when (val result = clienteRepository.listar(q = q.ifBlank { null }, pagina = 1, tamano = 100, sucursalId = sucursalId)) {
                is NetworkResult.Success -> _state.update { it.copy(isLoading = false, isRefreshing = false, clientes = result.data) }
                is NetworkResult.Error -> _state.update { it.copy(isLoading = false, isRefreshing = false, error = result.error.asUiText()) }
            }
        }
    }

    private fun loadSucursales() {
        viewModelScope.launch {
            (sucursalRepository.listar() as? NetworkResult.Success)?.let { res ->
                _state.update { it.copy(sucursales = res.data) }
            }
        }
    }

    private fun loadOperadores() {
        viewModelScope.launch {
            (usuarioRepository.listar(q = null, pagina = 1, tamano = 100) as? NetworkResult.Success)?.let { res ->
                _state.update { it.copy(operadores = res.data) }
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
