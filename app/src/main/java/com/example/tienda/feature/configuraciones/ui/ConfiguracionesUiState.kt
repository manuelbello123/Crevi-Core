package com.example.tienda.feature.configuraciones.ui

import com.example.tienda.core.util.UiText
import com.example.tienda.feature.configuraciones.data.ModoTema

data class ConfiguracionesUiState(
    val nombreUsuario: String = "",
    val rol: String = "",
    val esAdministrador: Boolean = false,

    // Toggles locales
    val biometricEnrollable: Boolean = false,
    val biometricActivo: Boolean = false,
    val confirmarAccionesCriticas: Boolean = true,
    val modoTema: ModoTema = ModoTema.SISTEMA,

    // Login de clientes (flag global del backend; solo admin lo ve/cambia)
    val loginClientesDeshabilitado: Boolean = false,
    val loginClientesCargando: Boolean = false,

    // Diálogo de cambio de contraseña
    val cambiarPassAbierto: Boolean = false,
    val passwordActual: String = "",
    val passwordNueva: String = "",
    val passwordConfirmar: String = "",
    val cambiandoPass: Boolean = false,
    val cambiarPassError: UiText? = null,

    // Diálogo de confirmación de "restablecer datos"
    val confirmarRestablecerAbierto: Boolean = false,

    val versionApp: String = "",
    val mensaje: UiText? = null,
    val error: UiText? = null,
)
