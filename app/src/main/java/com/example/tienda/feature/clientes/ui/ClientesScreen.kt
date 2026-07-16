package com.example.tienda.feature.clientes.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tienda.feature.clientes.domain.Cliente
import com.example.tienda.core.ui.components.PantallaError
import com.example.tienda.core.ui.components.PantallaEstadoVacio
import com.example.tienda.core.ui.components.ShowUiMessage
import com.example.tienda.core.ui.components.TiendaFab
import com.example.tienda.core.ui.components.TiendaPullRefresh
import com.example.tienda.core.ui.components.TiendaSearchBar
import com.example.tienda.core.ui.components.TiendaSnackbarHost
import com.example.tienda.core.ui.components.TiendaTopBar
import com.example.tienda.core.ui.components.tarjeta
import com.example.tienda.core.ui.theme.Background
import com.example.tienda.core.ui.theme.TiendaShapes
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import com.example.tienda.core.ui.theme.PrimarySoft
import com.example.tienda.core.ui.theme.Success
import com.example.tienda.core.ui.theme.Surface
import com.example.tienda.core.ui.theme.SurfaceMuted
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuItemColors
import androidx.compose.ui.graphics.Color

/** Punto de entrada del módulo Clientes (dueño del estado). */
@Composable
fun ClientesScreen(viewModel: ClientesViewModel, onOpenMenu: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.mode != ClientesMode.LISTA) { viewModel.cerrarForm() }

    when (state.mode) {
        ClientesMode.LISTA -> ClientesLista(
            state = state,
            viewModel = viewModel,
            onOpenMenu = onOpenMenu
        )

        ClientesMode.NUEVO, ClientesMode.EDITAR -> ClienteForm(
            state = state,
            onClose = viewModel::cerrarForm,
            onCrear = viewModel::crear,
            onGuardar = viewModel::guardarEdicion,
            onFormErrorShown = {},
        )
    }

    when (val dialogo = state.dialogo) {
        is ClientesDialog.Reasignar -> ReasignarDialog(state, dialogo.cliente, viewModel)
        is ClientesDialog.AccesoApp -> AccesoAppDialog(state, dialogo.cliente, viewModel)
        is ClientesDialog.ConfirmarEstado -> ConfirmarEstadoDialog(
            state,
            dialogo.cliente,
            viewModel
        )

        null -> Unit
    }
}

// ── LISTA ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientesLista(
    state: ClientesUiState,
    viewModel: ClientesViewModel,
    onOpenMenu: () -> Unit
) {
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    ShowUiMessage(state.mensaje, snackbarHostState, viewModel::clearMensaje)
    // El error de carga con lista vacía se muestra centrado; si hay lista, por snackbar.
    ShowUiMessage(
        if (state.clientes.isNotEmpty()) state.error else null,
        snackbarHostState,
        viewModel::clearError
    )

    var sheetCliente by remember { mutableStateOf<Cliente?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TiendaTopBar(title = "Clientes", onNavigationClick = onOpenMenu)
            TiendaSearchBar(
                state.query,
                viewModel::onQueryChange,
                placeholder = "Buscar por nombre o número"
            )

            TiendaPullRefresh(
                refreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f),
            ) {
                when {
                    state.isLoading && state.clientes.isEmpty() ->
                        Centered { CircularProgressIndicator(color = Primary, strokeWidth = 2.dp) }

                    state.error != null && state.clientes.isEmpty() ->
                        PantallaError("No se pudieron cargar los clientes", viewModel::retry)

                    state.clientes.isEmpty() && state.query.isBlank() ->
                        PantallaEstadoVacio(
                            "Aún no hay clientes",
                            "Crear cliente",
                            viewModel::abrirNuevo
                        )

                    state.clientes.isEmpty() ->
                        PantallaEstadoVacio(
                            "Sin resultados para \"${state.query}\"",
                            "Limpiar búsqueda",
                            viewModel::limpiarBusqueda,
                        )

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.clientes, key = { it.id }) { cliente ->
                            ClienteRow(
                                cliente = cliente,
                                sucursalNombre = sucursalNombre(state, cliente.sucursalId),
                                onClick = { sheetCliente = cliente },
                            )
                        }
                    }
                }
            }
        }

        TiendaFab(
            text = "Nuevo cliente",
            onClick = viewModel::abrirNuevo,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )

        TiendaSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    if (sheetCliente != null) {
        val cliente = sheetCliente!!
        ModalBottomSheet(
            onDismissRequest = { sheetCliente = null },
            sheetState = sheetState,
            containerColor = Surface,
        ) {
            ClienteActionsSheet(
                cliente = cliente,
                esAdministrador = state.esAdministrador,
                onEditar = { sheetCliente = null; viewModel.abrirEditar(cliente) },
                onReasignar = { sheetCliente = null; viewModel.abrirReasignar(cliente) },
                onAccesoApp = { sheetCliente = null; viewModel.abrirAccesoApp(cliente) },
                onEstado = { sheetCliente = null; viewModel.abrirConfirmarEstado(cliente) },
            )
        }
    }
}


@Composable
private fun ClienteRow(cliente: Cliente, sucursalNombre: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .tarjeta()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(PrimarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                iniciales(cliente.nombreCompleto),
                color = Primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f)) {
            // Fila superior: nombre + chip de estado alineados por baseline visual.
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    cliente.nombreCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.size(8.dp))
                EstadoChip(activo = cliente.activo)
            }
            Spacer(Modifier.size(2.dp))
            Text(
                "Nº ${cliente.numeroCliente}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            // Metadatos con iconos: teléfono y sucursal (si aplican).
            if (!cliente.telefono.isNullOrBlank()) {
                Spacer(Modifier.size(8.dp))
                RowMeta(icon = Icons.Filled.PhoneAndroid, text = cliente.telefono!!)
            }
            if (!sucursalNombre.isNullOrBlank()) {
                Spacer(Modifier.size(4.dp))
                RowMeta(icon = Icons.Filled.Storefront, text = sucursalNombre)
            }
            if (!cliente.direccion.isNullOrBlank()) {
                Spacer(Modifier.size(8.dp))
                RowMeta(icon = Icons.Filled.LocationOn, text = cliente.direccion!!)
            }
        }
    }
}

@Composable
private fun RowMeta(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.size(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun EstadoChip(activo: Boolean) {
    val color = if (activo) Success else TextSecondary
    val bg = if (activo) Success.copy(alpha = 0.12f) else SurfaceMuted
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (activo) Icons.Filled.CheckCircle else Icons.Filled.Block,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.size(4.dp))
        Text(
            if (activo) "ACTIVO" else "INACTIVO",
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
private fun ClienteActionsSheet(
    cliente: Cliente,
    esAdministrador: Boolean,
    onEditar: () -> Unit,
    onReasignar: () -> Unit,
    onAccesoApp: () -> Unit,
    onEstado: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 24.dp)
    ) {
        Text(
            cliente.nombreCompleto,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp),
        )
        Text(
            "Nº ${cliente.numeroCliente}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )
        HorizontalDivider(color = Border)
        SheetAction(Icons.Filled.Edit, "Editar", onEditar)
        SheetAction(
            icon = if (cliente.activo) Icons.Filled.Block else Icons.Filled.CheckCircle,
            label = if (cliente.activo) "Desactivar" else "Activar",
            onClick = onEstado,
        )
        SheetAction(Icons.Filled.PhoneAndroid, "Acceso a la app", onAccesoApp)
        if (esAdministrador) {
            SheetAction(Icons.Filled.SwapHoriz, "Reasignar sucursal/gerente", onReasignar)
        }
    }
}

@Composable
private fun SheetAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Primary)
        Spacer(Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
    }
}

// ── FORMULARIO (NUEVO / EDITAR) ──

@Composable
private fun ClienteForm(
    state: ClientesUiState,
    onClose: () -> Unit,
    onCrear: (String, String?, String?, Long?, Long?, Boolean, String?) -> Unit,
    onGuardar: (Long, String, String?, String?) -> Unit,
    onFormErrorShown: () -> Unit,
) {
    val editando = state.editando
    val isNuevo = state.mode == ClientesMode.NUEVO

    var nombre by remember(editando) { mutableStateOf(editando?.nombreCompleto ?: "") }
    var telefono by remember(editando) { mutableStateOf(editando?.telefono ?: "") }
    var direccion by remember(editando) { mutableStateOf(editando?.direccion ?: "") }
    var sucursalId by remember(editando) { mutableStateOf(editando?.sucursalId) }
    var gerenteId by remember(editando) { mutableStateOf<Long?>(null) }
    var accesoApp by remember(editando) { mutableStateOf(false) }
    var password by remember(editando) { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Cerrar",
                tint = TextPrimary,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onClose)
                    .padding(4.dp),
            )
            Spacer(Modifier.size(12.dp))
            Text(
                if (isNuevo) "Nuevo cliente" else "Editar cliente",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        }

        Spacer(Modifier.height(20.dp))

        AppTextField(nombre, { nombre = it }, "Nombre completo *", mayusculasCompletas = true)
        Spacer(Modifier.height(12.dp))
        AppTextField(telefono, { telefono = it }, "Teléfono", keyboardType = KeyboardType.Phone)
        Spacer(Modifier.height(12.dp))
        AppTextField(direccion, { direccion = it }, "Dirección")

        if (isNuevo && state.esAdministrador) {
            Spacer(Modifier.height(12.dp))
            DropdownField(
                label = "Sucursal",
                selectedText = state.sucursales.firstOrNull { it.id == sucursalId }?.nombre,
                options = state.sucursales.map { it.id to it.nombre },
                onSelect = { sucursalId = it; gerenteId = null },
            )
            Spacer(Modifier.height(12.dp))
            DropdownField(
                label = "Gerente responsable",
                selectedText = state.operadores.firstOrNull { it.id == gerenteId }?.nombre,
                options = state.operadoresDeSucursal(sucursalId).map { it.id to it.nombre },
                onSelect = { gerenteId = it },
            )
        }

        if (isNuevo) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Acceso a la app",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = accesoApp,
                    onCheckedChange = { accesoApp = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnPrimary,
                        checkedTrackColor = Primary
                    ),
                )
            }
            if (accesoApp) {
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    password,
                    { password = it },
                    "Contraseña *",
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )
            }
        }

        state.formError?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                it.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = if (isNuevo) "Crear cliente" else "Guardar cambios",
            loading = state.saving,
            onClick = {
                if (isNuevo) {
                    onCrear(nombre, direccion, telefono, sucursalId, gerenteId, accesoApp, password)
                } else {
                    onGuardar(editando!!.id, nombre, direccion, telefono)
                }
            },
        )
        Spacer(Modifier.height(24.dp))
    }
}

// ── DIÁLOGOS ──

@Composable
private fun ReasignarDialog(
    state: ClientesUiState,
    cliente: Cliente,
    viewModel: ClientesViewModel
) {
    var sucursalId by remember { mutableStateOf(cliente.sucursalId) }
    var gerenteId by remember { mutableStateOf(cliente.gerenteId) }

    AlertDialog(
        onDismissRequest = viewModel::cerrarDialogo,
        containerColor = Surface,
        title = { Text("Reasignar cliente", color = TextPrimary) },
        text = {
            Column {
                DropdownField(
                    label = "Sucursal",
                    selectedText = state.sucursales.firstOrNull { it.id == sucursalId }?.nombre,
                    options = state.sucursales.map { it.id to it.nombre },
                    onSelect = { sucursalId = it; gerenteId = null },
                )
                Spacer(Modifier.height(12.dp))
                DropdownField(
                    label = "Gerente responsable",
                    selectedText = state.operadores.firstOrNull { it.id == gerenteId }?.nombre,
                    options = state.operadoresDeSucursal(sucursalId).map { it.id to it.nombre },
                    onSelect = { gerenteId = it },
                )
                state.formError?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        it.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.reasignar(sucursalId!!, gerenteId!!) },
                enabled = sucursalId != null && gerenteId != null && !state.saving,
            ) { Text("Reasignar", color = Primary) }
        },
        dismissButton = {
            TextButton(onClick = viewModel::cerrarDialogo) {
                Text(
                    "Cancelar",
                    color = TextSecondary
                )
            }
        },
    )
}

@Composable
private fun AccesoAppDialog(
    state: ClientesUiState,
    cliente: Cliente,
    viewModel: ClientesViewModel
) {
    var accesoApp by remember { mutableStateOf(cliente.accesoApp) }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = viewModel::cerrarDialogo,
        containerColor = Surface,
        title = { Text("Acceso a la app", color = TextPrimary) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Habilitar acceso",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = accesoApp,
                        onCheckedChange = { accesoApp = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnPrimary,
                            checkedTrackColor = Primary
                        ),
                    )
                }
                if (accesoApp) {
                    Spacer(Modifier.height(12.dp))
                    AppTextField(
                        password,
                        { password = it },
                        "Contraseña *",
                        keyboardType = KeyboardType.Password,
                        isPassword = true
                    )
                }
                state.formError?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        it.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.cambiarAccesoApp(accesoApp, password.ifBlank { null }) },
                enabled = !state.saving,
            ) { Text("Guardar", color = Primary) }
        },
        dismissButton = {
            TextButton(onClick = viewModel::cerrarDialogo) {
                Text(
                    "Cancelar",
                    color = TextSecondary
                )
            }
        },
    )
}

@Composable
private fun ConfirmarEstadoDialog(
    state: ClientesUiState,
    cliente: Cliente,
    viewModel: ClientesViewModel
) {
    val desactivar = cliente.activo
    AlertDialog(
        onDismissRequest = viewModel::cerrarDialogo,
        containerColor = Surface,
        title = {
            Text(
                if (desactivar) "Desactivar cliente" else "Activar cliente",
                color = TextPrimary
            )
        },
        text = {
            Text(
                if (desactivar) "¿Desactivar a ${cliente.nombreCompleto}?"
                else "¿Activar a ${cliente.nombreCompleto}?",
                color = TextSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = viewModel::confirmarEstado, enabled = !state.saving) {
                Text(
                    if (desactivar) "Desactivar" else "Activar",
                    color = if (desactivar) MaterialTheme.colorScheme.error else Primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::cerrarDialogo) {
                Text(
                    "Cancelar",
                    color = TextSecondary
                )
            }
        },
    )
}

// ── Reutilizables ──

@Composable
private fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    // Solo el campo "Nombre completo" lo activa: permite escribir todo en mayúsculas
    // (el teclado ofrece cada letra en mayúscula, no solo la primera de la frase).
    mayusculasCompletas: Boolean = false,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next,
            capitalization = when {
                isPassword || keyboardType != KeyboardType.Text -> androidx.compose.ui.text.input.KeyboardCapitalization.None
                mayusculasCompletas -> androidx.compose.ui.text.input.KeyboardCapitalization.Characters
                else -> androidx.compose.ui.text.input.KeyboardCapitalization.Sentences
            },
        ),
        trailingIcon = if (isPassword) {
            {
                val txt = if (visible) "Ocultar" else "Ver"
                Text(
                    txt,
                    color = Primary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .clickable { visible = !visible }
                        .padding(8.dp)
                )
            }
        } else null,
        shape = TiendaShapes.Field,
        colors = fieldColors(),
    )
}

@Composable
private fun DropdownField(
    label: String,
    selectedText: String?,
    options: List<Pair<Long, String>>,
    onSelect: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(TiendaShapes.Field)
                    .background(Surface)
                    .border(1.dp, Border, TiendaShapes.Field)
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    selectedText ?: "Selecciona",
                    color = if (selectedText != null) TextPrimary else TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextSecondary)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (options.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Sin opciones") },
                        onClick = { expanded = false },
                        enabled = false
                    )
                }
                options.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = { onSelect(id); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
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

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = Border,
    focusedLeadingIconColor = Primary,
    unfocusedLeadingIconColor = TextSecondary,
    focusedLabelColor = Primary,
    unfocusedLabelColor = TextSecondary,
    cursorColor = Primary,
    focusedContainerColor = Surface,
    unfocusedContainerColor = Surface,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
)

private fun sucursalNombre(state: ClientesUiState, sucursalId: Long?): String? =
    if (!state.esAdministrador || sucursalId == null) null
    else state.sucursales.firstOrNull { it.id == sucursalId }?.nombre

private fun iniciales(nombre: String): String =
    nombre.trim().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
