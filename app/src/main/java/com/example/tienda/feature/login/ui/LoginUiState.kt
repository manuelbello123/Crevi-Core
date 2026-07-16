package com.example.tienda.feature.login.ui

import com.example.tienda.core.util.UiText

/**
 * Estado de UI de la pantalla de login. La navegación al home la dispara
 * `sessionFlow` (fuente de verdad), por eso aquí NO hay un `isLoggedIn`.
 */
data class LoginUiState(
    val usuario: String = "",
    val password: String = "",
    /** true mientras corre la petición → deshabilita el botón y muestra spinner. */
    val isLoading: Boolean = false,
    /** Error listo para mostrar en el snackbar (ya como UiText). */
    val errorMessage: UiText? = null,
    /** true si hay un refresh token guardado para ofrecer el acceso biométrico. */
    val canOfferBiometric: Boolean = false,
) {
    /** El botón "Entrar" solo se habilita con ambos campos y sin carga. */
    val canSubmit: Boolean
        get() = usuario.isNotBlank() && password.isNotBlank() && !isLoading
}
