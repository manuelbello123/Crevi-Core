package com.example.tienda.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.Surface
import com.example.tienda.core.ui.theme.TiendaShapes

/**
 * Estilo de card estándar: superficie blanca + borde sutil + radio 12dp.
 * ÚNICA fuente de verdad para todas las cards de la app.
 */
fun Modifier.tarjeta(): Modifier = this
    .clip(TiendaShapes.Card)
    .background(Surface)
    .border(1.dp, Border, TiendaShapes.Card)
