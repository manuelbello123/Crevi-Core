package com.example.tienda.feature.login.ui
import com.example.tienda.feature.login.data.AuthResult
import com.example.tienda.feature.login.data.LoginRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tienda.R
import com.example.tienda.core.session.SessionData
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.error.NetworkError
import com.example.tienda.core.network.error.asUiText
import com.example.tienda.core.security.CredentialStore
import com.example.tienda.core.session.SessionManager
import com.example.tienda.core.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de login. Enfoque biométrico "gate" con REFRESH TOKEN: la biometría
 * solo VERIFICA al usuario (el prompt lo dispara la View, que tiene Activity);
 * tras el éxito se canjea el refresh token guardado por una sesión fresca. Nunca
 * se guarda la contraseña; el refresh token es revocable server-side.
 */
class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val sessionManager: SessionManager,
    private val credentialStore: CredentialStore,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    /** Sesión activa (null = no logueado). La raíz de la app la observa. */
    val sessionFlow: StateFlow<SessionData?> = sessionManager.sessionFlow

    /** Mensaje de logout FORZADO por backend (401), para mostrar al cargar login. */
    val pendingLogoutMessage: StateFlow<UiText?> = sessionManager.pendingLogoutMessage

    init {
        // "Ofrecer biometría" sigue reactivamente al refresh token guardado. Así,
        // tras un logout FORZADO por 401 —que conserva el token— el botón de huella
        // reaparece al volver al login.
        viewModelScope.launch {
            credentialStore.enabledFlow.collect { enabled ->
                _state.update { it.copy(canOfferBiometric = enabled) }
            }
        }
    }

    // ─── inputs ───────────────────────────────────────────────────────

    fun onUsuarioChange(value: String) =
        _state.update { it.copy(usuario = value, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _state.update { it.copy(password = value, errorMessage = null) }

    // ─── login con contraseña ─────────────────────────────────────────

    fun login() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            handleResult(loginRepository.login(current.usuario, current.password))
        }
    }

    // ─── login con biometría (gate → refresh token) ───────────────────

    /**
     * La View llama esto SOLO tras un [com.example.tienda.core.ui.biometric.BiometricAuthResult.Success].
     * Canjea el refresh token guardado por una sesión fresca (y guarda el token rotado).
     */
    fun loginWithBiometric() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val token = credentialStore.token()
            if (token == null) {
                credentialStore.clear()
                _state.update {
                    it.copy(isLoading = false, canOfferBiometric = false, errorMessage = UiText.Resource(R.string.error_biometric_failed))
                }
                return@launch
            }
            when (val result = loginRepository.refresh(token)) {
                is NetworkResult.Success -> {
                    // El backend rotó el token: persistimos el NUEVO para el próximo ingreso.
                    credentialStore.save(result.data.refreshToken)
                    _state.update { it.copy(isLoading = false) }
                }
                is NetworkResult.Error -> {
                    // Token revocado/expirado (401): pide contraseña y borra el guardado.
                    val invalido = result.error is NetworkError.Unauthorized
                    if (invalido) credentialStore.clear()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = if (invalido) {
                                UiText.Resource(R.string.error_biometric_credentials_changed)
                            } else {
                                result.error.asUiText()
                            },
                        )
                    }
                }
            }
        }
    }

    // ─── activar biometría (opt-in tras un login con contraseña) ───────

    /**
     * Guarda el refresh token de la sesión activa en el CredentialStore. La View
     * lo llama tras confirmar con el prompt biométrico. Funciona tanto si el
     * usuario acaba de loguearse con contraseña como si la sesión fue restaurada.
     */
    fun enableBiometric() {
        val token = sessionManager.getSession()?.refreshToken ?: return
        viewModelScope.launch {
            credentialStore.save(token)
            _state.update { it.copy(canOfferBiometric = true) }
        }
    }

    // ─── otros ────────────────────────────────────────────────────────

    fun clearError() = _state.update { it.copy(errorMessage = null) }

    fun consumeLogoutMessage() = sessionManager.consumeLogoutMessage()

    /** Logout normal: revoca el refresh token en el servidor y limpia todo (reset total). */
    fun logout() {
        viewModelScope.launch {
            // Revoca server-side el token de la sesión activa (o el guardado biométrico)
            // mientras el access token sigue vigente — endSession se llama DESPUÉS.
            val token = sessionManager.getSession()?.refreshToken ?: credentialStore.token()
            loginRepository.logout(token)
            credentialStore.clear()
            sessionManager.endSession()
            _state.update {
                it.copy(usuario = "", password = "", errorMessage = null, isLoading = false)
            }
        }
    }

    private fun handleResult(result: NetworkResult<AuthResult>) {
        _state.update {
            when (result) {
                // Éxito: la navegación reacciona a sessionFlow; solo apagamos el spinner.
                is NetworkResult.Success -> it.copy(isLoading = false)
                is NetworkResult.Error -> it.copy(isLoading = false, errorMessage = result.error.toLoginUiText())
            }
        }
    }
}

/** En login, un 401 significa "credenciales inválidas", no "sesión expirada". */
private fun NetworkError.toLoginUiText(): UiText = when (this) {
    is NetworkError.Unauthorized -> UiText.Resource(R.string.error_invalid_credentials)
    else -> asUiText()
}
