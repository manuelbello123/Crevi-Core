package com.example.tienda.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tienda.core.ui.theme.InverseAccent
import com.example.tienda.core.ui.theme.InverseOnSurface
import com.example.tienda.core.ui.theme.InverseSurface
import com.example.tienda.core.util.UiText

/**
 * Host de snackbar con el estilo de la app. Úsalo en el `snackbarHost` del
 * Scaffold de cada pantalla. Para mostrar un error normalizado, combínalo con
 * [ShowUiMessage].
 */
@Composable
fun TiendaSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState, modifier) { data ->
        Snackbar(
            modifier = Modifier.padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = InverseSurface,
            contentColor = InverseOnSurface,
            actionContentColor = InverseAccent,
        ) {
            Text(data.visuals.message)
        }
    }
}

/**
 * Observa un [UiText] (típicamente `state.errorMessage`) y lo muestra en el
 * snackbar; al terminar invoca [onShown] para que el ViewModel lo limpie.
 */
@Composable
fun ShowUiMessage(
    message: UiText?,
    hostState: SnackbarHostState,
    onShown: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(message) {
        message?.let {
            hostState.showSnackbar(it.asString(context))
            onShown()
        }
    }
}
