package com.example.tienda.core.session

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SessionPreferencesKeys {
    /** Token JWT CIFRADO (Base64 de IV+ciphertext). Nunca se guarda en claro. */
    val TOKEN         = stringPreferencesKey("session_token")
    /** Refresh token opaco CIFRADO. Se usa para /auth/refresh (biometría o refresco silencioso). */
    val REFRESH_TOKEN = stringPreferencesKey("session_refresh_token")
    val ID          = longPreferencesKey("session_id")
    val NOMBRE      = stringPreferencesKey("session_nombre")
    val ROL         = stringPreferencesKey("session_rol")
    val SUCURSAL_ID = longPreferencesKey("session_sucursal_id")
    val USUARIO     = stringPreferencesKey("session_usuario")
}
