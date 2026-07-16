package com.example.tienda.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/*
 * Tema ÚNICO y CLARO: el mismo esquema sin importar el modo del sistema
 * (sin dynamic color).
 */
private val TiendaColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimarySoft,
    onPrimaryContainer = PrimaryDark,

    secondary = Accent,
    onSecondary = OnAccent,
    tertiary = Primary,
    onTertiary = OnPrimary,

    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,

    outline = Border,
    outlineVariant = BorderSoft,

    error = Danger,
    onError = OnDanger,

    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
)

@Composable
fun TiendaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TiendaColorScheme,
        typography = Typography,
        content = content,
    )
}
