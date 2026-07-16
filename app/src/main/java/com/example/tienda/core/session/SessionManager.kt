package com.example.tienda.core.session

import com.example.tienda.core.session.SessionData
import com.example.tienda.core.util.UiText
import kotlinx.coroutines.flow.StateFlow

/**
 * Fuente de verdad de la sesión del operador. La navegación observa
 * [sessionFlow] (null = ir a login) y [isReady] (false = aún restaurando,
 * mostrar splash).
 */
interface SessionManager {

    /** Sesión activa o null. */
    val sessionFlow: StateFlow<SessionData?>

    /** false hasta que [initialize] termina de restaurar la sesión guardada. */
    val isReady: StateFlow<Boolean>

    /**
     * Mensaje pendiente tras un logout FORZADO por el backend (401: token
     * expirado o usuario desactivado). La pantalla de login lo muestra y luego
     * llama [consumeLogoutMessage]. null = sin mensaje.
     */
    val pendingLogoutMessage: StateFlow<UiText?>

    /** Restaura la sesión persistida (valida expiración). Llamar al arrancar. */
    suspend fun initialize()

    suspend fun startSession(session: SessionData)

    /** Logout normal (el usuario pulsa salir): limpia sesión sin mensaje. */
    suspend fun endSession()

    /** Logout forzado: limpia sesión y deja un [reason] para mostrar en login. */
    suspend fun endSessionWithReason(reason: UiText)

    /** Limpia el mensaje pendiente tras mostrarlo. */
    fun consumeLogoutMessage()

    fun getSession(): SessionData?

    /** Token actual (ya descifrado en memoria) para el header Authorization. */
    fun getToken(): String?
}
