package com.example.tienda.feature.usuarios.ui

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tienda.core.enums.UserRole
import com.example.tienda.feature.usuarios.domain.Usuario
import com.example.tienda.core.ui.components.PantallaError
import com.example.tienda.core.ui.components.PantallaEstadoVacio
import com.example.tienda.core.ui.components.ShowUiMessage
import com.example.tienda.core.ui.components.TiendaFab
import com.example.tienda.core.ui.components.TiendaPullRefresh
import com.example.tienda.core.ui.components.TiendaSearchBar
import com.example.tienda.core.ui.components.TiendaSnackbarHost
import com.example.tienda.core.ui.components.TiendaTopBar
import com.example.tienda.core.ui.components.tarjeta
import com.example.tienda.core.ui.theme.Accent
import com.example.tienda.core.ui.theme.TiendaShapes
import com.example.tienda.core.ui.theme.Background
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.BorderSoft
import com.example.tienda.core.ui.theme.Danger
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import com.example.tienda.core.ui.theme.PrimaryDark
import com.example.tienda.core.ui.theme.PrimarySoft
import com.example.tienda.core.ui.theme.Surface
import com.example.tienda.core.ui.theme.SurfaceMuted
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary

/** Módulo Usuarios (solo admin) — estilo "Reliant Professional" (Stitch). */
@Composable
fun UsuariosScreen(viewModel: UsuariosViewModel, onOpenMenu: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.mode != UsuariosMode.LISTA) { viewModel.cerrarForm() }

    when (state.mode) {
        UsuariosMode.LISTA -> UsuariosLista(
            state = state,
            viewModel = viewModel,
            onOpenMenu = onOpenMenu
        )

        UsuariosMode.NUEVO, UsuariosMode.EDITAR -> UsuarioForm(
            state = state,
            onClose = viewModel::cerrarForm,
            onCrear = viewModel::crear,
            onGuardar = viewModel::guardarEdicion,
        )
    }

    when (val dialogo = state.dialogo) {
        is UsuariosDialog.ReasignarSucursal -> ReasignarSucursalDialog(
            state,
            dialogo.usuario,
            viewModel
        )

        is UsuariosDialog.ResetPassword -> ResetPasswordDialog(state, viewModel)
        is UsuariosDialog.ConfirmarEstado -> ConfirmarEstadoDialog(
            state,
            dialogo.usuario,
            viewModel
        )

        null -> Unit
    }
}

// ── LISTA ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsuariosLista(
    state: UsuariosUiState,
    viewModel: UsuariosViewModel,
    onOpenMenu: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ShowUiMessage(state.mensaje, snackbarHostState, viewModel::clearMensaje)
    ShowUiMessage(
        if (state.usuarios.isNotEmpty()) state.error else null,
        snackbarHostState,
        viewModel::clearError
    )

    var sheetUsuario by remember { mutableStateOf<Usuario?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Box(
        Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(Modifier.fillMaxSize()) {
            TiendaTopBar(title = "Usuarios", onNavigationClick = onOpenMenu)
            TiendaSearchBar(state.query, viewModel::onQueryChange, placeholder = "Buscar por nombre o usuario")

            TiendaPullRefresh(
                refreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f),
            ) {
                when {
                    state.isLoading && state.usuarios.isEmpty() ->
                        Centered { CircularProgressIndicator(color = Primary, strokeWidth = 2.dp) }

                    state.error != null && state.usuarios.isEmpty() ->
                        PantallaError("No se pudieron cargar los usuarios", viewModel::retry)

                    state.usuarios.isEmpty() && state.query.isBlank() ->
                        PantallaEstadoVacio("Aún no hay usuarios", "Crear usuario", viewModel::abrirNuevo)

                    state.usuarios.isEmpty() ->
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
                        items(state.usuarios, key = { it.id }) { usuario ->
                            UsuarioRow(
                                usuario = usuario,
                                sucursalLabel = sucursalLabelDe(state, usuario),
                                onClick = { sheetUsuario = usuario },
                            )
                        }
                    }
                }
            }
        }

        TiendaFab(
            text = "Nuevo usuario",
            onClick = viewModel::abrirNuevo,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )

        TiendaSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    if (sheetUsuario != null) {
        val usuario = sheetUsuario!!
        ModalBottomSheet(
            onDismissRequest = { sheetUsuario = null },
            sheetState = sheetState,
            containerColor = Surface,
        ) {
            UsuarioActionsSheet(
                usuario = usuario,
                esYo = usuario.id == state.miId,
                onEditar = { sheetUsuario = null; viewModel.abrirEditar(usuario) },
                onReasignar = { sheetUsuario = null; viewModel.abrirReasignarSucursal(usuario) },
                onResetPassword = { sheetUsuario = null; viewModel.abrirResetPassword(usuario) },
                onEstado = { sheetUsuario = null; viewModel.abrirConfirmarEstado(usuario) },
            )
        }
    }
}


@Composable
private fun UsuarioRow(usuario: Usuario, sucursalLabel: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().tarjeta().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(PrimarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(iniciales(usuario.nombre), color = Primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f)) {
            // Fila superior: nombre + chip de estado.
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    usuario.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.size(8.dp))
                EstadoChip(activo = usuario.activo)
            }
            Spacer(Modifier.size(2.dp))
            Text("@${usuario.usuario}", style = MaterialTheme.typography.labelMedium, color = TextSecondary)

            Spacer(Modifier.size(8.dp))
            RowMeta(icon = Icons.Filled.LockReset, text = rolLabel(usuario.rol))
            Spacer(Modifier.size(4.dp))
            RowMeta(icon = Icons.Filled.Storefront, text = sucursalLabel)
        }
    }
}

@Composable
private fun RowMeta(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.size(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = TextSecondary, maxLines = 1)
    }
}

@Composable
private fun EstadoChip(activo: Boolean) {
    val text = if (activo) "ACTIVO" else "INACTIVO"
    val color = if (activo) Accent else TextSecondary
    val bg = if (activo) Accent.copy(alpha = 0.10f) else SurfaceMuted
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun UsuarioActionsSheet(
    usuario: Usuario,
    esYo: Boolean,
    onEditar: () -> Unit,
    onReasignar: () -> Unit,
    onResetPassword: () -> Unit,
    onEstado: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 24.dp)
    ) {
        Text(
            usuario.nombre,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
        Text(
            "@${usuario.usuario} · ${rolLabel(usuario.rol)}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        HorizontalDivider(color = BorderSoft)
        SheetAction(Icons.Filled.Edit, "Editar", onEditar)
        SheetAction(Icons.Filled.LockReset, "Restablecer contraseña", onResetPassword)
        if (usuario.rol == UserRole.GERENTE) {
            SheetAction(Icons.Filled.Storefront, "Reasignar sucursal", onReasignar)
        }
        if (!esYo) {
            SheetAction(
                icon = if (usuario.activo) Icons.Filled.Block else Icons.Filled.CheckCircle,
                label = if (usuario.activo) "Desactivar" else "Activar",
                onClick = onEstado,
            )
        }
    }
}

@Composable
private fun SheetAction(icon: ImageVector, label: String, onClick: () -> Unit) {
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

// ── FORMULARIO ──

@Composable
private fun UsuarioForm(
    state: UsuariosUiState,
    onClose: () -> Unit,
    onCrear: (String, String, String, UserRole, Long?) -> Unit,
    onGuardar: (Long, String, String) -> Unit,
) {
    val editando = state.editando
    val isNuevo = state.mode == UsuariosMode.NUEVO

    var nombre by remember(editando) { mutableStateOf(editando?.nombre ?: "") }
    var usuario by remember(editando) { mutableStateOf(editando?.usuario ?: "") }
    var password by remember(editando) { mutableStateOf("") }
    var rol by remember(editando) { mutableStateOf(UserRole.GERENTE) }
    var sucursalId by remember(editando) { mutableStateOf<Long?>(null) }

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
                if (isNuevo) "Nuevo usuario" else "Editar usuario",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }

        Spacer(Modifier.height(20.dp))

        AppTextField(nombre, { nombre = it }, "Nombre *")
        Spacer(Modifier.height(12.dp))
        AppTextField(usuario, { usuario = it }, "Usuario *")

        if (isNuevo) {
            Spacer(Modifier.height(12.dp))
            AppTextField(
                password,
                { password = it },
                "Contraseña *",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            Spacer(Modifier.height(16.dp))
            Text("ROL", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            RolSelector(
                rol = rol,
                onRolChange = { rol = it; if (it == UserRole.ADMINISTRADOR) sucursalId = null })

            if (rol == UserRole.GERENTE) {
                Spacer(Modifier.height(12.dp))
                DropdownField(
                    label = "Sucursal",
                    selectedText = state.sucursales.firstOrNull { it.id == sucursalId }?.nombre,
                    options = state.sucursales.map { it.id to it.nombre },
                    onSelect = { sucursalId = it },
                )
            }
        }

        state.formError?.let {
            Spacer(Modifier.height(12.dp))
            Text(it.asString(), color = Danger, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = if (isNuevo) "Crear usuario" else "Guardar cambios",
            loading = state.saving,
            onClick = {
                if (isNuevo) onCrear(nombre, usuario, password, rol, sucursalId)
                else onGuardar(editando!!.id, nombre, usuario)
            },
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RolSelector(rol: UserRole, onRolChange: (UserRole) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        RolPill(
            "Administrador",
            rol == UserRole.ADMINISTRADOR
        ) { onRolChange(UserRole.ADMINISTRADOR) }
        RolPill("Gerente", rol == UserRole.GERENTE) { onRolChange(UserRole.GERENTE) }
    }
}

@Composable
private fun RolPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Primary else SurfaceMuted)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) OnPrimary else TextSecondary
        )
    }
}

// ── DIÁLOGOS ──

@Composable
private fun ReasignarSucursalDialog(
    state: UsuariosUiState,
    usuario: Usuario,
    viewModel: UsuariosViewModel
) {
    var sucursalId by remember { mutableStateOf(usuario.sucursalId) }
    AlertDialog(
        onDismissRequest = viewModel::cerrarDialogo,
        containerColor = Surface,
        title = {
            Text(
                "Reasignar sucursal",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        },
        text = {
            Column {
                DropdownField(
                    label = "Sucursal",
                    selectedText = state.sucursales.firstOrNull { it.id == sucursalId }?.nombre,
                    options = state.sucursales.map { it.id to it.nombre },
                    onSelect = { sucursalId = it },
                )
                state.formError?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it.asString(), color = Danger, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.reasignarSucursal(sucursalId!!) },
                enabled = sucursalId != null && !state.saving
            ) {
                Text("Reasignar", color = Primary)
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

@Composable
private fun ResetPasswordDialog(state: UsuariosUiState, viewModel: UsuariosViewModel) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = viewModel::cerrarDialogo,
        containerColor = Surface,
        title = {
            Text(
                "Restablecer contraseña",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        },
        text = {
            Column {
                AppTextField(
                    password,
                    { password = it },
                    "Nueva contraseña *",
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )
                state.formError?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it.asString(), color = Danger, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.resetPassword(password) }, enabled = !state.saving) {
                Text("Guardar", color = Primary)
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

@Composable
private fun ConfirmarEstadoDialog(
    state: UsuariosUiState,
    usuario: Usuario,
    viewModel: UsuariosViewModel
) {
    val desactivar = usuario.activo
    AlertDialog(
        onDismissRequest = viewModel::cerrarDialogo,
        containerColor = Surface,
        title = {
            Text(
                if (desactivar) "Desactivar usuario" else "Activar usuario",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        },
        text = {
            Text(
                if (desactivar) "¿Desactivar a ${usuario.nombre}?" else "¿Activar a ${usuario.nombre}?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = viewModel::confirmarEstado, enabled = !state.saving) {
                Text(
                    if (desactivar) "Desactivar" else "Activar",
                    color = if (desactivar) Danger else Primary
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
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next,
            capitalization = if (!isPassword && keyboardType == KeyboardType.Text) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
        ),
        trailingIcon = if (isPassword) {
            {
                Text(
                    if (visible) "Ocultar" else "Ver",
                    color = Primary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .clickable { visible = !visible }
                        .padding(8.dp),
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
                    .padding(horizontal = 16.dp, vertical = 14.dp),
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
            .height(52.dp)
            .clip(TiendaShapes.Field)
            .background(Primary)
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(22.dp), color = OnPrimary, strokeWidth = 2.dp)
        } else {
            Text(text, style = MaterialTheme.typography.titleMedium, color = OnPrimary)
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
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

/** Etiqueta de sucursal: admin = todas; sin sucursal = sin asignar; gerente = su sucursal. */
private fun sucursalLabelDe(state: UsuariosUiState, usuario: Usuario): String = when {
    usuario.rol == UserRole.ADMINISTRADOR -> "Todas las sucursales"
    usuario.sucursalId == null -> "Sin asignar"
    else -> state.sucursales.firstOrNull { it.id == usuario.sucursalId }?.nombre ?: "Sin asignar"
}

private fun rolLabel(rol: UserRole): String = when (rol) {
    UserRole.ADMINISTRADOR -> "Administrador"
    UserRole.GERENTE -> "Gerente"
}

private fun iniciales(nombre: String): String =
    nombre.trim().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
