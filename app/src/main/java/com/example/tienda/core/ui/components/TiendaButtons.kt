package com.example.tienda.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import com.example.tienda.core.ui.theme.SurfaceMuted
import com.example.tienda.core.ui.theme.TextDisabled
import com.example.tienda.core.ui.theme.TiendaShapes
import com.example.tienda.core.ui.theme.TiendaSpacing

/**
 * Botón primario estándar: fondo primary, texto blanco, altura 52dp, radio 12dp.
 * Estado de carga con spinner. Deshabilitado se ve tenue.
 */
@Composable
fun BotonPrimario(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TiendaSpacing.ButtonHeight)
            .clip(TiendaShapes.Button)
            .background(if (enabled) Primary else SurfaceMuted)
            .clickable(enabled = enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(22.dp), color = OnPrimary, strokeWidth = 2.dp)
        } else {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) OnPrimary else TextDisabled,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Botón secundario estándar: contorno primary, texto primary, sin fondo.
 * Altura 52dp, radio 12dp.
 */
@Composable
fun BotonSecundario(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TiendaSpacing.ButtonHeight)
            .clip(TiendaShapes.Button)
            .border(1.dp, Primary, TiendaShapes.Button)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = Primary, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * FAB extendido estándar de "Nuevo/Nueva X". Reemplaza los FABs sin texto
 * que había en clientes/usuarios/ventas. Radio 12dp, fondo primary.
 */
@Composable
fun TiendaFab(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Add,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = Primary,
        contentColor = OnPrimary,
        shape = TiendaShapes.Card,
        icon = { Icon(icon, contentDescription = null) },
        text = { Text(text, fontWeight = FontWeight.SemiBold) },
        modifier = modifier,
    )
}

