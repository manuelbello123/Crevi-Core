package com.example.tienda.feature.login.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tienda.core.ui.biometric.BiometricAuthResult
import com.example.tienda.core.ui.biometric.rememberBiometricAuthenticator
import kotlinx.coroutines.launch

/**
 * Conecta el [LoginViewModel] con [LoginScreen] y orquesta el prompt biométrico
 * "entrar con huella" (el prompt necesita Activity → vive aquí, en UI).
 *
 * Enfoque "gate" igual que la app de clientes: el prompt solo verifica al usuario;
 * al tener éxito se hace un login real con las credenciales guardadas. Además
 * auto-dispara el prompt una vez al abrir el login si la biometría está activada.
 */
@Composable
fun LoginRoute(viewModel: LoginViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val session by viewModel.sessionFlow.collectAsStateWithLifecycle()
    val biometric = rememberBiometricAuthenticator()
    val scope = rememberCoroutineScope()

    val biometricAvailable = remember(biometric) { biometric?.canAuthenticate() == true }

    fun runBiometricLogin() {
        val authenticator = biometric ?: return
        scope.launch {
            when (authenticator.authenticate(
                title = "Entrar con huella",
                subtitle = "Confirma tu identidad para acceder",
            )) {
                BiometricAuthResult.Success -> viewModel.loginWithBiometric()
                BiometricAuthResult.Cancelled -> Unit
                is BiometricAuthResult.Failed -> Unit // el usuario puede usar contraseña
            }
        }
    }

    // Auto-dispara el ingreso biométrico una vez si ya está activado y no hay sesión.
    var autoPrompted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.canOfferBiometric, biometricAvailable) {
        if (state.canOfferBiometric && biometricAvailable && !autoPrompted && session == null) {
            autoPrompted = true
            runBiometricLogin()
        }
    }

    LoginScreen(
        state = state,
        biometricAvailable = biometricAvailable,
        onUsuarioChange = viewModel::onUsuarioChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLogin = viewModel::login,
        onBiometricLogin = ::runBiometricLogin,
        onErrorShown = viewModel::clearError,
    )
}
