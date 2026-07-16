package com.example.tienda.core.network

import kotlinx.serialization.Serializable

/**
 * Envoltura de error uniforme del backend:
 *   { "error": { "codigo": "REGLA_DE_NEGOCIO", "mensaje": "texto legible" } }
 *
 * El "codigo" mapea a un HTTP (ver NetworkError); el "mensaje" es el texto
 * legible que mostramos al usuario en el snackbar.
 */
@Serializable
data class ApiErrorEnvelope(
    val error: ApiErrorBody? = null
)

@Serializable
data class ApiErrorBody(
    val codigo: String? = null,
    val mensaje: String? = null
)
