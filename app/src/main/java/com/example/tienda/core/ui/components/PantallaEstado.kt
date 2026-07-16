package com.example.tienda.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.Danger
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary
import com.example.tienda.core.ui.theme.TiendaShapes

/**
 * Estado vacío/error genérico, centrado. Una sola fuente de verdad para
 * "No se pudo cargar…", "No hay X", "Sin resultados": mismo tamaño, misma
 * tipografía, mismo botón outlined con radio 12 en todas las pantallas.
 *
 * Sin acción → solo mensaje centrado (mismo layout que las demás pantallas).
 */
@Composable
fun PantallaEstadoVacio(
    mensaje: String,
    textoAccion: String? = null,
    onAccion: (() -> Unit)? = null,
    icon: ImageVector? = null,
    iconTint: Color = Danger,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        if (textoAccion != null && onAccion != null) {
            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = onAccion,
                shape = TiendaShapes.Button,
                border = BorderStroke(1.dp, Border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                }
                Text(
                    text = textoAccion,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/**
 * Estado de ERROR estándar: icono de refrescar en rojo + botón outlined "Reintentar".
 * Úsalo cuando la carga falló y la lista quedó vacía.
 */
@Composable
fun PantallaError(
    mensaje: String,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PantallaEstadoVacio(
        mensaje = mensaje,
        textoAccion = "Reintentar",
        onAccion = onReintentar,
        icon = Icons.Filled.Refresh,
        iconTint = Danger,
        modifier = modifier,
    )
}
