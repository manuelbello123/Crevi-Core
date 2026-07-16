package com.example.tienda.core.network.error

import androidx.annotation.StringRes
import com.example.tienda.R
import com.example.tienda.core.util.UiText

/**
 * Falla de red/negocio normalizada para toda la app.
 *
 * Extiende [Throwable] para poder lanzarse desde el HttpResponseValidator
 * (ver NetworkUtils) y volver a atraparse en [safeApiCall].
 *
 * Mapa con el contrato del backend (codigo → HTTP):
 *   VALIDACION 400 · NO_AUTENTICADO 401 · NO_AUTORIZADO 403 · NO_ENCONTRADO 404
 *   CONFLICTO 409 · REGLA_DE_NEGOCIO 422 · DEMASIADAS_PETICIONES 429 · ERROR_INTERNO 500
 */
sealed class NetworkError : Throwable() {

    // ── Validación local (antes de tocar la red) ──
    data object EmptyFields : NetworkError()

    // ── Errores del backend ({error:{codigo,mensaje}}) ──
    /** 401 — token inválido/expirado o usuario desactivado → forzar logout. */
    data object Unauthorized : NetworkError()

    /** 403 — el rol no puede ejecutar esta acción. */
    data class Forbidden(val serverMessage: String? = null) : NetworkError()

    /** 404 — recurso no encontrado. */
    data object NotFound : NetworkError()

    /** 400 — datos inválidos. */
    data class Validation(val serverMessage: String? = null) : NetworkError()

    /** 409 — conflicto (ej. usuario/cliente duplicado). */
    data class Conflict(val serverMessage: String? = null) : NetworkError()

    /** 422 — regla de negocio (ej. abono mayor al saldo, cerrar cuenta con saldo). */
    data class BusinessRule(val serverMessage: String? = null) : NetworkError()

    /** 429 — demasiadas peticiones (ej. 5 logins/min por IP). */
    data object TooManyRequests : NetworkError()

    /** 500 — error interno del servidor. */
    data class ServerError(val serverMessage: String? = null) : NetworkError()

    // ── Transporte / local ──
    data object Timeout : NetworkError()
    data object NoInternet : NetworkError()
    data object Serialization : NetworkError()
    data class Unknown(val detail: String? = null) : NetworkError()
}

/**
 * Convierte el error en texto para mostrar (snackbar).
 *  - Errores con "mensaje" del backend → se respeta ese texto ([UiText.Dynamic]).
 *  - El resto → recurso traducible ([UiText.Resource]).
 */
fun NetworkError.asUiText(): UiText = when (this) {
    NetworkError.EmptyFields      -> UiText.Resource(R.string.error_empty_fields)
    NetworkError.Unauthorized     -> UiText.Resource(R.string.error_unauthorized)
    NetworkError.NotFound         -> UiText.Resource(R.string.error_not_found)
    NetworkError.TooManyRequests  -> UiText.Resource(R.string.error_too_many_requests)
    NetworkError.Timeout          -> UiText.Resource(R.string.error_timeout)
    NetworkError.NoInternet       -> UiText.Resource(R.string.error_no_internet)
    NetworkError.Serialization    -> UiText.Resource(R.string.error_serialization)
    is NetworkError.Unknown       -> UiText.Resource(R.string.error_unknown)

    // Estos priorizan el "mensaje" del backend; si falta, fallback traducible.
    is NetworkError.Forbidden     -> serverMessage.orResource(R.string.error_forbidden)
    is NetworkError.Validation    -> serverMessage.orResource(R.string.error_validation)
    is NetworkError.Conflict      -> serverMessage.orResource(R.string.error_conflict)
    is NetworkError.BusinessRule  -> serverMessage.orResource(R.string.error_business_rule)
    is NetworkError.ServerError   -> serverMessage.orResource(R.string.error_server)
}

private fun String?.orResource(@StringRes resId: Int): UiText =
    this?.takeIf { it.isNotBlank() }?.let { UiText.Dynamic(it) } ?: UiText.Resource(resId)
