package com.example.tienda.feature.ventas.ui
import com.example.tienda.feature.ventas.data.VentaRepository
import com.example.tienda.feature.ventas.domain.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tienda.R
import com.example.tienda.feature.clientes.domain.Cliente
import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.error.asUiText
import com.example.tienda.feature.clientes.data.ClienteRepository
import com.example.tienda.feature.cobranzas.domain.Cuenta
import com.example.tienda.feature.cobranzas.data.CobranzaRepository
import com.example.tienda.core.session.SessionManager
import com.example.tienda.core.util.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * Ventas: lista del día (filtrable por estado) + worklist de pendientes, y alta
 * de venta (mostrador solo contado; cliente con cuenta para contado/crédito/mixto;
 * abrir cuenta; toggle pendiente; consolidar/cancelar). La sucursal la fija el
 * switch del Home con [setSucursal].
 */
class VentasViewModel(
    private val ventaRepository: VentaRepository,
    private val clienteRepository: ClienteRepository,
    private val cobranzaRepository: CobranzaRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(
        VentasUiState(esAdministrador = sessionManager.getSession()?.esAdministrador == true)
    )
    val state: StateFlow<VentasUiState> = _state.asStateFlow()

    private var sucursalCargada = false
    private var clienteSearchJob: Job? = null

    fun setSucursal(sucursalId: Long?) {
        if (sucursalCargada && sucursalId == _state.value.selectedSucursalId) return
        sucursalCargada = true
        _state.update { it.copy(selectedSucursalId = sucursalId) }
        cargarTodo()
    }

    // ── Lista ──

    fun onFiltroEstado(estado: EstadoVenta?) {
        _state.update { it.copy(filtroEstado = estado) }
        // El filtro "Pendientes" muestra el worklist (state.pendientes); el resto, la semana.
        if (estado == EstadoVenta.PENDIENTE) loadPendientes() else loadVentas()
    }

    // Búsqueda y filtro por tipo filtran en memoria (no recargan).
    fun onQueryChange(value: String) = _state.update { it.copy(query = value) }

    fun onFiltroTipo(tipo: FiltroTipo) = _state.update { it.copy(filtroTipo = tipo) }

    // ── Navegación de semanas ──

    fun semanaAnterior() {
        _state.update { it.copy(semanaInicio = it.semanaInicio.minusWeeks(1)) }
        loadVentas()
    }

    fun semanaSiguiente() {
        if (_state.value.esSemanaActual) return // no se navega al futuro
        _state.update { it.copy(semanaInicio = it.semanaInicio.plusWeeks(1)) }
        loadVentas()
    }

    fun cargarDetalle(ventaId: Long) {
        _state.update { it.copy(ventaDetalle = null, detalleLoading = true) }
        viewModelScope.launch {
            when (val r = ventaRepository.obtener(ventaId)) {
                is NetworkResult.Success -> _state.update { it.copy(detalleLoading = false, ventaDetalle = r.data) }
                is NetworkResult.Error -> _state.update { it.copy(detalleLoading = false, error = r.error.asUiText()) }
            }
        }
    }

    fun cerrarDetalle() = _state.update { it.copy(ventaDetalle = null, detalleLoading = false) }

    // ── Edición de renglones (solo ventas pendientes) ──

    fun abrirEditarRenglones(venta: Venta) = _state.update {
        it.limpiarForm().copy(
            mode = VentasMode.EDITAR,
            editandoVentaId = venta.id,
            editandoVentaEstado = venta.estado,
            renglones = venta.renglones.map { r -> RenglonVentaInput(r.descripcion, r.cantidad, r.precioUnit, r.tipo) },
        )
    }

    fun guardarRenglones() {
        val s = _state.value
        val ventaId = s.editandoVentaId ?: return
        if (s.renglones.isEmpty()) return formError(R.string.error_venta_sin_renglones)
        // Reglas específicas (pendiente conserva crédito, saldo no negativo, etc.) las
        // valida el backend — devuelve el mensaje concreto si algo falla.

        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            when (val r = ventaRepository.editarRenglones(ventaId, s.renglones)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(mensaje = UiText.Resource(R.string.venta_renglones_actualizados)).limpiarForm() }
                    cargarTodo()
                }
                is NetworkResult.Error -> _state.update { it.copy(saving = false, formError = r.error.asUiText()) }
            }
        }
    }

    fun retry() = cargarTodo()

    fun refresh() {
        loadVentas(refreshing = true)
        loadPendientes()
    }

    // ── Acciones de venta ──

    fun consolidar(ventaId: Long) {
        viewModelScope.launch {
            when (val r = ventaRepository.consolidar(ventaId)) {
                is NetworkResult.Success -> { _state.update { it.copy(mensaje = UiText.Resource(R.string.venta_consolidada)) }; cargarTodo() }
                is NetworkResult.Error -> _state.update { it.copy(error = r.error.asUiText()) }
            }
        }
    }

    fun cancelar(ventaId: Long) {
        viewModelScope.launch {
            when (val r = ventaRepository.cancelar(ventaId)) {
                is NetworkResult.Success -> { _state.update { it.copy(mensaje = UiText.Resource(R.string.venta_cancelada)) }; cargarTodo() }
                is NetworkResult.Error -> _state.update { it.copy(error = r.error.asUiText()) }
            }
        }
    }

    // ── Formulario nueva venta ──

    fun abrirNueva() = _state.update { it.limpiarForm().copy(mode = VentasMode.NUEVA) }

    fun cerrarForm() = _state.update { it.limpiarForm() }

    fun setMostrador(mostrador: Boolean) = _state.update {
        it.copy(
            esMostrador = mostrador,
            clienteSeleccionado = null,
            cuentasCliente = emptyList(),
            cuentaSeleccionada = null,
            cuentaCreada = false,
            clienteQuery = "",
            clientesResultado = emptyList(),
            nombreMostrador = if (mostrador) it.nombreMostrador else "",
            formError = null,
        )
    }

    fun onNombreMostradorChange(value: String) = _state.update { it.copy(nombreMostrador = value) }

    // pendiente y anticipo son mutuamente excluyentes (pendiente aún no genera deuda).
    fun onPendienteChange(value: Boolean) = _state.update {
        it.copy(pendiente = value, anticipo = if (value) "" else it.anticipo)
    }

    fun onAnticipoChange(value: String) = _state.update {
        it.copy(anticipo = value, pendiente = if (value.isNotBlank()) false else it.pendiente)
    }

    // Búsqueda de cliente
    fun onClienteQueryChange(value: String) {
        _state.update { it.copy(clienteQuery = value) }
        clienteSearchJob?.cancel()
        if (value.isBlank()) {
            _state.update { it.copy(clientesResultado = emptyList(), buscandoClientes = false) }
            return
        }
        clienteSearchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _state.update { it.copy(buscandoClientes = true) }
            // Acotado a la sucursal en contexto (switch del Home), consistente con
            // Cobranzas y Clientes: solo se vende a clientes de la sucursal seleccionada.
            val result = clienteRepository.listar(q = value, pagina = 1, tamano = 20, sucursalId = _state.value.selectedSucursalId)
            _state.update {
                it.copy(
                    buscandoClientes = false,
                    clientesResultado = (result as? NetworkResult.Success)?.data ?: emptyList(),
                )
            }
        }
    }

    fun seleccionarCliente(cliente: Cliente) {
        _state.update { it.copy(clienteSeleccionado = cliente, clientesResultado = emptyList(), clienteQuery = "", cuentaSeleccionada = null, cuentaCreada = false, formError = null) }
        loadCuentas(cliente.id)
    }

    fun seleccionarCuenta(cuenta: Cuenta) = _state.update { it.copy(cuentaSeleccionada = cuenta) }

    fun deseleccionarCliente() = _state.update {
        it.copy(clienteSeleccionado = null, cuentasCliente = emptyList(), cuentaSeleccionada = null, cuentaCreada = false, anticipo = "", formError = null)
    }

    fun abrirCuenta(nombre: String?) {
        val cliente = _state.value.clienteSeleccionado ?: return
        _state.update { it.copy(cuentasLoading = true, formError = null) }
        viewModelScope.launch {
            when (val r = cobranzaRepository.abrirCuenta(cliente.id, nombre)) {
                is NetworkResult.Success -> _state.update {
                    it.copy(
                        cuentasLoading = false,
                        cuentasCliente = it.cuentasCliente + r.data,
                        cuentaSeleccionada = r.data,
                        cuentaCreada = true,
                        mensaje = UiText.Resource(R.string.cuenta_abierta),
                    )
                }
                is NetworkResult.Error -> _state.update { it.copy(cuentasLoading = false, formError = r.error.asUiText()) }
            }
        }
    }

    // Renglones
    fun agregarRenglon(descripcion: String, cantidad: Int, precioUnitTexto: String, tipo: TipoRenglon) {
        val renglon = construirRenglon(descripcion, cantidad, precioUnitTexto, tipo) ?: return
        _state.update { it.copy(renglones = it.renglones + renglon, formError = null) }
    }

    fun editarRenglon(index: Int, descripcion: String, cantidad: Int, precioUnitTexto: String, tipo: TipoRenglon) {
        val renglon = construirRenglon(descripcion, cantidad, precioUnitTexto, tipo) ?: return
        _state.update {
            if (index !in it.renglones.indices) it
            else it.copy(renglones = it.renglones.mapIndexed { i, r -> if (i == index) renglon else r }, formError = null)
        }
    }

    fun quitarRenglon(index: Int) = _state.update {
        if (index in it.renglones.indices) it.copy(renglones = it.renglones.filterIndexed { i, _ -> i != index }) else it
    }

    fun crearVenta() {
        val s = _state.value
        if (s.renglones.isEmpty()) return formError(R.string.error_venta_sin_renglones)

        val cuentaId: Long?
        val nombreMostrador: String?
        if (s.esMostrador) {
            if (s.nombreMostrador.isBlank()) return formError(R.string.error_mostrador_nombre)
            if (s.hayCredito) return formError(R.string.error_mostrador_solo_contado)
            cuentaId = null
            nombreMostrador = s.nombreMostrador
        } else {
            if (s.clienteSeleccionado == null) return formError(R.string.error_venta_cliente_requerido)
            if (s.cuentaSeleccionada == null) return formError(R.string.error_venta_cuenta_requerida)
            cuentaId = s.cuentaSeleccionada.id
            nombreMostrador = null
        }

        // Anticipo: solo aplica con crédito, cuenta y NO pendiente.
        val aplicaAnticipo = !s.esMostrador && s.hayCredito && !s.pendiente && s.cuentaSeleccionada != null
        var anticipoMonto: BigDecimal? = null
        if (aplicaAnticipo && s.anticipo.isNotBlank()) {
            val monto = s.anticipo.trim().toBigDecimalOrNull()
            if (monto == null || monto <= BigDecimal.ZERO) return formError(R.string.error_monto_invalido)
            val deudaProyectada = s.cuentaSeleccionada.saldo + s.totalCredito
            if (monto > deudaProyectada) return formError(R.string.error_anticipo_supera)
            anticipoMonto = monto
        }

        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            when (val ventaRes = ventaRepository.crear(cuentaId, nombreMostrador, s.pendiente, s.renglones)) {
                is NetworkResult.Success -> {
                    // Venta creada; si hay anticipo, lo registramos sobre la cuenta.
                    if (anticipoMonto != null && cuentaId != null) {
                        val antRes = cobranzaRepository.registrarAnticipo(cuentaId, anticipoMonto, null)
                        if (antRes is NetworkResult.Error) {
                            // La venta sí se creó; avisamos que el anticipo falló.
                            _state.update { it.copy(error = antRes.error.asUiText(), mensaje = UiText.Resource(R.string.venta_creada)).limpiarForm() }
                            cargarTodo()
                            return@launch
                        }
                    }
                    _state.update { it.copy(mensaje = UiText.Resource(R.string.venta_creada)).limpiarForm() }
                    cargarTodo()
                }
                is NetworkResult.Error -> _state.update { it.copy(saving = false, formError = ventaRes.error.asUiText()) }
            }
        }
    }

    // ── Limpieza ──

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearMensaje() = _state.update { it.copy(mensaje = null) }
    fun clearFormError() = _state.update { it.copy(formError = null) }

    // ── Privados ──

    private fun formError(res: Int) {
        _state.update { it.copy(formError = UiText.Resource(res)) }
    }

    /** Valida y arma un renglón; si algo está mal publica formError y devuelve null. */
    private fun construirRenglon(descripcion: String, cantidad: Int, precioUnitTexto: String, tipo: TipoRenglon): RenglonVentaInput? {
        val precio = precioUnitTexto.trim().toBigDecimalOrNull()
        if (descripcion.isBlank() || cantidad < 1 || precio == null || precio <= BigDecimal.ZERO) {
            formError(R.string.error_renglon_invalido)
            return null
        }
        return RenglonVentaInput(descripcion.trim(), cantidad, precio, tipo)
    }

    private fun cargarTodo() {
        loadVentas()
        loadPendientes()
    }

    private fun loadVentas(refreshing: Boolean = false) {
        _state.update { it.copy(isLoading = !refreshing, isRefreshing = refreshing, error = null) }
        viewModelScope.launch {
            val s = _state.value
            when (val r = ventaRepository.listar(
                sucursalId = s.selectedSucursalId,
                fechaDesde = s.semanaInicio.toString(),
                fechaHasta = s.semanaFin.toString(),
                estado = s.filtroEstado,
                pagina = 1,
                tamano = 100,
            )) {
                is NetworkResult.Success -> _state.update { it.copy(isLoading = false, isRefreshing = false, ventas = r.data) }
                is NetworkResult.Error -> _state.update { it.copy(isLoading = false, isRefreshing = false, error = r.error.asUiText()) }
            }
        }
    }

    private fun loadPendientes() {
        viewModelScope.launch {
            (ventaRepository.pendientes() as? NetworkResult.Success)?.let { res ->
                _state.update { it.copy(pendientes = res.data) }
            }
        }
    }

    private fun loadCuentas(clienteId: Long) {
        _state.update { it.copy(cuentasLoading = true) }
        viewModelScope.launch {
            when (val r = cobranzaRepository.cuentasDeCliente(clienteId)) {
                is NetworkResult.Success -> _state.update {
                    it.copy(cuentasLoading = false, cuentasCliente = r.data, cuentaSeleccionada = r.data.firstOrNull())
                }
                is NetworkResult.Error -> _state.update { it.copy(cuentasLoading = false, formError = r.error.asUiText()) }
            }
        }
    }

    private fun VentasUiState.limpiarForm(): VentasUiState = copy(
        mode = VentasMode.LISTA,
        editandoVentaId = null,
        editandoVentaEstado = null,
        esMostrador = false,
        nombreMostrador = "",
        clienteQuery = "",
        clientesResultado = emptyList(),
        buscandoClientes = false,
        clienteSeleccionado = null,
        cuentasCliente = emptyList(),
        cuentasLoading = false,
        cuentaSeleccionada = null,
        cuentaCreada = false,
        pendiente = false,
        anticipo = "",
        renglones = emptyList(),
        saving = false,
        formError = null,
    )

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
