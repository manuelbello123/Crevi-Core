package com.example.tienda.feature.ventas.ui
import com.example.tienda.feature.ventas.domain.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.tienda.feature.clientes.domain.Cliente
import com.example.tienda.feature.cobranzas.domain.Cuenta
import com.example.tienda.core.util.aMoneda
import com.example.tienda.core.ui.components.ShowUiMessage
import com.example.tienda.core.ui.components.TiendaSnackbarHost
import com.example.tienda.core.ui.components.TiendaTopBar
import com.example.tienda.core.ui.components.tarjeta
import com.example.tienda.core.ui.theme.Accent
import com.example.tienda.core.ui.theme.TiendaShapes
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.Danger
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import com.example.tienda.core.ui.theme.PrimarySoft
import com.example.tienda.core.ui.theme.Surface
import com.example.tienda.core.ui.theme.SurfaceMuted
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VentaForm(state: VentasUiState, viewModel: VentasViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    ShowUiMessage(state.mensaje, snackbarHostState, viewModel::clearMensaje)

    var mostrarSheet by remember { mutableStateOf(false) }
    var editIndex by remember { mutableStateOf<Int?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TiendaTopBar(
                title = "Nueva venta",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = viewModel::cerrarForm,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize().imePadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { ClienteMostradorSection(state, viewModel) }
                item {
                    ArticulosSection(
                        state, viewModel,
                        onAddClick = { editIndex = null; mostrarSheet = true },
                        onEditClick = { editIndex = it; mostrarSheet = true },
                    )
                }
                item { PagoSection(state, viewModel) }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
        TiendaSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    if (mostrarSheet) {
        val inicial = editIndex?.let { state.renglones.getOrNull(it) }
        AgregarRenglonSheet(
            esMostrador = state.esMostrador,
            inicial = inicial,
            onDismiss = { mostrarSheet = false },
            onGuardar = { d, c, p, t ->
                val idx = editIndex
                if (idx == null) viewModel.agregarRenglon(d, c, p, t)
                else viewModel.editarRenglon(idx, d, c, p, t)
                mostrarSheet = false
            },
        )
    }
}

/**
 * Edición de los renglones de una venta PENDIENTE (reemplazo total vía PUT).
 * Reutiliza la sección de artículos y el sheet de alta/edición del formulario de venta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditarRenglonesForm(state: VentasUiState, viewModel: VentasViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    ShowUiMessage(state.mensaje, snackbarHostState, viewModel::clearMensaje)

    var mostrarSheet by remember { mutableStateOf(false) }
    var editIndex by remember { mutableStateOf<Int?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TiendaTopBar(
                title = "Editar artículos",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = viewModel::cerrarForm,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize().imePadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    ArticulosSection(
                        state, viewModel,
                        onAddClick = { editIndex = null; mostrarSheet = true },
                        onEditClick = { editIndex = it; mostrarSheet = true },
                    )
                }
                item {
                    SeccionCard("Guardar cambios") {
                        val aviso = when (state.editandoVentaEstado) {
                            EstadoVenta.PENDIENTE ->
                                "Una venta pendiente debe conservar al menos un artículo a crédito. Para devolver todo, cancela la venta."
                            EstadoVenta.CONSOLIDADA ->
                                "Los abonos ya registrados se conservan. Si la edición dejaría saldo negativo en la cuenta, no se guardará."
                            else -> null
                        }
                        aviso?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        state.formError?.let {
                            Spacer(Modifier.height(12.dp))
                            Text(it.asString(), style = MaterialTheme.typography.bodyMedium, color = Danger)
                        }
                        Spacer(Modifier.height(16.dp))
                        BotonPrimario("Guardar cambios", loading = state.saving, onClick = viewModel::guardarRenglones)
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
        TiendaSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    if (mostrarSheet) {
        val inicial = editIndex?.let { state.renglones.getOrNull(it) }
        AgregarRenglonSheet(
            esMostrador = state.esMostrador,
            inicial = inicial,
            onDismiss = { mostrarSheet = false },
            onGuardar = { d, c, p, t ->
                val idx = editIndex
                if (idx == null) viewModel.agregarRenglon(d, c, p, t)
                else viewModel.editarRenglon(idx, d, c, p, t)
                mostrarSheet = false
            },
        )
    }
}

// ── 1. Cliente / Mostrador ──

@Composable
private fun ClienteMostradorSection(state: VentasUiState, viewModel: VentasViewModel) {
    SeccionCard("Cliente") {
        SelectorDoble(
            opcionA = "Cliente", opcionB = "Mostrador",
            seleccionA = !state.esMostrador,
            onA = { viewModel.setMostrador(false) },
            onB = { viewModel.setMostrador(true) },
        )
        Spacer(Modifier.height(12.dp))

        if (state.esMostrador) {
            AppField(state.nombreMostrador, viewModel::onNombreMostradorChange, "Nombre para mostrador")
        } else if (state.clienteSeleccionado == null) {
            AppField(state.clienteQuery, viewModel::onClienteQueryChange, "Buscar cliente por nombre o número")
            if (state.buscandoClientes) {
                Spacer(Modifier.height(8.dp))

                CircularProgressIndicator(Modifier.size(20.dp), color = Primary, strokeWidth = 2.dp)
            }
            state.clientesResultado.forEach { cliente ->
                ClienteResultadoRow(cliente) { viewModel.seleccionarCliente(cliente) }
            }
        } else {
            ClienteSeleccionadoRow(state.clienteSeleccionado, onCambiar = viewModel::deseleccionarCliente)
            Spacer(Modifier.height(12.dp))
            Text("Cuenta", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            if (state.cuentasLoading) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Primary, strokeWidth = 2.dp)
            } else {
                // Cuentas existentes para elegir.
                if (state.cuentasCliente.isEmpty()) {
                    Text("Sin cuentas. Crea una para vender a crédito.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                } else {
                    state.cuentasCliente.forEachIndexed { i, cuenta ->
                        CuentaSelectorRow(
                            cuenta = cuenta,
                            index = i,
                            seleccionada = state.cuentaSeleccionada?.id == cuenta.id,
                            onClick = { viewModel.seleccionarCuenta(cuenta) },
                        )
                    }
                }
                // Crear una cuenta nueva (con nombre), permitido una sola vez.
                if (!state.cuentaCreada) {
                    var nombreCuenta by remember(state.clienteSeleccionado.id) { mutableStateOf("") }
                    Spacer(Modifier.height(12.dp))
                    AppField(nombreCuenta, { nombreCuenta = it }, "Nombre de la nueva cuenta")
                    Spacer(Modifier.height(8.dp))
                    BotonPrimario(
                        "Crear cuenta",
                        loading = false,
                        onClick = { viewModel.abrirCuenta(nombreCuenta.trim().ifBlank { null }) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ClienteResultadoRow(cliente: Cliente, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(cliente.nombreCompleto, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text("Nº ${cliente.numeroCliente}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun ClienteSeleccionadoRow(cliente: Cliente, onCambiar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(cliente.nombreCompleto, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text("Nº ${cliente.numeroCliente}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        TextButton(onClick = onCambiar) { Text("Cambiar", color = Primary) }
    }
}

@Composable
private fun CuentaSelectorRow(cuenta: Cuenta, index: Int, seleccionada: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(TiendaShapes.Field)
            .background(if (seleccionada) PrimarySoft else Surface)
            .border(1.dp, if (seleccionada) Primary else Border, TiendaShapes.Field)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(cuenta.nombre ?: "Cuenta ${index + 1}", style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.weight(1f))
        Text(cuenta.saldo.aMoneda(), style = MaterialTheme.typography.bodyMedium, color = if (cuenta.saldo > BigDecimal.ZERO) Danger else Accent)
    }
}

// ── 2. Artículos ──

@Composable
private fun ArticulosSection(
    state: VentasUiState,
    viewModel: VentasViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Int) -> Unit,
) {
    SeccionCard("Artículos") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${state.renglones.size} artículo(s)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(1f))
            TextButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Añadir", color = Primary)
            }
        }
        if (state.renglones.isEmpty()) {
            Text("Agrega artículos a la venta.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        } else {
            state.renglones.forEachIndexed { i, r ->
                HorizontalDivider(color = Border)
                Row(
                    Modifier.fillMaxWidth().clickable { onEditClick(i) }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(r.descripcion, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        Text(
                            "${r.cantidad} × ${r.precioUnit.aMoneda()} · ${if (r.tipo == TipoRenglon.CREDITO) "Crédito" else "Contado"}",
                            style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                        )
                    }
                    Text(r.importe.aMoneda(), style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(8.dp))
                    Icon(
                        Icons.Filled.Close, contentDescription = "Quitar", tint = TextSecondary,
                        modifier = Modifier.clip(RoundedCornerShape(50)).clickable { viewModel.quitarRenglon(i) }.padding(4.dp),
                    )
                }
            }
        }
    }
}

// ── 3. Detalles de pago ──

@Composable
private fun PagoSection(state: VentasUiState, viewModel: VentasViewModel) {
    val anticipoMonto = state.anticipo.trim().toBigDecimalOrNull() ?: BigDecimal.ZERO
    val saldoConVenta = state.cuentaSeleccionada?.let { it.saldo + state.totalCredito - anticipoMonto }
    val mostrarCredito = !state.esMostrador && state.hayCredito && state.cuentaSeleccionada != null

    SeccionCard("Detalles de pago") {
        Text(tipoVentaLabel(state), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

        if (mostrarCredito) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dejar pendiente", style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.weight(1f))
                Switch(
                    checked = state.pendiente,
                    onCheckedChange = viewModel::onPendienteChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = OnPrimary, checkedTrackColor = Primary),
                )
            }
            if (!state.pendiente) {
                Spacer(Modifier.height(8.dp))
                AppField(state.anticipo, viewModel::onAnticipoChange, "Anticipo (opcional)", keyboardType = KeyboardType.Decimal)
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Border)
        Spacer(Modifier.height(12.dp))

        TotalRow("Subtotal", state.total.aMoneda())
        if (state.totalContado > BigDecimal.ZERO) TotalRow("Contado", state.totalContado.aMoneda())
        if (state.totalCredito > BigDecimal.ZERO) TotalRow("Crédito", state.totalCredito.aMoneda())
        if (anticipoMonto > BigDecimal.ZERO) TotalRow("Anticipo", "- ${anticipoMonto.aMoneda()}", color = Accent)
        if (saldoConVenta != null) {
            Spacer(Modifier.height(4.dp))
            TotalRow("Saldo de la cuenta con la venta", saldoConVenta.aMoneda(), fuerte = true, color = if (saldoConVenta > BigDecimal.ZERO) Danger else Accent)
        }

        state.formError?.let {
            Spacer(Modifier.height(12.dp))
            Text(it.asString(), style = MaterialTheme.typography.bodyMedium, color = Danger)
        }

        Spacer(Modifier.height(16.dp))
        BotonPrimario("Confirmar venta", loading = state.saving, onClick = viewModel::crearVenta)
    }
}

// ── Hoja para agregar artículo ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgregarRenglonSheet(
    esMostrador: Boolean,
    inicial: RenglonVentaInput?,
    onDismiss: () -> Unit,
    onGuardar: (String, Int, String, TipoRenglon) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val esEdicion = inicial != null
    var descripcion by remember { mutableStateOf(inicial?.descripcion ?: "") }
    var cantidad by remember { mutableStateOf(inicial?.cantidad?.toString() ?: "1") }
    var precio by remember { mutableStateOf(inicial?.precioUnit?.toPlainString() ?: "") }
    var tipo by remember { mutableStateOf(inicial?.tipo ?: TipoRenglon.CREDITO) }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Surface) {
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Text(if (esEdicion) "Editar artículo" else "Añadir artículo", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            AppField(descripcion, { descripcion = it }, "Descripción")
            Spacer(Modifier.height(10.dp))
            Row {
                Box(Modifier.weight(1f)) { AppField(cantidad, { cantidad = it }, "Cantidad", keyboardType = KeyboardType.Number) }
                Spacer(Modifier.size(10.dp))
                Box(Modifier.weight(1f)) { AppField(precio, { precio = it }, "Precio unit.", keyboardType = KeyboardType.Decimal) }
            }
            if (!esMostrador) {
                Spacer(Modifier.height(12.dp))
                SelectorDoble(
                    opcionA = "Crédito", opcionB = "Contado",
                    seleccionA = tipo == TipoRenglon.CREDITO,
                    onA = { tipo = TipoRenglon.CREDITO },
                    onB = { tipo = TipoRenglon.CONTADO },
                )
            }
            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Danger)
            }
            Spacer(Modifier.height(16.dp))
            BotonPrimario(if (esEdicion) "Guardar" else "Agregar", loading = false, onClick = {
                val cant = cantidad.trim().toIntOrNull() ?: 0
                val prec = precio.trim().toBigDecimalOrNull()
                if (descripcion.isBlank() || cant < 1 || prec == null || prec <= BigDecimal.ZERO) {
                    error = "Revisa descripción, cantidad y precio"
                } else {
                    onGuardar(descripcion, cant, precio, if (esMostrador) TipoRenglon.CONTADO else tipo)
                }
            })
        }
    }
}

// ── Reutilizables ──

@Composable
private fun SeccionCard(titulo: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().tarjeta().padding(16.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SelectorDoble(opcionA: String, opcionB: String, seleccionA: Boolean, onA: () -> Unit, onB: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Pill(opcionA, seleccionA, onA)
        Pill(opcionB, !seleccionA, onB)
    }
}

@Composable
private fun Pill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Primary else SurfaceMuted)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) OnPrimary else TextSecondary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AppField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
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
private fun TotalRow(label: String, valor: String, fuerte: Boolean = false, color: androidx.compose.ui.graphics.Color = TextPrimary) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(
            label,
            style = if (fuerte) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (fuerte) TextPrimary else TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            valor,
            style = if (fuerte) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = if (fuerte) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun BotonPrimario(text: String, loading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(TiendaShapes.Field)
            .background(Primary)
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(22.dp), color = OnPrimary, strokeWidth = 2.dp)
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge, color = OnPrimary)
        }
    }
}

private fun tipoVentaLabel(state: VentasUiState): String = when {
    state.esMostrador -> "Mostrador (solo contado)"
    state.totalContado > BigDecimal.ZERO && state.totalCredito > BigDecimal.ZERO -> "Venta mixta (contado + crédito)"
    state.totalCredito > BigDecimal.ZERO -> "Venta a crédito"
    else -> "Venta de contado"
}
