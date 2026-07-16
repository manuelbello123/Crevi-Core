package com.example.tienda.core.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Guarda el REFRESH TOKEN para el re-login biométrico (enfoque "gate"): la
 * biometría solo desbloquea el acceso a este token, que se canjea por una sesión
 * fresca contra el backend (POST /auth/refresh). NO se guarda la contraseña, así
 * que un dispositivo comprometido no expone la credencial real; y el token es
 * revocable server-side (logout, cambio de contraseña, desactivación).
 *
 * El token se cifra en reposo con [CryptoManager] (clave AES/GCM del Keystore, no
 * biométrica). Vive en un DataStore PROPIO (separado del de sesión) para que el
 * logout normal o el 401 no lo borren.
 */
class CredentialStore(
    private val dataStore: DataStore<Preferences>,
    private val crypto: CryptoManager,
) {

    /** Emite `true` cuando hay un refresh token guardado; reactivo al DataStore. */
    val enabledFlow: Flow<Boolean> = dataStore.data
        .map { it[KEY_TOKEN] != null }
        .distinctUntilChanged()

    /** true si el ingreso biométrico está activado (hay refresh token guardado). */
    suspend fun isEnabled(): Boolean = dataStore.data.first()[KEY_TOKEN] != null

    /** Habilita/actualiza el ingreso biométrico persistiendo el refresh token cifrado. */
    suspend fun save(refreshToken: String) {
        val encrypted = crypto.encrypt(refreshToken) ?: return
        dataStore.edit { it[KEY_TOKEN] = encrypted }
    }

    /** Refresh token guardado, o `null` si no hay o el descifrado falla. */
    suspend fun token(): String? {
        val stored = dataStore.data.first()[KEY_TOKEN] ?: return null
        return crypto.decrypt(stored)
    }

    /** Deshabilita el ingreso biométrico y borra el refresh token guardado. */
    suspend fun clear() {
        dataStore.edit { it.remove(KEY_TOKEN) }
    }

    private companion object {
        val KEY_TOKEN = stringPreferencesKey("biometric_refresh_token")
    }
}
