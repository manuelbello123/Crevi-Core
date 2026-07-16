package com.example.tienda.core.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Texto destinado a la UI que puede provenir de dos fuentes:
 *  - [Resource]: un recurso traducible (@StringRes) → errores genéricos/transporte.
 *  - [Dynamic] : un String ya resuelto → el "mensaje" exacto que manda el backend.
 *
 * Se resuelve con [asString] tanto fuera de Compose (con Context) como dentro.
 */
sealed interface UiText {

    data class Dynamic(val value: String) : UiText

    data class Resource(
        @param:StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    fun asString(context: Context): String = when (this) {
        is Dynamic  -> value
        is Resource -> context.getString(resId, *args.toTypedArray())
    }

    @Composable
    fun asString(): String = when (this) {
        is Dynamic  -> value
        is Resource -> stringResource(resId, *args.toTypedArray())
    }
}
