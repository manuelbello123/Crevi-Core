package com.example.tienda.feature.usuarios.ui
import com.example.tienda.feature.usuarios.data.UsuarioRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tienda.R
import com.example.tienda.core.enums.UserRole
import com.example.tienda.feature.usuarios.domain.Usuario
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.error.asUiText
import com.example.tienda.core.session.SessionManager
import com.example.tienda.feature.sucursales.data.SucursalRepository
import com.example.tienda.core.util.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Módulo Usuarios (solo admin): lista/búsqueda + alta, edición, activar/desactivar
 * (no a sí mismo), reasignar sucursal y resetear contraseña. Mismo patrón que
 * Clientes: [UsuariosUiState.mode] para el formulario, [UsuariosUiState.dialogo]
 * para acciones cortas.
 */
class UsuariosViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val sucursalRepository: SucursalRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(UsuariosUiState(miId = sessionManager.getSession()?.id))
    val state: StateFlow<UsuariosUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadUsuarios()
        loadSucursales()
    }

    // ── Lista / búsqueda ──

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadUsuarios()
        }
    }

    fun limpiarBusqueda() = onQueryChange("")

    fun retry() = loadUsuarios()

    fun refresh() = loadUsuarios(refreshing = true)

    // ── Sub-navegación (formulario) ──

    fun abrirNuevo() = _state.update { it.copy(mode = UsuariosMode.NUEVO, editando = null, formError = null) }

    fun abrirEditar(usuario: Usuario) =
        _state.update { it.copy(mode = UsuariosMode.EDITAR, editando = usuario, formError = null) }

    fun cerrarForm() =
        _state.update { it.copy(mode = UsuariosMode.LISTA, editando = null, formError = null, saving = false) }

    // ── Diálogos ──

    fun abrirReasignarSucursal(usuario: Usuario) =
        _state.update { it.copy(dialogo = UsuariosDialog.ReasignarSucursal(usuario), formError = null) }

    fun abrirResetPassword(usuario: Usuario) =
        _state.update { it.copy(dialogo = UsuariosDialog.ResetPassword(usuario), formError = null) }

    fun abrirConfirmarEstado(usuario: Usuario) =
        _state.update { it.copy(dialogo = UsuariosDialog.ConfirmarEstado(usuario)) }

    fun cerrarDialogo() = _state.update { it.copy(dialogo = null, saving = false, formError = null) }

    // ── Alta ──

    fun crear(nombre: String, usuario: String, password: String, rol: UserRole, sucursalId: Long?) {
        if (nombre.isBlank() || usuario.isBlank()) return formError(R.string.error_usuario_requerido)
        if (usuario.trim().length < 4) return formError(R.string.error_usuario_min)
        if (password.length < 8) return formError(R.string.error_password_min)
        if (rol == UserRole.GERENTE && sucursalId == null) return formError(R.string.error_sucursal_requerida)

        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            // El admin se asigna a la matriz; el gerente requiere sucursalId.
            val sucursal = if (rol == UserRole.GERENTE) sucursalId else null
            onFormResult(usuarioRepository.crear(nombre, usuario, password, rol, sucursal), R.string.usuario_creado)
        }
    }

    // ── Edición (nombre y usuario) ──

    fun guardarEdicion(id: Long, nombre: String, usuario: String) {
        if (nombre.isBlank() || usuario.isBlank()) return formError(R.string.error_usuario_requerido)
        if (usuario.trim().length < 4) return formError(R.string.error_usuario_min)

        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            onFormResult(usuarioRepository.actualizar(id, nombre, usuario), R.string.usuario_actualizado)
        }
    }

    // ── Reasignar sucursal ──

    fun reasignarSucursal(sucursalId: Long) {
        val usuario = (_state.value.dialogo as? UsuariosDialog.ReasignarSucursal)?.usuario ?: return
        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            when (val result = usuarioRepository.reasignarSucursal(usuario.id, sucursalId)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(saving = false, dialogo = null, mensaje = UiText.Resource(R.string.usuario_reasignado)) }
                    loadUsuarios()
                }
                is NetworkResult.Error -> _state.update { it.copy(saving = false, formError = result.error.asUiText()) }
            }
        }
    }

    // ── Resetear contraseña ──

    fun resetPassword(passwordNueva: String) {
        val usuario = (_state.value.dialogo as? UsuariosDialog.ResetPassword)?.usuario ?: return
        if (passwordNueva.length < 8) return formError(R.string.error_password_min)

        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            when (val result = usuarioRepository.resetPassword(usuario.id, passwordNueva)) {
                is NetworkResult.Success ->
                    _state.update { it.copy(saving = false, dialogo = null, mensaje = UiText.Resource(R.string.password_reseteada)) }
                is NetworkResult.Error -> _state.update { it.copy(saving = false, formError = result.error.asUiText()) }
            }
        }
    }

    // ── Activar / desactivar ──

    fun confirmarEstado() {
        val usuario = (_state.value.dialogo as? UsuariosDialog.ConfirmarEstado)?.usuario ?: return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            when (val result = usuarioRepository.cambiarActivo(usuario.id, !usuario.activo)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(saving = false, dialogo = null, mensaje = UiText.Resource(R.string.usuario_estado_actualizado)) }
                    loadUsuarios()
                }
                is NetworkResult.Error -> _state.update { it.copy(saving = false, dialogo = null, error = result.error.asUiText()) }
            }
        }
    }

    // ── Limpieza ──

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearMensaje() = _state.update { it.copy(mensaje = null) }

    // ── Privados ──

    private fun formError(res: Int) {
        _state.update { it.copy(formError = UiText.Resource(res)) }
    }

    private fun onFormResult(result: NetworkResult<Usuario>, successRes: Int) {
        when (result) {
            is NetworkResult.Success -> {
                _state.update {
                    it.copy(saving = false, mode = UsuariosMode.LISTA, editando = null, mensaje = UiText.Resource(successRes))
                }
                loadUsuarios()
            }
            is NetworkResult.Error -> _state.update { it.copy(saving = false, formError = result.error.asUiText()) }
        }
    }

    private fun loadUsuarios(refreshing: Boolean = false) {
        val q = _state.value.query
        _state.update { it.copy(isLoading = !refreshing, isRefreshing = refreshing, error = null) }
        viewModelScope.launch {
            when (val result = usuarioRepository.listar(q = q.ifBlank { null }, pagina = 1, tamano = 100)) {
                is NetworkResult.Success -> _state.update { it.copy(isLoading = false, isRefreshing = false, usuarios = result.data) }
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

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
