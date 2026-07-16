package com.example.tienda.feature.cobranzas.ui
import com.example.tienda.feature.cobranzas.domain.*
import com.example.tienda.core.util.aMoneda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tienda.feature.ventas.domain.RenglonVenta
import com.example.tienda.feature.ventas.domain.TipoRenglon
import com.example.tienda.feature.ventas.domain.Venta
import com.example.tienda.core.ui.components.DayHeader
import com.example.tienda.core.ui.components.PantallaError
import com.example.tienda.core.ui.components.PantallaEstadoVacio
import com.example.tienda.core.ui.components.ShowUiMessage
import com.example.tienda.core.ui.components.TiendaPullRefresh
import com.example.tienda.core.ui.components.TiendaSnackbarHost
import com.example.tienda.core.ui.components.TiendaTopBar
import com.example.tienda.core.ui.components.tarjeta
import com.example.tienda.core.ui.theme.TiendaShapes
import com.example.tienda.core.ui.theme.Accent
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.Danger
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import com.example.tienda.core.ui.theme.Success
import com.example.tienda.core.ui.theme.Surface
import com.example.tienda.core.ui.theme.SurfaceMuted
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary
import com.example.tienda.core.ui.theme.Warning
import java.math.BigDecimal

/**
 * Detalle de cliente: saldo global, tabs de cuentas, saldo de la cuenta
 * seleccionada, registrar abono, e historial de movimientos de esa cuenta.
 */
@Composable
fun ClienteDetalle(state: CobranzasUiState, viewModel: CobranzasViewModel) {
    val cliente = state.clienteSeleccionado ?: return

    val snackbarHostState = remember { SnackbarHostState() }
    ShowUiMessage(state.mensaje, snackbarHostState, viewModel::clearMensaje)
    // Solo por snackbar si ya hay cuentas cargadas; si no, la pantalla de error se encarga.
    ShowUiMessage(if (state.cuentas.isNotEmpty()) state.error else null, snackbarHostState, viewModel::clearError)

    // Movimiento manual que se está editando (abre el editor inferior).
    var movEditando by remember { mutableStateOf<HistorialItem?>(null) }

    var monto by remember { mutableStateOf("") }
    // Al confirmarse un movimiento (mensaje), limpiamos el campo.
    LaunchedEffect(state.mensaje) { if (state.mensaje != null) monto = "" }

    val saldoGlobal = state.cuentas.fold(BigDecimal.ZERO) { acc, c -> acc + c.saldo }
    val saldoCuenta = state.cuentaSeleccionada?.saldo ?: BigDecimal.ZERO
    // Para enlazar los renglones de cada movimiento de venta (concepto venta) por su ventaId.
    val ventasPorId = state.ventasCuenta.associateBy { it.id }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TiendaTopBar(
                title = cliente.nombreCompleto,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = viewModel::volverAClientes,
            )

            when {
                state.cuentasLoading && state.cuentas.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                    }

                state.cuentasError != null && state.cuentas.isEmpty() ->
                    PantallaError("No se pudieron cargar las cuentas", onReintentar = viewModel::refrescarDetalle)

                state.cuentas.isEmpty() ->
                    PantallaEstadoVacio("Este cliente no tiene cuentas")

                else -> TiendaPullRefresh(
                    refreshing = state.detalleRefreshing,
                    onRefresh = viewModel::refrescarDetalle,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        // Módulo 1: Hero con saldo TOTAL (todas las cuentas del cliente).
                        item { SaldoTotalHero(saldo = saldoGlobal, numCuentas = state.cuentas.size) }

                        // Módulo 2: Cuenta seleccionada — encabezado + selector estilo pill + card unificada (saldo + abono).
                        item {
                            SeccionCuenta(
                                cuentas = state.cuentas,
                                seleccionadaId = state.cuentaSeleccionada?.id,
                                onSelect = viewModel::seleccionarCuenta,
                                onLongPress = viewModel::abrirRenombrarCuenta,
                                saldoCuenta = saldoCuenta,
                                monto = monto,
                                onMontoChange = { monto = it },
                                registrando = state.registrando,
                                formError = state.formError?.asString(),
                                onConfirmar = { viewModel.registrarAbono(monto, null) },
                            )
                        }

                        // Módulo 3: Historial de movimientos.
                        item {
                            Text(
                                "Historial de movimientos",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        val movimientos = state.historial?.movimientos.orEmpty()
                        when {
                            state.historialLoading && state.historial == null ->
                                item {
                                    Box(Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                                    }
                                }

                            // Falló la carga (antes se ocultaba como "Sin movimientos").
                            state.historialError != null && state.historial == null ->
                                item {
                                    Column(Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            state.historialError?.asString() ?: "No se pudo cargar el historial",
                                            style = MaterialTheme.typography.bodyMedium, color = Danger,
                                        )
                                        TextButton(onClick = viewModel::refrescarDetalle) { Text("Reintentar", color = Primary) }
                                    }
                                }

                            movimientos.isEmpty() ->
                                item { Text("Sin movimientos", style = MaterialTheme.typography.bodyMedium, color = TextSecondary) }

                            // DayHeader solo en los días con movimientos (nombre del día + fecha completa).
                            else -> movimientos.groupBy { it.fecha }.forEach { (fecha, delDia) ->
                                item(key = "mh-$fecha") { DayHeader(java.time.LocalDate.parse(fecha), fechaCompleta = true) }
                                items(delDia) { mov ->
                                    MovimientoRow(
                                        mov,
                                        ventasPorId[mov.ventaId],
                                        onEditar = if (mov.esManual) ({ movEditando = mov }) else null,
                                    )
                                }
                            }
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }

        TiendaSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    movEditando?.let { mov ->
        EditarMovimientoSheet(
            item = mov,
            onGuardar = { nuevoMonto, nota ->
                mov.movimientoId?.let { viewModel.editarMovimiento(it, nuevoMonto, nota.ifBlank { null }) }
                movEditando = null
            },
            onEliminar = {
                mov.movimientoId?.let { viewModel.eliminarMovimiento(it) }
                movEditando = null
            },
            onDismiss = { movEditando = null },
        )
    }

    // Diálogo para renombrar la cuenta (disparado por long-press en el tab).
    if (state.renombrarCuentaAbierto != null) {
        RenombrarCuentaDialog(state = state, viewModel = viewModel)
    }
}

@Composable
private fun RenombrarCuentaDialog(state: CobranzasUiState, viewModel: CobranzasViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::cerrarRenombrarCuenta,
        title = { Text("Renombrar cuenta", color = TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = state.renombrarNombre,
                    onValueChange = viewModel::onRenombrarNombreChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre (opcional)") },
                    singleLine = true,
                    shape = TiendaShapes.Field,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Border,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = Primary,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                )
                state.renombrarError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it.asString(), style = MaterialTheme.typography.bodyMedium, color = Danger)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = viewModel::guardarNombreCuenta, enabled = !state.renombrando) {
                Text(if (state.renombrando) "Guardando…" else "Guardar", color = Primary, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = viewModel::cerrarRenombrarCuenta) { Text("Cancelar", color = TextSecondary) } },
        containerColor = Surface,
        shape = TiendaShapes.Dialog,
    )
}

/**
 * Módulo destacado con el saldo TOTAL del cliente (suma de todas sus cuentas).
 * Fondo Primary + texto en OnPrimary para diferenciarlo del saldo por cuenta.
 */
@Composable
private fun SaldoTotalHero(saldo: BigDecimal, numCuentas: Int) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(TiendaShapes.Card)
            .background(Primary)
            .padding(20.dp),
    ) {
        Text(
            "SALDO TOTAL",
            style = MaterialTheme.typography.labelMedium,
            color = OnPrimary.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            saldo.aMoneda(),
            style = MaterialTheme.typography.displaySmall,
            color = OnPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (numCuentas == 1) "1 cuenta" else "$numCuentas cuentas",
            style = MaterialTheme.typography.labelMedium,
            color = OnPrimary.copy(alpha = 0.7f),
        )
    }
}

/**
 * Módulo unificado de la cuenta seleccionada: header + selector segmentado
 * (estilo Configuraciones/Apariencia) + card con saldo y registrar abono.
 */
@Composable
private fun SeccionCuenta(
    cuentas: List<Cuenta>,
    seleccionadaId: Long?,
    onSelect: (Cuenta) -> Unit,
    onLongPress: (Cuenta) -> Unit,
    saldoCuenta: BigDecimal,
    monto: String,
    onMontoChange: (String) -> Unit,
    registrando: Boolean,
    formError: String?,
    onConfirmar: () -> Unit,
) {
    Column {
        Text(
            "CUENTA",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        SelectorCuentas(cuentas, seleccionadaId, onSelect, onLongPress)
        Spacer(Modifier.height(12.dp))
        CuentaSeleccionadaCard(
            saldoCuenta = saldoCuenta,
            monto = monto,
            onMontoChange = onMontoChange,
            registrando = registrando,
            formError = formError,
            onConfirmar = onConfirmar,
        )
    }
}

/**
 * Selector estilo pill segmentado (como el de Tema en Configuraciones).
 * Tap = seleccionar; long-press = renombrar. Scroll horizontal si son muchas.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun SelectorCuentas(
    cuentas: List<Cuenta>,
    seleccionadaId: Long?,
    onSelect: (Cuenta) -> Unit,
    onLongPress: (Cuenta) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .clip(TiendaShapes.Field)
                .background(SurfaceMuted)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            cuentas.forEachIndexed { i, cuenta ->
                val activa = cuenta.id == seleccionadaId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activa) Surface else androidx.compose.ui.graphics.Color.Transparent)
                        .combinedClickable(
                            onClick = { onSelect(cuenta) },
                            onLongClick = { onLongPress(cuenta) },
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        cuenta.nombre ?: "Cuenta ${i + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (activa) Primary else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/**
 * Card de la cuenta seleccionada: saldo + registrar abono, unificados para
 * dejar claro que ambos datos corresponden a la MISMA cuenta.
 */
@Composable
private fun CuentaSeleccionadaCard(
    saldoCuenta: BigDecimal,
    monto: String,
    onMontoChange: (String) -> Unit,
    registrando: Boolean,
    formError: String?,
    onConfirmar: () -> Unit,
) {
    val conDeuda = saldoCuenta > BigDecimal.ZERO
    Column(
        Modifier
            .fillMaxWidth()
            .tarjeta()
            .padding(16.dp),
    ) {
        // Saldo de la cuenta seleccionada.
        Text("Saldo de la cuenta", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(
            saldoCuenta.aMoneda(),
            style = MaterialTheme.typography.headlineMedium,
            color = if (conDeuda) Danger else Success,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Border)
        Spacer(Modifier.height(16.dp))

        // Registrar abono (inline, misma card — es acción sobre esta cuenta).
        Text("Registrar abono", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = monto,
            onValueChange = onMontoChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Monto") },
            singleLine = true,
            enabled = conDeuda && !registrando,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = TiendaShapes.Field,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border,
                focusedLabelColor = Primary,
                unfocusedLabelColor = TextSecondary,
                cursorColor = Primary,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
            ),
        )
        formError?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = Danger)
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(TiendaShapes.Button)
                .background(if (conDeuda) Primary else SurfaceMuted)
                .clickable(enabled = conDeuda && !registrando, onClick = onConfirmar),
            contentAlignment = Alignment.Center,
        ) {
            if (registrando) {
                CircularProgressIndicator(Modifier.size(22.dp), color = OnPrimary, strokeWidth = 2.dp)
            } else {
                Text(
                    "Confirmar pago",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (conDeuda) OnPrimary else TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (!conDeuda) {
            Spacer(Modifier.height(8.dp))
            Text("Esta cuenta no tiene saldo por cobrar.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun MovimientoRow(item: HistorialItem, venta: Venta?, onEditar: (() -> Unit)? = null) {
    val efecto = item.efectoSaldo
    val signo = when {
        efecto > BigDecimal.ZERO -> "+"
        efecto < BigDecimal.ZERO -> "-"
        else -> ""
    }
    val color = when {
        efecto > BigDecimal.ZERO -> Danger
        efecto < BigDecimal.ZERO -> Accent
        else -> TextSecondary
    }
    // Renglones del movimiento: en una venta los muestro según el tipo del concepto
    // (crédito/contado), así una venta mixta no repite renglones en ambos movimientos.
    val renglones = when (item.concepto) {
        TipoMovimiento.CARGO_CREDITO -> venta?.renglones?.filter { it.tipo == TipoRenglon.CREDITO }.orEmpty()
        TipoMovimiento.VENTA_CONTADO -> venta?.renglones?.filter { it.tipo == TipoRenglon.CONTADO }.orEmpty()
        else -> emptyList()
    }
    // Estilo de icono coherente con "Actividad reciente" del Home.
    val iconColor = iconoConceptoColor(item.concepto)
    val icono = iconoConcepto(item.concepto)
    Column(
        Modifier
            .fillMaxWidth()
            .tarjeta()
            .then(if (onEditar != null) Modifier.clickable(onClick = onEditar) else Modifier)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icono, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(conceptoLabel(item.concepto), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("${item.fecha} · ${item.hora}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(Modifier.size(8.dp))
            Text("$signo${item.monto.aMoneda()}", style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.SemiBold)
        }
        if (renglones.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            renglones.forEach { CompraRenglonRow(it) }
        }
        if (item.registradoPor.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text("Registró: ${item.registradoPor}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

/** Icono coherente con "Actividad reciente" del Home. */
private fun iconoConcepto(t: TipoMovimiento) = when (t) {
    TipoMovimiento.VENTA_CONTADO, TipoMovimiento.CARGO_CREDITO -> Icons.Filled.PointOfSale
    else -> Icons.Filled.Payments
}

private fun iconoConceptoColor(t: TipoMovimiento) = when (t) {
    // Ventas (contado y crédito) usan el mismo color de icono; el monto rojo del
    // cargo a crédito ya comunica la deuda.
    TipoMovimiento.VENTA_CONTADO, TipoMovimiento.CARGO_CREDITO -> Primary
    TipoMovimiento.ABONO, TipoMovimiento.ANTICIPO -> Accent   // ingresos
    TipoMovimiento.DEVOLUCION -> Warning
    TipoMovimiento.DESCONOCIDO -> TextSecondary
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarMovimientoSheet(
    item: HistorialItem,
    onGuardar: (monto: String, nota: String) -> Unit,
    onEliminar: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var monto by remember { mutableStateOf(item.monto.toPlainString()) }
    var nota by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var pedirEliminar by remember { mutableStateOf(false) }
    val concepto = conceptoLabel(item.concepto).lowercase()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Surface) {
        Column(Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Text("Editar $concepto", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            MovTextField(monto, { monto = it }, "Monto", KeyboardType.Decimal)
            Spacer(Modifier.height(10.dp))
            MovTextField(nota, { nota = it }, "Nota (en blanco no cambia)", KeyboardType.Text)
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Danger)
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(TiendaShapes.Field)
                    .background(Primary)
                    .clickable {
                        val m = monto.trim().toBigDecimalOrNull()
                        if (m == null || m <= BigDecimal.ZERO) error = "Monto inválido"
                        else onGuardar(monto, nota)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text("Guardar cambios", style = MaterialTheme.typography.labelLarge, color = OnPrimary)
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = { pedirEliminar = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Eliminar movimiento", color = Danger)
            }
        }
    }

    if (pedirEliminar) {
        AlertDialog(
            onDismissRequest = { pedirEliminar = false },
            title = { Text("Eliminar movimiento", color = TextPrimary) },
            text = { Text("¿Eliminar este $concepto? Se recalculará el saldo de la cuenta.", color = TextSecondary) },
            confirmButton = { TextButton(onClick = { pedirEliminar = false; onEliminar() }) { Text("Eliminar", color = Danger) } },
            dismissButton = { TextButton(onClick = { pedirEliminar = false }) { Text("Cancelar", color = Primary) } },
            containerColor = Surface,
        )
    }
}

@Composable
private fun MovTextField(value: String, onValueChange: (String) -> Unit, label: String, keyboardType: KeyboardType) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = if (keyboardType == KeyboardType.Text) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
        ),
        shape = TiendaShapes.Field,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = Border,
            focusedLabelColor = Primary,
            unfocusedLabelColor = TextSecondary,
            cursorColor = Primary,
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
        ),
    )
}

@Composable
private fun CompraRenglonRow(r: RenglonVenta) {
    Row(Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(r.descripcion, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text(
                "${r.cantidad} × ${r.precioUnit.aMoneda()} · ${if (r.tipo == TipoRenglon.CREDITO) "Crédito" else "Contado"}",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary,
            )
        }
        Text(r.importe.aMoneda(), style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Centrado(text: String) {
    Box(Modifier
        .fillMaxSize()
        .padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

// ── Helpers ──

private fun conceptoLabel(t: TipoMovimiento): String = when (t) {
    TipoMovimiento.ABONO -> "Abono"
    TipoMovimiento.ANTICIPO -> "Anticipo"
    TipoMovimiento.DEVOLUCION -> "Devolución"
    TipoMovimiento.CARGO_CREDITO -> "Venta a crédito"
    TipoMovimiento.VENTA_CONTADO -> "Venta de contado"
    TipoMovimiento.DESCONOCIDO -> "Movimiento"
}
