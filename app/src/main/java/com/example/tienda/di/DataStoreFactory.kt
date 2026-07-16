package com.example.tienda.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

/**
 * DataStore Preferences con nombre de archivo propio. Usamos dos:
 *  - "session"     → sesión (token cifrado); se limpia en logout/401.
 *  - "credentials" → credenciales para biometría; sobrevive al logout normal.
 */
fun createDataStore(context: Context, name: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(name) }
    )
