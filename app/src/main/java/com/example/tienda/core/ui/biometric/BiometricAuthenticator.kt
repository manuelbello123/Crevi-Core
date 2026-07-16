package com.example.tienda.core.ui.biometric

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Disponibilidad de la biometría en el dispositivo. */
enum class BiometricStatus {
    /** Hay hardware y al menos una huella/rostro registrado → se puede usar. */
    AVAILABLE,

    /** Hay hardware pero el usuario no ha registrado biometría en el sistema. */
    NOT_ENROLLED,

    /** El dispositivo no tiene sensor biométrico. */
    NO_HARDWARE,

    /** Temporalmente no disponible u otro estado no usable. */
    UNAVAILABLE,
}

/** Resultado de un intento de autenticación biométrica. */
sealed interface BiometricAuthResult {
    /** El usuario se autenticó correctamente. */
    data object Success : BiometricAuthResult

    /** El usuario canceló (botón cancelar / gesto). No es un error a mostrar. */
    data object Cancelled : BiometricAuthResult

    /** Falló por error del sistema (bloqueo por reintentos, etc.). */
    data class Failed(val message: String) : BiometricAuthResult
}

/**
 * Puerta biométrica del dispositivo (huella / rostro) — enfoque "gate", igual
 * que la app de clientes. NO cifra nada por sí misma: solo verifica al usuario.
 * Las credenciales viven cifradas por el OS en [com.example.tienda.core.security.CredentialStore]
 * y, tras pasar la puerta, se usan para un login real contra el backend (token fresco).
 *
 * Requiere una [FragmentActivity] (por eso vive en la capa de UI, no en el ViewModel).
 * Usa biometría fuerte (clase 3) sin CryptoObject.
 */
class BiometricAuthenticator(
    private val activity: FragmentActivity,
) {

    /** Estado actual del sensor biométrico. */
    fun status(): BiometricStatus =
        when (BiometricManager.from(activity).canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.NO_HARDWARE
            else -> BiometricStatus.UNAVAILABLE
        }

    /** ¿El dispositivo puede autenticar con biometría fuerte enrolada? */
    fun canAuthenticate(): Boolean = status() == BiometricStatus.AVAILABLE

    /**
     * Muestra el prompt biométrico del sistema y suspende hasta el resultado.
     * Sin CryptoObject: solo verifica al usuario (enfoque "gate").
     */
    suspend fun authenticate(title: String, subtitle: String? = null): BiometricAuthResult =
        suspendCancellableCoroutine { cont ->
            val prompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (cont.isActive) cont.resume(BiometricAuthResult.Success)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (!cont.isActive) return
                        val cancelado = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                            errorCode == BiometricPrompt.ERROR_CANCELED
                        cont.resume(
                            if (cancelado) BiometricAuthResult.Cancelled
                            else BiometricAuthResult.Failed(errString.toString()),
                        )
                    }

                    // Un intento fallido deja el prompt abierto para reintentar;
                    // solo onError/onSucceeded terminan el flujo.
                    override fun onAuthenticationFailed() = Unit
                },
            )

            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .apply { subtitle?.let(::setSubtitle) }
                .setNegativeButtonText("Cancelar")
                .setConfirmationRequired(false)
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .build()

            prompt.authenticate(info)
            cont.invokeOnCancellation { prompt.cancelAuthentication() }
        }
}

/** Crea un [BiometricAuthenticator] si el Context resuelve a una FragmentActivity. */
@Composable
fun rememberBiometricAuthenticator(): BiometricAuthenticator? {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    return remember(activity) { activity?.let { BiometricAuthenticator(it) } }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
