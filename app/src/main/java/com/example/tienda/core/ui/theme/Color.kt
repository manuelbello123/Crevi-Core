package com.example.tienda.core.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Paleta GLOBAL "Reliant Professional" (Stitch): navy de confianza, teal para
 * positivo/cobrado, rojo para deuda, sobre superficies claras. Tema único claro.
 */

// ── Superficies ──
val Background   = Color(0xFFF7F9FC) // canvas
val Surface      = Color(0xFFFFFFFF) // cards / barras
val SurfaceMuted = Color(0xFFECEEF1) // fills sutiles
val PrimarySoft  = Color(0xFFE7EBF3) // selección / avatares (navy muy claro)
val Border       = Color(0xFFC5C6CE) // bordes de input
val BorderSoft   = Color(0xFFE5E7EB) // divisores

// ── Marca / acciones ──
val Primary     = Color(0xFF1A2B48) // navy
val PrimaryDark = Color(0xFF031632)
val OnPrimary   = Color(0xFFFFFFFF)
val Accent      = Color(0xFF006A60) // teal (positivo / cobrado)
val OnAccent    = Color(0xFFFFFFFF)

// ── Texto ──
val TextPrimary   = Color(0xFF191C1E)
val TextSecondary = Color(0xFF44474D)
val TextDisabled  = Color(0xFF9AA0A6)

// ── Semánticos ──
val Danger   = Color(0xFFBA1A1A) // deuda / eliminar
val OnDanger = Color(0xFFFFFFFF)
val Success  = Color(0xFF006A60) // teal (positivo)
val Warning  = Color(0xFFD97706) // semáforo ámbar

// ── Snackbar inverso (oscuro sobre tema claro) ──
val InverseSurface   = Color(0xFF2D3133)
val InverseOnSurface = Color(0xFFEFF1F4)
val InverseAccent    = Color(0xFF67D9C9)
