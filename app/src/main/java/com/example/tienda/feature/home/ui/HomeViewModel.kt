package com.example.tienda.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tienda.R
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.util.UiText
import com.example.tienda.core.session.SessionManager
import com.example.tienda.feature.cobranzas.data.CobranzaRepository
import com.example.tienda.feature.cobranzas.domain.MovimientoReciente
import com.example.tienda.feature.cobranzas.domain.TipoMovimiento
import com.example.tienda.feature.corte.data.CorteRepository
import com.example.tienda.feature.home.domain.ActividadItem
import com.example.tienda.feature.home.domain.ActividadTipo
import com.example.tienda.feature.sucursales.data.SucursalRepository
import com.example.tienda.feature.ventas.data.VentaRepository
import com.example.tienda.feature.ventas.domain.EstadoVenta
import com.example.tienda.feature.ventas.domain.VentaResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Shell (Home) + resumen del día/semana de la sucursal seleccionada: totales
 * (corte semanal) + feed de actividad reciente (ventas + abonos/anticipos). El
 * switch de sucursal fija el alcance; el resumen es SIEMPRE de una sola sucursal.
 */
class HomeViewModel(
    private val sucursalRepository: SucursalRepository,
    private val corteRepository: CorteRepository,
    private val ventaRepository: VentaRepository,
    private val cobranzaRepository: CobranzaRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val session = sessionManager.getSession()

    private val _state = MutableStateFlow(
        HomeUiState(
            nombre = session?.nombre.orEmpty(),
            rol = session?.rol,
            esAdministrador = session?.esAdministrador == true,
            selectedSucursalId = session?.sucursalId,
        )
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        if (_state.value.esAdministrador) loadSucursales()
        _state.value.selectedSucursalId?.let { cargarResumen(it) }
    }

    fun onSucursalSelected(sucursalId: Long) {
        if (sucursalId == _state.value.selectedSucursalId) return
        _state.update { it.copy(selectedSucursalId = sucursalId) }
        cargarResumen(sucursalId)
    }

    fun refrescarResumen() {
        _state.value.selectedSucursalId?.let { cargarResumen(it, refreshing = true) }
    }

    private fun loadSucursales() {
        viewModelScope.launch {
            (sucursalRepository.listar() as? NetworkResult.Success)?.let { res ->
                _state.update { it.copy(sucursales = res.data) }
            }
        }
    }

    private fun cargarResumen(sucursalId: Long, refreshing: Boolean = false) {
        _state.update { it.copy(resumenLoading = !refreshing, resumenRefreshing = refreshing, resumenError = null) }
        viewModelScope.launch {
            val hoy = LocalDate.now()
            val lunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val domingo = lunes.plusDays(6)

            val corteRes = corteRepository.porSucursal(null, null, sucursalId)
            val ventasRes = ventaRepository.listar(sucursalId, lunes.toString(), domingo.toString(), null, 1, 50)
            val movsRes = cobranzaRepository.movimientosRecientes(sucursalId, 30)

            // Solo es error "duro" si falla lo esencial (corte y ventas).
            if (corteRes is NetworkResult.Error && ventasRes is NetworkResult.Error) {
                _state.update {
                    it.copy(resumenLoading = false, resumenRefreshing = false, resumenError = UiText.Resource(R.string.error_resumen))
                }
                return@launch
            }

            val corte = (corteRes as? NetworkResult.Success)?.data?.firstOrNull()
            val ventas = (ventasRes as? NetworkResult.Success)?.data.orEmpty()
            val movs = (movsRes as? NetworkResult.Success)?.data.orEmpty()

            _state.update {
                it.copy(
                    resumenLoading = false,
                    resumenRefreshing = false,
                    totalRecaudado = corte?.totalIngresos ?: BigDecimal.ZERO,
                    totalContado = corte?.totalContado ?: BigDecimal.ZERO,
                    totalAbonos = corte?.totalAbonos ?: BigDecimal.ZERO,
                    totalAnticipos = corte?.totalAnticipos ?: BigDecimal.ZERO,
                    pendientesCount = ventas.count { v -> v.estado == EstadoVenta.PENDIENTE },
                    actividad = construirActividad(ventas, movs, hoy),
                )
            }
        }
    }

    private fun construirActividad(ventas: List<VentaResumen>, movs: List<MovimientoReciente>, hoy: LocalDate): List<ActividadItem> {
        // Solo la semana ISO actual (lunes–domingo). Las ventas ya vienen filtradas
        // por rango en el backend; los movimientos NO, así que aquí los recortamos.
        val lunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val domingo = lunes.plusDays(6)
        val items = mutableListOf<ActividadItem>()

        ventas.forEach { v ->
            items += ActividadItem(
                tipo = when (v.estado) {
                    EstadoVenta.PENDIENTE -> ActividadTipo.PENDIENTE
                    EstadoVenta.CANCELADA -> ActividadTipo.CANCELADA
                    else -> ActividadTipo.VENTA
                },
                titulo = v.nombreCliente ?: v.nombreMostrador ?: "Venta",
                subtitulo = subtituloVenta(v),
                monto = v.total,
                cuando = cuandoTexto(v.fechaCompra, v.horaCompra, hoy),
                orden = "${v.fechaCompra}T${v.horaCompra}",
            )
        }

        movs.filter { it.tipo == TipoMovimiento.ABONO || it.tipo == TipoMovimiento.ANTICIPO || it.tipo == TipoMovimiento.DEVOLUCION }
            .filter { enSemana(it.creadoEn.take(10), lunes, domingo) }
            .forEach { m ->
                val fecha = m.creadoEn.take(10)
                val hora = m.creadoEn.drop(11).take(5)
                items += ActividadItem(
                    tipo = when (m.tipo) {
                        TipoMovimiento.ANTICIPO -> ActividadTipo.ANTICIPO
                        TipoMovimiento.DEVOLUCION -> ActividadTipo.DEVOLUCION
                        else -> ActividadTipo.ABONO
                    },
                    titulo = m.nombreCliente ?: conceptoNombre(m.tipo),
                    subtitulo = conceptoNombre(m.tipo),
                    monto = m.monto,
                    cuando = cuandoTexto(fecha, hora, hoy),
                    orden = m.creadoEn.replace(' ', 'T'),
                )
            }

        return items.sortedByDescending { it.orden }.take(15)
    }

    private fun enSemana(fechaIso: String, lunes: LocalDate, domingo: LocalDate): Boolean {
        val f = runCatching { LocalDate.parse(fechaIso) }.getOrNull() ?: return false
        return !f.isBefore(lunes) && !f.isAfter(domingo)
    }

    private fun subtituloVenta(v: VentaResumen): String = when {
        v.cuentaId == null -> "Venta de mostrador"
        v.tipo == "mixto" -> "Venta mixta"
        v.tipo == "credito" -> "Venta a crédito"
        else -> "Venta de contado"
    }

    private fun conceptoNombre(t: TipoMovimiento): String = when (t) {
        TipoMovimiento.ABONO -> "Abono"
        TipoMovimiento.ANTICIPO -> "Anticipo"
        TipoMovimiento.DEVOLUCION -> "Devolución"
        else -> "Movimiento"
    }

    private fun cuandoTexto(fecha: String, hora: String, hoy: LocalDate): String =
        if (fecha == hoy.toString()) hora.take(5)
        else runCatching { LocalDate.parse(fecha).format(DIA_MES) }.getOrDefault(fecha)

    private companion object {
        val DIA_MES: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es"))
    }
}
