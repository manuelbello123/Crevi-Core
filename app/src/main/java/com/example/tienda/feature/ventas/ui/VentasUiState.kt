package com.example.tienda.feature.ventas.ui
import com.example.tienda.feature.ventas.domain.*

import com.example.tienda.feature.clientes.domain.Cliente
import com.example.tienda.feature.cobranzas.domain.Cuenta
import com.example.tienda.core.util.UiText
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Sub-navegación del módulo Ventas. */
enum class VentasMode { LISTA, NUEVA, EDITAR }

/** Filtro por tipo de venta sobre la lista cargada (mostrador = sin cuenta). */
enum class FiltroTipo { TODOS, MOSTRADOR, CONTADO, CREDITO, MIXTO }

/** Lunes de la semana ISO que contiene [fecha]. */
fun inicioSemana(fecha: LocalDate): LocalDate = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

/** ¿La venta [v] coincide con este filtro de tipo? */
fun FiltroTipo.coincide(v: VentaResumen): Boolean = when (this) {
    FiltroTipo.TODOS -> true
    FiltroTipo.MOSTRADOR -> v.cuentaId == null
    FiltroTipo.CONTADO -> v.cuentaId != null && v.tipo == "contado"
    FiltroTipo.CREDITO -> v.cuentaId != null && v.tipo == "credito"
    FiltroTipo.MIXTO -> v.cuentaId != null && v.tipo == "mixto"
}

data class VentasUiState(
    val esAdministrador: Boolean = false,
    val selectedSucursalId: Long? = null,

    // ── Lista de ventas (de la semana ISO seleccionada, filtrable por estado) ──
    val ventas: List<VentaResumen> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
    val filtroEstado: EstadoVenta? = null, // null = todas

    // Semana mostrada (lunes), búsqueda y filtro por tipo (estos dos filtran en memoria)
    val semanaInicio: LocalDate = inicioSemana(LocalDate.now()),
    val query: String = "",
    val filtroTipo: FiltroTipo = FiltroTipo.TODOS,

    // ── Worklist de pendientes (con fecha límite) ──
    val pendientes: List<VentaPendiente> = emptyList(),

    // ── Detalle de una venta (al tocarla en la lista; trae renglones) ──
    val ventaDetalle: Venta? = null,
    val detalleLoading: Boolean = false,

    // ── Formulario nueva venta / edición de renglones ──
    val mode: VentasMode = VentasMode.LISTA,
    val editandoVentaId: Long? = null, // venta cuyos renglones se editan (modo EDITAR)
    val editandoVentaEstado: EstadoVenta? = null, // estado de la venta editada (para reglas condicionales)
    val esMostrador: Boolean = false,
    val nombreMostrador: String = "",

    // Selección de cliente
    val clienteQuery: String = "",
    val clientesResultado: List<Cliente> = emptyList(),
    val buscandoClientes: Boolean = false,
    val clienteSeleccionado: Cliente? = null,

    // Cuentas del cliente (para el crédito)
    val cuentasCliente: List<Cuenta> = emptyList(),
    val cuentasLoading: Boolean = false,
    val cuentaSeleccionada: Cuenta? = null,
    val cuentaCreada: Boolean = false, // ya se creó una cuenta nueva en esta venta (solo una vez)

    val pendiente: Boolean = false,
    val anticipo: String = "", // adelanto (solo crédito no pendiente, con cuenta)
    val renglones: List<RenglonVentaInput> = emptyList(),

    val saving: Boolean = false,
    val formError: UiText? = null,

    val mensaje: UiText? = null,
) {
    val totalContado: BigDecimal
        get() = renglones.filter { it.tipo == TipoRenglon.CONTADO }.fold(BigDecimal.ZERO) { acc, r -> acc + r.importe }

    val totalCredito: BigDecimal
        get() = renglones.filter { it.tipo == TipoRenglon.CREDITO }.fold(BigDecimal.ZERO) { acc, r -> acc + r.importe }

    val total: BigDecimal get() = totalContado + totalCredito

    val hayCredito: Boolean get() = renglones.any { it.tipo == TipoRenglon.CREDITO }

    // ── Semana / filtros de la lista ──

    val semanaFin: LocalDate get() = semanaInicio.plusDays(6)

    /** No se puede navegar a una semana futura. */
    val esSemanaActual: Boolean get() = semanaInicio == inicioSemana(LocalDate.now())

    /** Ventas de la semana ya filtradas por tipo y texto (cliente/mostrador). */
    val ventasVisibles: List<VentaResumen>
        get() = ventas.filter { filtroTipo.coincide(it) }.filter { coincideTexto(it, query) }

    private fun coincideTexto(v: VentaResumen, q: String): Boolean {
        if (q.isBlank()) return true
        val needle = q.trim().lowercase()
        return (v.nombreCliente?.lowercase()?.contains(needle) == true) ||
            (v.nombreMostrador?.lowercase()?.contains(needle) == true)
    }
}
