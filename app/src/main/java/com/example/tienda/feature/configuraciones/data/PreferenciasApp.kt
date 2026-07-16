package com.example.tienda.feature.configuraciones.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Modo de tema seleccionado por el usuario. */
enum class ModoTema { SISTEMA, CLARO, OSCURO }

/**
 * Preferencias locales de la app (toggles simples). Datos NO sensibles;
 * se guardan en claro en un DataStore propio.
 */
class PreferenciasApp(
    private val dataStore: DataStore<Preferences>,
) {
    /** Pide confirmación adicional en acciones destructivas. */
    val confirmarAccionesCriticas: Flow<Boolean> =
        dataStore.data.map { it[KEY_CONFIRMAR] ?: true }

    /** Modo de tema seleccionado (por ahora informativo — no altera la paleta). */
    val modoTema: Flow<ModoTema> =
        dataStore.data.map { prefs ->
            when (prefs[KEY_TEMA]) {
                "CLARO" -> ModoTema.CLARO
                "OSCURO" -> ModoTema.OSCURO
                else -> ModoTema.SISTEMA
            }
        }

    suspend fun setConfirmarAccionesCriticas(activo: Boolean) {
        dataStore.edit { it[KEY_CONFIRMAR] = activo }
    }

    suspend fun setModoTema(modo: ModoTema) {
        dataStore.edit { it[KEY_TEMA] = modo.name }
    }

    suspend fun limpiar() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val KEY_CONFIRMAR = booleanPreferencesKey("confirmar_acciones_criticas")
        val KEY_TEMA = stringPreferencesKey("modo_tema")
    }
}
