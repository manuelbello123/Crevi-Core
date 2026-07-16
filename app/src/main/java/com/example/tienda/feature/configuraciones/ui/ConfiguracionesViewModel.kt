package com.example.tienda.feature.configuraciones.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tienda.R
import com.example.tienda.core.enums.UserRole
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.error.asUiText
import com.example.tienda.core.security.CredentialStore
import com.example.tienda.core.session.SessionManager
import com.example.tienda.core.util.UiText
import com.example.tienda.feature.configuraciones.data.AuthRepository
import com.example.tienda.feature.configuraciones.data.ConfigRepository
import com.example.tienda.feature.configuraciones.data.ModoTema
import com.example.tienda.feature.configuraciones.data.PreferenciasApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Configuraciones de la app: cambio de contraseña (contra backend), toggles
 * locales de comportamiento (biometría, confirmar acciones) y restablecer datos.
 */
class ConfiguracionesViewModel(
    private val authRepository: AuthRepository,
    private val configRepository: ConfigRepository,
    private val preferencias: PreferenciasApp,
    private val credentialStore: CredentialStore,
    private val sessionManager: SessionManager,
    versionApp: String,
) : ViewModel() {

    private val _state = MutableStateFlow(ConfiguracionesUiState(versionApp = versionApp))
    val state: StateFlow<ConfiguracionesUiState> = _state.asStateFlow()

    init {
        val session = sessionManager.getSession()
        _state.update {
            it.copy(
                nombreUsuario = session?.nombre.orEmpty(),
                rol = rolLabel(session?.rol),
                esAdministrador = session?.esAdministrador == true,
            )
        }
        // Todos los toggles locales se colectan reactivamente: cualquier cambio en el
        // DataStore/credentialStore (guardar o borrar) refresca el estado sin lag.
        viewModelScope.launch {
            credentialStore.enabledFlow.collect { activo ->
                _state.update { it.copy(biometricActivo = activo) }
            }
        }
        viewModelScope.launch {
            preferencias.confirmarAccionesCriticas.collect { v ->
                _state.update { it.copy(confirmarAccionesCriticas = v) }
            }
        }
        viewModelScope.launch {
            preferencias.modoTema.collect { v ->
                _state.update { it.copy(modoTema = v) }
            }
        }
        // El flag de login de clientes es global del backend (solo admin puede leerlo/cambiarlo).
        if (_state.value.esAdministrador) cargarLoginClientes()
    }

    private fun cargarLoginClientes() {
        _state.update { it.copy(loginClientesCargando = true) }
        viewModelScope.launch {
            when (val r = configRepository.obtenerLoginClientesDeshabilitado()) {
                is NetworkResult.Success -> _state.update { it.copy(loginClientesCargando = false, loginClientesDeshabilitado = r.data) }
                is NetworkResult.Error -> _state.update { it.copy(loginClientesCargando = false, error = r.error.asUiText()) }
            }
        }
    }

    /** La View reporta si el device puede autenticar biométricamente. */
    fun setBiometricEnrollable(enrollable: Boolean) {
        _state.update { it.copy(biometricEnrollable = enrollable) }
    }

    // ── Cambio de contraseña ──

    fun abrirCambiarPass() = _state.update {
        it.copy(cambiarPassAbierto = true, passwordActual = "", passwordNueva = "", passwordConfirmar = "", cambiarPassError = null)
    }

    fun cerrarCambiarPass() = _state.update { it.copy(cambiarPassAbierto = false, cambiandoPass = false, cambiarPassError = null) }

    fun onPassActualChange(v: String) = _state.update { it.copy(passwordActual = v, cambiarPassError = null) }
    fun onPassNuevaChange(v: String) = _state.update { it.copy(passwordNueva = v, cambiarPassError = null) }
    fun onPassConfirmarChange(v: String) = _state.update { it.copy(passwordConfirmar = v, cambiarPassError = null) }

    fun guardarNuevaPass() {
        val s = _state.value
        when {
            s.passwordActual.isBlank() -> return err(R.string.error_pass_actual_requerida)
            s.passwordNueva.length < 8 -> return err(R.string.error_pass_min)
            s.passwordNueva == s.passwordActual -> return err(R.string.error_pass_igual_actual)
            s.passwordConfirmar != s.passwordNueva -> return err(R.string.error_pass_no_coincide)
        }
        _state.update { it.copy(cambiandoPass = true, cambiarPassError = null) }
        viewModelScope.launch {
            when (val r = authRepository.cambiarPassword(s.passwordActual, s.passwordNueva)) {
                is NetworkResult.Success -> {
                    // La contraseña cambió; invalidamos credenciales biométricas (quedaron obsoletas).
                    credentialStore.clear()
                    _state.update {
                        it.copy(
                            cambiandoPass = false,
                            cambiarPassAbierto = false,
                            biometricActivo = false,
                            mensaje = UiText.Resource(R.string.password_actualizada),
                        )
                    }
                }
                is NetworkResult.Error -> _state.update { it.copy(cambiandoPass = false, cambiarPassError = r.error.asUiText()) }
            }
        }
    }

    // ── Toggles ──

    fun toggleConfirmar(activo: Boolean) {
        viewModelScope.launch { preferencias.setConfirmarAccionesCriticas(activo) }
    }

    fun toggleLoginClientes(activo: Boolean) {
        val previo = _state.value.loginClientesDeshabilitado
        // Optimista: refleja el cambio ya, y revierte si el backend lo rechaza.
        _state.update { it.copy(loginClientesDeshabilitado = activo, loginClientesCargando = true) }
        viewModelScope.launch {
            when (val r = configRepository.setLoginClientesDeshabilitado(activo)) {
                is NetworkResult.Success -> _state.update { it.copy(loginClientesCargando = false, loginClientesDeshabilitado = r.data) }
                is NetworkResult.Error -> _state.update {
                    it.copy(loginClientesCargando = false, loginClientesDeshabilitado = previo, error = r.error.asUiText())
                }
            }
        }
    }

    fun setModoTema(modo: ModoTema) {
        viewModelScope.launch { preferencias.setModoTema(modo) }
    }

    /** Desactiva la biometría borrando las credenciales guardadas. */
    fun desactivarBiometrica() {
        viewModelScope.launch {
            credentialStore.clear()
            _state.update { it.copy(mensaje = UiText.Resource(R.string.biometrico_desactivado)) }
        }
    }

    // ── Restablecer datos ──

    fun abrirRestablecer() = _state.update { it.copy(confirmarRestablecerAbierto = true) }
    fun cerrarRestablecer() = _state.update { it.copy(confirmarRestablecerAbierto = false) }

    fun restablecerDatos() {
        viewModelScope.launch {
            credentialStore.clear()
            preferencias.limpiar()
            _state.update { it.copy(confirmarRestablecerAbierto = false) }
            sessionManager.endSession() // manda a login
        }
    }

    fun clearMensaje() = _state.update { it.copy(mensaje = null) }
    fun clearError() = _state.update { it.copy(error = null) }

    private fun err(res: Int) {
        _state.update { it.copy(cambiarPassError = UiText.Resource(res)) }
    }

    private fun rolLabel(rol: UserRole?): String = when (rol) {
        UserRole.ADMINISTRADOR -> "Administrador"
        UserRole.GERENTE -> "Gerente"
        null -> ""
    }
}
