package com.example.tienda.feature.cobranzas.ui
import com.example.tienda.feature.cobranzas.domain.*
import com.example.tienda.core.util.aMoneda

import com.example.tienda.feature.clientes.domain.Cliente
import com.example.tienda.feature.ventas.domain.Venta
import com.example.tienda.core.util.UiText

/**
 * Flujo: lista de clientes activos (de la sucursal seleccionada en el Home) con
 * saldo global + semáforo → (futuro) cuentas del cliente → historial + registrar
 * abono/devolución. La sucursal la fija el Home vía setSucursal().
 */
data class CobranzasUiState(
    // Sucursal seleccionada (la fija el switch del Home)
    val selectedSucursalId: Long? = null,

    // Lista de clientes activos con saldo global + semáforo
    val query: String = "",
    val clientes: List<ClienteCobranza> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,

    // Cliente seleccionado → sus cuentas
    val clienteSeleccionado: Cliente? = null,
    val cuentas: List<Cuenta> = emptyList(),
    val cuentasLoading: Boolean = false,
    val cuentasError: UiText? = null,
    val detalleRefreshing: Boolean = false, // pull-to-refresh en la pantalla de detalle

    // Cuenta seleccionada → historial
    val cuentaSeleccionada: Cuenta? = null,
    val historial: HistorialCuenta? = null,
    val historialLoading: Boolean = false,
    val historialError: UiText? = null,

    // Compras (ventas) de la cuenta seleccionada, con sus renglones
    val ventasCuenta: List<Venta> = emptyList(),
    val ventasLoading: Boolean = false,

    // Registrar movimiento (formulario inline sobre la cuenta seleccionada)
    val registrando: Boolean = false,
    val formError: UiText? = null,

    // Diálogo para renombrar una cuenta (long-press en el tab)
    val renombrarCuentaAbierto: Cuenta? = null,
    val renombrarNombre: String = "",
    val renombrando: Boolean = false,
    val renombrarError: UiText? = null,

    // Feedback
    val mensaje: UiText? = null,
)
