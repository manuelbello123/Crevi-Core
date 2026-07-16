package com.example.tienda.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Encabezado de día para listas agrupadas por fecha (p. ej. ventas de la semana,
 * movimientos del historial). Muestra el día de la semana (Lunes, Martes…) y la
 * fecha a la derecha: corta ("22 jun") o completa ("22 de Junio de 2026") según
 * [fechaCompleta].
 */
@Composable
fun DayHeader(fecha: LocalDate, modifier: Modifier = Modifier, fechaCompleta: Boolean = false) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = nombreDia(fecha),
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = if (fechaCompleta) fechaLarga(fecha) else fecha.format(DIA_MES),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

private val LOCALE_ES: Locale = Locale.forLanguageTag("es")
private val DIA_MES = DateTimeFormatter.ofPattern("d MMM", LOCALE_ES)

private fun nombreDia(fecha: LocalDate): String =
    fecha.dayOfWeek.getDisplayName(TextStyle.FULL, LOCALE_ES)
        .replaceFirstChar { it.uppercase() }

/** "22 de Junio de 2026" (mes con mayúscula inicial). */
private fun fechaLarga(fecha: LocalDate): String {
    val mes = fecha.month.getDisplayName(TextStyle.FULL, LOCALE_ES).replaceFirstChar { it.uppercase() }
    return "${fecha.dayOfMonth} de $mes de ${fecha.year}"
}
