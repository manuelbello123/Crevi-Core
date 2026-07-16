package com.example.tienda.feature.cobranzas.ui
import com.example.tienda.feature.cobranzas.data.CobranzaRepository
import com.example.tienda.feature.cobranzas.domain.*
import com.example.tienda.core.util.aMoneda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tienda.R
import com.example.tienda.feature.clientes.domain.Cliente
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.error.asUiText
import com.example.tienda.feature.clientes.data.ClienteRepository
import com.example.tienda.feature.ventas.data.VentaRepository
import com.example.tienda.core.util.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Cobranza: lista de clientes ACTIVOS de la sucursal seleccionada (la fija el
 * switch del Home con [setSucursal]) con su saldo global + semáforo. Más
 * adelante: cuentas + saldos → historial → registrar abono/devolución.
 */
class CobranzasViewModel(
    private val clienteRepository: ClienteRepository,
    private val cobranzaRepository: CobranzaRepository,
    private val ventaRepository: VentaRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CobranzasUiState())
    val state: StateFlow<CobranzasUiState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var sucursalCargada = false

    /** Llamado desde el Home cuando cambia el switch de sucursal. */
    fun setSucursal(sucursalId: Long?) {
        if (sucursalCargada && sucursalId == _state.value.selectedSucursalId) return
        sucursalCargada = true
        _state.update {
            it.copy(
                selectedSucursalId = sucursalId,
                clienteSeleccionado = null, cuentas = emptyList(),
                cuentaSeleccionada = null, historial = null, ventasCuenta = emptyList(),
            )
        }
        loadClientes()
    }

    // ── Lista / búsqueda ──

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadClientes()
        }
    }

    fun limpiarBusqueda() = onQueryChange("")

    fun retry() = loadClientes()

    fun refresh() = loadClientes(refreshing = true)

    // ── Selección cliente → cuentas (para la pantalla de detalle, a futuro) ──

    fun seleccionarCliente(cliente: Cliente) {
        _state.update { it.copy(clienteSeleccionado = cliente, cuentaSeleccionada = null, historial = null, ventasCuenta = emptyList()) }
        loadCuentas(cliente.id)
    }

    fun volverAClientes() =
        _state.update {
            it.copy(clienteSeleccionado = null, cuentas = emptyList(), cuentasError = null, cuentaSeleccionada = null, historial = null, ventasCuenta = emptyList())
        }

    fun seleccionarCuenta(cuenta: Cuenta) {
        _state.update { it.copy(cuentaSeleccionada = cuenta) }
        loadHistorial(cuenta.id)
        loadVentas(cuenta.id)
    }

    fun volverACuentas() =
        _state.update { it.copy(cuentaSeleccionada = null, historial = null, historialError = null, ventasCuenta = emptyList()) }

    // ── Registrar movimiento (formulario inline sobre la cuenta seleccionada) ──

    fun registrarAbono(montoTexto: String, nota: String?) {
        val cuenta = _state.value.cuentaSeleccionada ?: return
        val monto = validarMonto(montoTexto, cuenta) ?: return
        _state.update { it.copy(registrando = true, formError = null) }
        viewModelScope.launch {
            onMovimientoResult(cobranzaRepository.registrarAbono(cuenta.id, monto, nota), R.string.abono_registrado)
        }
    }

    fun registrarDevolucion(montoTexto: String, ventaId: Long?, nota: String?) {
        val cuenta = _state.value.cuentaSeleccionada ?: return
        val monto = validarMonto(montoTexto, cuenta) ?: return
        _state.update { it.copy(registrando = true, formError = null) }
        viewModelScope.launch {
            onMovimientoResult(cobranzaRepository.registrarDevolucion(cuenta.id, monto, ventaId, nota), R.string.devolucion_registrada)
        }
    }

    // ── Renombrar cuenta ──

    fun abrirRenombrarCuenta(cuenta: Cuenta) = _state.update {
        it.copy(renombrarCuentaAbierto = cuenta, renombrarNombre = cuenta.nombre.orEmpty(), renombrarError = null)
    }

    fun cerrarRenombrarCuenta() = _state.update {
        it.copy(renombrarCuentaAbierto = null, renombrarNombre = "", renombrando = false, renombrarError = null)
    }

    fun onRenombrarNombreChange(v: String) = _state.update { it.copy(renombrarNombre = v, renombrarError = null) }

    fun guardarNombreCuenta() {
        val cuenta = _state.value.renombrarCuentaAbierto ?: return
        val nuevoNombre = _state.value.renombrarNombre
        if (nuevoNombre.trim() == cuenta.nombre.orEmpty().trim()) {
            _state.update { it.copy(renombrarCuentaAbierto = null, renombrarNombre = "") }
            return
        }
        _state.update { it.copy(renombrando = true, renombrarError = null) }
        viewModelScope.launch {
            when (val r = cobranzaRepository.editarCuenta(cuenta.id, nuevoNombre)) {
                is NetworkResult.Success -> {
                    val actualizada = r.data
                    // Actualizo la cuenta en la lista y la seleccionada, sin recargar todo.
                    _state.update { s ->
                        s.copy(
                            renombrando = false,
                            renombrarCuentaAbierto = null,
                            renombrarNombre = "",
                            cuentas = s.cuentas.map { if (it.id == actualizada.id) actualizada else it },
                            cuentaSeleccionada = if (s.cuentaSeleccionada?.id == actualizada.id) actualizada else s.cuentaSeleccionada,
                            mensaje = UiText.Resource(R.string.cuenta_renombrada),
                        )
                    }
                }
                is NetworkResult.Error -> _state.update { it.copy(renombrando = false, renombrarError = r.error.asUiText()) }
            }
        }
    }

    // ── Editar / eliminar movimientos manuales (abono/anticipo/devolución) ──

    fun editarMovimiento(movimientoId: Long, montoTexto: String, nota: String?) {
        val monto = montoTexto.trim().toBigDecimalOrNull()
        if (monto == null || monto <= BigDecimal.ZERO) {
            _state.update { it.copy(error = UiText.Resource(R.string.error_monto_invalido)) }
            return
        }
        viewModelScope.launch {
            when (val r = cobranzaRepository.editarMovimiento(movimientoId, monto, nota)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(mensaje = UiText.Resource(R.string.movimiento_actualizado)) }
                    recargarTrasMovimiento()
                }
                is NetworkResult.Error -> _state.update { it.copy(error = r.error.asUiText()) }
            }
        }
    }

    fun eliminarMovimiento(movimientoId: Long) {
        viewModelScope.launch {
            when (val r = cobranzaRepository.eliminarMovimiento(movimientoId)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(mensaje = UiText.Resource(R.string.movimiento_eliminado)) }
                    recargarTrasMovimiento()
                }
                is NetworkResult.Error -> _state.update { it.copy(error = r.error.asUiText()) }
            }
        }
    }

    fun clearFormError() = _state.update { it.copy(formError = null) }

    // ── Limpieza ──

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearMensaje() = _state.update { it.copy(mensaje = null) }

    // ── Privados ──

    private fun validarMonto(montoTexto: String, cuenta: Cuenta): BigDecimal? {
        val monto = montoTexto.trim().toBigDecimalOrNull()
        if (monto == null || monto <= BigDecimal.ZERO) {
            _state.update { it.copy(formError = UiText.Resource(R.string.error_monto_invalido)) }
            return null
        }
        if (monto > cuenta.saldo) {
            _state.update { it.copy(formError = UiText.Resource(R.string.error_monto_supera_saldo)) }
            return null
        }
        return monto
    }

    private fun onMovimientoResult(result: NetworkResult<Movimiento>, successRes: Int) {
        when (result) {
            is NetworkResult.Success -> {
                _state.update { it.copy(registrando = false, mensaje = UiText.Resource(successRes)) }
                recargarTrasMovimiento()
            }
            is NetworkResult.Error -> _state.update { it.copy(registrando = false, formError = result.error.asUiText()) }
        }
    }

    /** Tras crear/editar/eliminar un movimiento: refresca cuentas (saldos) y la lista de clientes. */
    private fun recargarTrasMovimiento() {
        _state.value.clienteSeleccionado?.let { loadCuentas(it.id) }
        loadClientes()
    }

    private fun loadClientes(refreshing: Boolean = false) {
        val q = _state.value.query
        val sucursalId = _state.value.selectedSucursalId
        _state.update { it.copy(isLoading = !refreshing, isRefreshing = refreshing, error = null) }
        viewModelScope.launch {
            val clientesRes = clienteRepository.listar(q = q.ifBlank { null }, pagina = 1, tamano = 100, sucursalId = sucursalId)
            if (clientesRes is NetworkResult.Error) {
                _state.update { it.copy(isLoading = false, isRefreshing = false, error = clientesRes.error.asUiText()) }
                return@launch
            }
            val activos = (clientesRes as NetworkResult.Success).data.filter { it.activo }

            val saldosRes = cobranzaRepository.porCobrar(sucursalId)
            if (saldosRes is NetworkResult.Error) {
                _state.update { it.copy(isLoading = false, isRefreshing = false, error = saldosRes.error.asUiText()) }
                return@launch
            }
            val cuentasPorCliente = (saldosRes as NetworkResult.Success).data.groupBy { it.clienteId }
            // "Falta semanal" = con deuda y sin abono/anticipo desde el lunes de la semana ISO actual.
            val lunesSemana = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

            val items = activos.map { cliente ->
                val cuentas = cuentasPorCliente[cliente.id].orEmpty()
                val saldo = cuentas.fold(BigDecimal.ZERO) { acc, c -> acc + c.saldo }
                val ultimoPago = cuentas.mapNotNull { it.ultimoPago }.maxOrNull()
                val falta = saldo > BigDecimal.ZERO && (ultimoPago == null || ultimoPago.isBefore(lunesSemana))
                ClienteCobranza(cliente = cliente, saldoGlobal = saldo, ultimoPago = ultimoPago, faltaSemana = falta)
            }.sortedWith(
                // 1) Quienes YA registraron pago esta semana (al día) → arriba,
                //    ordenados por fecha del último pago (más reciente primero).
                // 2) Quienes tienen deuda y NO han registrado pago esta semana → al final.
                // 3) Sin deuda → hasta el fondo (no aplican al ciclo).
                // Al registrar un abono, el cliente sube del grupo 2 al 1 automáticamente
                // en el próximo load (recargarTrasMovimiento lo dispara).
                compareBy<ClienteCobranza>(
                    { grupo(it) },
                    // Más reciente primero → invierto con LocalDate.MIN como "sin datos".
                    { java.time.LocalDate.now().toEpochDay() - (it.ultimoPago?.toEpochDay() ?: Long.MIN_VALUE) },
                )
            )
            _state.update { it.copy(isLoading = false, isRefreshing = false, clientes = items) }
        }
    }

    /** Clave de grupo para ordenar la lista: menor = más arriba. */
    private fun grupo(c: ClienteCobranza): Int = when {
        c.conDeuda && !c.faltaSemana -> 0   // Ya registró pago esta semana → arriba.
        c.faltaSemana -> 1                  // Con deuda pero sin abono esta semana → al final.
        else -> 2                            // Sin deuda → hasta el fondo.
    }

    private fun loadCuentas(clienteId: Long, refreshing: Boolean = false) {
        _state.update {
            if (refreshing) it.copy(detalleRefreshing = true)
            else it.copy(cuentasLoading = true, cuentasError = null)
        }
        viewModelScope.launch {
            when (val result = cobranzaRepository.cuentasDeCliente(clienteId)) {
                is NetworkResult.Success -> {
                    // Orden: mayor saldo (más deuda) primero. Desempata por id
                    // ascendente para que el orden sea determinista entre recargas.
                    val cuentas = result.data.sortedWith(
                        compareByDescending<Cuenta> { it.saldo }.thenBy { it.id }
                    )
                    // Mantener la cuenta seleccionada (con saldo fresco) o auto-seleccionar la primera.
                    val seleccion = _state.value.cuentaSeleccionada
                        ?.let { sel -> cuentas.firstOrNull { it.id == sel.id } }
                        ?: cuentas.firstOrNull()
                    _state.update { it.copy(cuentasLoading = false, detalleRefreshing = false, cuentas = cuentas, cuentaSeleccionada = seleccion) }
                    seleccion?.let { loadHistorial(it.id); loadVentas(it.id) }
                }
                is NetworkResult.Error -> _state.update { it.copy(cuentasLoading = false, detalleRefreshing = false, cuentasError = result.error.asUiText()) }
            }
        }
    }

    /** Pull-to-refresh del detalle: recarga cuentas (saldos) → historial + compras. */
    fun refrescarDetalle() {
        _state.value.clienteSeleccionado?.let { loadCuentas(it.id, refreshing = true) }
    }

    private fun loadHistorial(cuentaId: Long) {
        _state.update { it.copy(historialLoading = true, historialError = null) }
        viewModelScope.launch {
            when (val result = cobranzaRepository.historial(cuentaId)) {
                is NetworkResult.Success -> _state.update { it.copy(historialLoading = false, historial = result.data) }
                is NetworkResult.Error -> _state.update { it.copy(historialLoading = false, historialError = result.error.asUiText()) }
            }
        }
    }

    private fun loadVentas(cuentaId: Long) {
        _state.update { it.copy(ventasLoading = true) }
        viewModelScope.launch {
            when (val result = ventaRepository.ventasDeCuenta(cuentaId)) {
                is NetworkResult.Success -> _state.update { it.copy(ventasLoading = false, ventasCuenta = result.data) }
                is NetworkResult.Error -> _state.update { it.copy(ventasLoading = false) }
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
