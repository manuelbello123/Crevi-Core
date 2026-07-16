package com.example.tienda.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Tokens de forma unificados. Una sola fuente de verdad para radios:
 * cards, buscadores, botones, sheets y diálogos. Los chips y avatares
 * (píldora/circulares) NO usan estos radios — son intencionalmente redondos.
 */
object TiendaShapes {
    /** Radio universal (12dp): cards, buscadores, botones primarios, sheets, diálogos. */
    val Card = RoundedCornerShape(12.dp)
    val Field = RoundedCornerShape(12.dp)
    val Button = RoundedCornerShape(12.dp)
    val Dialog = RoundedCornerShape(12.dp)

    /** Pill: chips, badges, tags. */
    val Pill = RoundedCornerShape(999.dp)
}

/** Espaciados clave (para no reinventar en cada card/lista). */
object TiendaSpacing {
    /** Padding interno estándar de una card. */
    val CardPadding = 16.dp

    /** Separación vertical entre cards de una lista. */
    val CardGap = 12.dp

    /** Padding horizontal de contenido de pantalla. */
    val ScreenPadding = 16.dp

    /** Altura de botones primarios y buscadores. */
    val ButtonHeight = 52.dp
}
