package com.example.tienda.core.session

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.tienda.core.enums.UserRole
import com.example.tienda.core.session.SessionData
import com.example.tienda.core.security.CryptoManager
import com.example.tienda.core.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * Implementación de [SessionManager] sobre DataStore Preferences. El token se
 * persiste CIFRADO con [CryptoManager]; el resto de campos en claro (no son
 * secretos). El token descifrado vive solo en memoria, en [_session].
 */
class DataStoreSessionManager(
    private val dataStore: DataStore<Preferences>,
    private val crypto: CryptoManager,
) : SessionManager {

    private val _session = MutableStateFlow<SessionData?>(null)
    override val sessionFlow: StateFlow<SessionData?> = _session.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    // Evento UI efímero: no se persiste, solo sobrevive hasta que login lo muestra.
    private val _pendingLogoutMessage = MutableStateFlow<UiText?>(null)
    override val pendingLogoutMessage: StateFlow<UiText?> = _pendingLogoutMessage.asStateFlow()

    override suspend fun initialize() {
        val prefs = dataStore.data.first()
        val session = prefs.toSessionData()

        when {
            session == null                -> _session.value = null
            isTokenExpired(session.token)  -> endSession()   // exp del JWT cumplido → limpiar
            else                           -> _session.value = session
        }
        _isReady.value = true
    }

    override suspend fun startSession(session: SessionData) {
        val encryptedToken = crypto.encrypt(session.token) ?: return
        val encryptedRefresh = crypto.encrypt(session.refreshToken) ?: return
        dataStore.edit { prefs ->
            prefs[SessionPreferencesKeys.TOKEN]         = encryptedToken
            prefs[SessionPreferencesKeys.REFRESH_TOKEN] = encryptedRefresh
            prefs[SessionPreferencesKeys.ID]         = session.id
            prefs[SessionPreferencesKeys.NOMBRE]     = session.nombre
            prefs[SessionPreferencesKeys.ROL]        = session.rol.name

            session.sucursalId
                ?.let { prefs[SessionPreferencesKeys.SUCURSAL_ID] = it }
                ?: prefs.remove(SessionPreferencesKeys.SUCURSAL_ID)
            session.usuario
                ?.let { prefs[SessionPreferencesKeys.USUARIO] = it }
                ?: prefs.remove(SessionPreferencesKeys.USUARIO)
        }
        _session.value = session
        _pendingLogoutMessage.value = null
    }

    override suspend fun endSession() {
        dataStore.edit { it.clear() }
        _session.value = null
    }

    override suspend fun endSessionWithReason(reason: UiText) {
        // setear ANTES de limpiar para que la navegación a login ya tenga el mensaje
        _pendingLogoutMessage.value = reason
        dataStore.edit { it.clear() }
        _session.value = null
    }

    override fun consumeLogoutMessage() {
        _pendingLogoutMessage.value = null
    }

    override fun getSession(): SessionData? = _session.value
    override fun getToken(): String? = _session.value?.token

    // ── mapeo Preferences → dominio ──

    private fun Preferences.toSessionData(): SessionData? {
        val encryptedToken = this[SessionPreferencesKeys.TOKEN] ?: return null
        val token  = crypto.decrypt(encryptedToken) ?: return null   // clave invalidada → sin sesión
        val encryptedRefresh = this[SessionPreferencesKeys.REFRESH_TOKEN] ?: return null
        val refreshToken = crypto.decrypt(encryptedRefresh) ?: return null
        val id     = this[SessionPreferencesKeys.ID]     ?: return null
        val nombre = this[SessionPreferencesKeys.NOMBRE] ?: return null
        val rol    = this[SessionPreferencesKeys.ROL]    ?: return null

        return SessionData(
            token        = token,
            refreshToken = refreshToken,
            id           = id,
            nombre       = nombre,
            rol          = UserRole.from(rol),
            sucursalId   = this[SessionPreferencesKeys.SUCURSAL_ID],
            usuario      = this[SessionPreferencesKeys.USUARIO],
        )
    }

    /**
     * Expiración según el claim `exp` del propio JWT (segundos Unix) → siempre
     * coincide con lo que emitió el backend, sin acoplar constantes. Si no se
     * puede leer `exp`, no forzamos logout aquí: dejamos que un 401 lo maneje.
     */
    private fun isTokenExpired(token: String): Boolean {
        val expMs = jwtExpiraEnMs(token) ?: return false
        return System.currentTimeMillis() >= expMs
    }

    private fun jwtExpiraEnMs(token: String): Long? = runCatching {
        val payload = token.split(".").getOrNull(1) ?: return null
        val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP), Charsets.UTF_8)
        Regex("\"exp\"\\s*:\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toLong()?.times(1000)
    }.getOrNull()
}
