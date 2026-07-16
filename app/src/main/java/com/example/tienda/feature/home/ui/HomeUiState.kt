package com.example.tienda.feature.home.ui

import com.example.tienda.core.enums.UserRole
import com.example.tienda.core.util.UiText
import com.example.tienda.feature.home.domain.ActividadItem
import com.example.tienda.feature.sucursales.domain.Sucursal
import java.math.BigDecimal

/**
 * Secciones de la app. Las 3 primeras son los tabs del bottom bar; el resto
 * se navegan desde el drawer. (El historial se integró al resumen del Home.)
 */
enum class HomeSection {
    // Bottom bar
    HOME, COBRANZAS, VENTAS,
    // Solo drawer
    CORTE, CLIENTES, USUARIOS, SUCURSALES, CONFIGURACIONES,
}

/**
 * Estado del shell (Home): identidad del operador para el drawer + switch de
 * sucursal (compartido por Cobranza y, a futuro, Ventas). La lista de cobranza
 * la maneja CobranzasViewModel.
 */
data class HomeUiState(
    val nombre: String = "",
    val rol: UserRole? = null,
    val esAdministrador: Boolean = false,

    val sucursales: List<Sucursal> = emptyList(),
    val selectedSucursalId: Long? = null,

    // ── Resumen (de la sucursal seleccionada; totales de la semana ISO) ──
    val resumenLoading: Boolean = false,
    val resumenRefreshing: Boolean = false,
    val resumenError: UiText? = null,
    val totalRecaudado: BigDecimal = BigDecimal.ZERO,
    val totalContado: BigDecimal = BigDecimal.ZERO,
    val totalAbonos: BigDecimal = BigDecimal.ZERO,
    val totalAnticipos: BigDecimal = BigDecimal.ZERO,
    val pendientesCount: Int = 0,
    val actividad: List<ActividadItem> = emptyList(),
) {
    val selectedSucursalNombre: String?
        get() = sucursales.firstOrNull { it.id == selectedSucursalId }?.nombre

    /** Sin sucursal concreta (admin en "todas"): el resumen no aplica. */
    val sinSucursal: Boolean get() = selectedSucursalId == null
}
