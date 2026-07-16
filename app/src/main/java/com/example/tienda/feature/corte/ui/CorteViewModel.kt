package com.example.tienda.feature.corte.ui
import com.example.tienda.feature.corte.data.CorteRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.error.asUiText
import com.example.tienda.core.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.WeekFields

/**
 * Corte semanal (admin: todas las sucursales; gerente: la suya, lo fuerza el backend).
 * Muestra los totales de la semana ISO + desglose por sucursal o por usuario, y
 * permite exportar el Excel. El desglose por usuario se carga bajo demanda.
 */
class CorteViewModel(
    private val corteRepository: CorteRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CorteUiState(esAdministrador = sessionManager.getSession()?.esAdministrador == true)
    )
    val state: StateFlow<CorteUiState> = _state.asStateFlow()

    init {
        val (anio, semana) = isoActual()
        _state.update { it.copy(anio = anio, semana = semana, esSemanaActual = true) }
        cargar()
    }

    // ── Vista (por sucursal / por usuario) ──

    fun setVista(vista: CorteVista) {
        if (vista == _state.value.vista) return
        _state.update { it.copy(vista = vista) }
        if (vista == CorteVista.USUARIO && _state.value.porUsuario.isEmpty()) cargarUsuarios()
    }

    // ── Navegación de semana ──

    fun seleccionarSemana(anio: Int, semana: Int) {
        val (aAct, sAct) = isoActual()
        _state.update {
            it.copy(
                anio = anio,
                semana = semana,
                esSemanaActual = anio == aAct && semana == sAct,
                porSucursal = emptyList(),
                porUsuario = emptyList(),
            )
        }
        cargar()
    }

    fun semanaAnterior() {
        val lunes = lunesDe(_state.value.anio, _state.value.semana).minusWeeks(1)
        seleccionarSemana(lunes.get(WeekFields.ISO.weekBasedYear()), lunes.get(WeekFields.ISO.weekOfWeekBasedYear()))
    }

    fun semanaSiguiente() {
        if (_state.value.esSemanaActual) return // no se navega al futuro
        val lunes = lunesDe(_state.value.anio, _state.value.semana).plusWeeks(1)
        seleccionarSemana(lunes.get(WeekFields.ISO.weekBasedYear()), lunes.get(WeekFields.ISO.weekOfWeekBasedYear()))
    }

    fun retry() = cargar()

    fun refresh() = cargar(refreshing = true)

    // ── Exportar Excel ──

    fun exportarExcel() {
        if (_state.value.exportando) return
        _state.update { it.copy(exportando = true, error = null) }
        viewModelScope.launch {
            val s = _state.value
            when (val r = corteRepository.excel(s.anio, s.semana, null)) {
                is NetworkResult.Success ->
                    _state.update { it.copy(exportando = false, excel = ExcelDescarga("corte_${s.anio}_s${s.semana}.xlsx", r.data)) }
                is NetworkResult.Error ->
                    _state.update { it.copy(exportando = false, error = r.error.asUiText()) }
            }
        }
    }

    /** La pantalla llama esto cuando ya guardó/compartió el archivo. */
    fun excelDescargado() = _state.update { it.copy(excel = null) }

    // ── Limpieza ──

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearMensaje() = _state.update { it.copy(mensaje = null) }

    // ── Privados ──

    private fun cargar(refreshing: Boolean = false) {
        _state.update { it.copy(isLoading = !refreshing, isRefreshing = refreshing, error = null) }
        viewModelScope.launch {
            val s = _state.value
            when (val r = corteRepository.porSucursal(s.anio, s.semana, null)) {
                is NetworkResult.Success -> _state.update { it.copy(isLoading = false, isRefreshing = false, porSucursal = r.data) }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isLoading = false, isRefreshing = false, error = r.error.asUiText()) }
                    return@launch
                }
            }
            // El desglose por usuario solo se carga cuando esa vista está activa.
            if (_state.value.vista == CorteVista.USUARIO) cargarUsuarios()
        }
    }

    private fun cargarUsuarios() {
        viewModelScope.launch {
            val s = _state.value
            when (val r = corteRepository.porUsuario(s.anio, s.semana, null)) {
                is NetworkResult.Success -> _state.update { it.copy(porUsuario = r.data) }
                is NetworkResult.Error -> _state.update { it.copy(error = r.error.asUiText()) }
            }
        }
    }

    private fun isoActual(): Pair<Int, Int> {
        val hoy = LocalDate.now()
        return hoy.get(WeekFields.ISO.weekBasedYear()) to hoy.get(WeekFields.ISO.weekOfWeekBasedYear())
    }

    /** Lunes de la semana ISO indicada. */
    private fun lunesDe(anio: Int, semana: Int): LocalDate =
        LocalDate.now()
            .with(WeekFields.ISO.weekBasedYear(), anio.toLong())
            .with(WeekFields.ISO.weekOfWeekBasedYear(), semana.toLong())
            .with(WeekFields.ISO.dayOfWeek(), 1)
}
