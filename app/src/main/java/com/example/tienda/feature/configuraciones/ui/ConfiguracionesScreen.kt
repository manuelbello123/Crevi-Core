package com.example.tienda.feature.configuraciones.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tienda.core.ui.components.ShowUiMessage
import com.example.tienda.core.ui.components.TiendaSnackbarHost
import com.example.tienda.core.ui.components.TiendaTopBar
import com.example.tienda.core.ui.components.tarjeta
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.Danger
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import com.example.tienda.core.ui.theme.PrimarySoft
import com.example.tienda.core.ui.theme.Success
import com.example.tienda.core.ui.theme.Surface
import com.example.tienda.core.ui.theme.SurfaceMuted
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary
import com.example.tienda.core.ui.theme.TiendaShapes
import com.example.tienda.feature.configuraciones.data.ModoTema

@Composable
fun ConfiguracionesScreen(
    viewModel: ConfiguracionesViewModel,
    onOpenMenu: () -> Unit,
    onEnableBiometric: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    ShowUiMessage(state.mensaje, snackbarHostState, viewModel::clearMensaje)
    ShowUiMessage(state.error, snackbarHostState, viewModel::clearError)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TiendaTopBar(title = "Configuraciones", onNavigationClick = onOpenMenu)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item { PerfilCard(state.nombreUsuario, state.rol) }

                item {
                    Seccion("Cuenta") {
                        FilaConfig(
                            icon = Icons.Filled.Lock,
                            titulo = "Cambiar contraseña",
                            subtitulo = "Actualiza tu contraseña de acceso",
                            onClick = viewModel::abrirCambiarPass,
                            trailing = FilaTrailing.Chevron,
                        )
                        FilaSwitch(
                            icon = Icons.Filled.Fingerprint,
                            titulo = "Acceso con huella",
                            subtitulo = when {
                                !state.biometricEnrollable && !state.biometricActivo -> "No disponible en este dispositivo"
                                state.biometricActivo -> "Activado — entra más rápido con tu huella"
                                else -> "Activa para entrar más rápido con tu huella"
                            },
                            checked = state.biometricActivo,
                            enabled = state.biometricEnrollable || state.biometricActivo,
                            onCheckedChange = { activar ->
                                if (activar) onEnableBiometric() else viewModel.desactivarBiometrica()
                            },
                        )
                        FilaConfig(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            titulo = "Cerrar sesión",
                            subtitulo = "Salir de esta cuenta",
                            onClick = onLogout,
                            trailing = FilaTrailing.Chevron,
                        )
                    }
                }

                item {
                    Seccion("Apariencia") {
                        FilaSelector(
                            icon = Icons.Filled.Palette,
                            titulo = "Tema",
                            subtitulo = "Elige cómo se ve la app",
                            opciones = listOf(
                                ModoTema.SISTEMA to "Sistema",
                                ModoTema.CLARO to "Claro",
                                ModoTema.OSCURO to "Oscuro",
                            ),
                            seleccionado = state.modoTema,
                            onSelect = viewModel::setModoTema,
                        )
                    }
                }

                item {
                    Seccion("Seguridad") {
                        FilaSwitch(
                            icon = Icons.Filled.Shield,
                            titulo = "Confirmar acciones críticas",
                            subtitulo = "Pedir confirmación al cancelar ventas o eliminar movimientos",
                            checked = state.confirmarAccionesCriticas,
                            onCheckedChange = viewModel::toggleConfirmar,
                        )
                        // Flag global (backend): solo el administrador puede verlo/cambiarlo.
                        if (state.esAdministrador) {
                            FilaSwitch(
                                icon = Icons.Filled.NoAccounts,
                                titulo = "Bloquear login de clientes",
                                subtitulo = "Impide que los clientes entren a la app (afecta a todos)",
                                checked = state.loginClientesDeshabilitado,
                                enabled = !state.loginClientesCargando,
                                onCheckedChange = viewModel::toggleLoginClientes,
                            )
                        }
                    }
                }

                item {
                    Seccion("Datos") {
                        FilaConfig(
                            icon = Icons.Filled.DeleteForever,
                            titulo = "Restablecer aplicación",
                            subtitulo = "Borra sesión, huella y preferencias locales",
                            onClick = viewModel::abrirRestablecer,
                            trailing = FilaTrailing.Chevron,
                            peligro = true,
                        )
                    }
                }

                item {
                    Seccion("Acerca de") {
                        FilaConfig(
                            icon = Icons.Filled.Info,
                            titulo = "Versión de la app",
                            subtitulo = state.versionApp,
                            onClick = null,
                        )
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }

        TiendaSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    if (state.cambiarPassAbierto) {
        CambiarPasswordSheet(state = state, viewModel = viewModel)
    }
    if (state.confirmarRestablecerAbierto) {
        RestablecerDialog(onConfirmar = viewModel::restablecerDatos, onCancelar = viewModel::cerrarRestablecer)
    }
}

// ── Perfil (header) ──

@Composable
private fun PerfilCard(nombre: String, rol: String) {
    Row(Modifier.fillMaxWidth().tarjeta().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(PrimarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f)) {
            Text(nombre.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            if (rol.isNotBlank()) {
                Text(rol, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }
    }
}

// ── Sección + filas ──

@Composable
private fun Seccion(titulo: String, content: @Composable () -> Unit) {
    Column {
        Text(
            titulo.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Column(Modifier.fillMaxWidth().tarjeta()) {
            content()
        }
    }
}

private sealed interface FilaTrailing {
    data object Chevron : FilaTrailing
}

@Composable
private fun FilaConfig(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: (() -> Unit)?,
    trailing: FilaTrailing? = null,
    peligro: Boolean = false,
) {
    val tint = if (peligro) Danger else Primary
    val textoColor = if (peligro) Danger else TextPrimary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(tint.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = textoColor)
            Text(subtitulo, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
        when (trailing) {
            FilaTrailing.Chevron -> Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
            null -> Unit
        }
    }
}

@Composable
private fun FilaSwitch(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val alpha = if (enabled) 1f else 0.5f
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Primary.copy(alpha = 0.10f * alpha)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Primary.copy(alpha = alpha), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = TextPrimary.copy(alpha = alpha))
            Text(subtitulo, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = OnPrimary, checkedTrackColor = Primary),
        )
    }
}

@Composable
private fun <T> FilaSelector(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    opciones: List<Pair<T, String>>,
    seleccionado: T,
    onSelect: (T) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Primary.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(subtitulo, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }
        Spacer(Modifier.size(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(TiendaShapes.Field).background(SurfaceMuted).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            opciones.forEach { (valor, etiqueta) ->
                val activo = valor == seleccionado
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activo) Surface else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { onSelect(valor) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        etiqueta,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (activo) Primary else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ── Cambio de contraseña (sheet) ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CambiarPasswordSheet(state: ConfiguracionesUiState, viewModel: ConfiguracionesViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = viewModel::cerrarCambiarPass, sheetState = sheetState, containerColor = Surface) {
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Text("Cambiar contraseña", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("Ingresa tu contraseña actual y define la nueva.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(16.dp))

            PassField(state.passwordActual, viewModel::onPassActualChange, "Contraseña actual")
            Spacer(Modifier.height(10.dp))
            PassField(state.passwordNueva, viewModel::onPassNuevaChange, "Nueva contraseña (mín. 8)")
            Spacer(Modifier.height(10.dp))
            PassField(state.passwordConfirmar, viewModel::onPassConfirmarChange, "Confirmar nueva contraseña")

            state.cambiarPassError?.let {
                Spacer(Modifier.height(10.dp))
                Text(it.asString(), style = MaterialTheme.typography.bodyMedium, color = Danger)
            }

            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(TiendaShapes.Button)
                    .background(if (state.cambiandoPass) SurfaceMuted else Primary)
                    .clickable(enabled = !state.cambiandoPass, onClick = viewModel::guardarNuevaPass),
                contentAlignment = Alignment.Center,
            ) {
                if (state.cambiandoPass) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = Primary, strokeWidth = 2.dp)
                } else {
                    Text("Guardar", style = MaterialTheme.typography.labelLarge, color = OnPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PassField(value: String, onValueChange: (String) -> Unit, label: String) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            Icon(
                if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                contentDescription = if (visible) "Ocultar" else "Mostrar",
                tint = TextSecondary,
                modifier = Modifier.clip(CircleShape).clickable { visible = !visible }.padding(4.dp),
            )
        },
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

// ── Diálogo: restablecer ──

@Composable
private fun RestablecerDialog(onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Restablecer aplicación", color = TextPrimary) },
        text = {
            Text(
                "Se borrarán la sesión, la huella guardada y las preferencias locales. Necesitarás iniciar sesión de nuevo. Los datos del servidor no se tocan.",
                color = TextSecondary,
            )
        },
        confirmButton = { TextButton(onClick = onConfirmar) { Text("Restablecer", color = Danger, fontWeight = FontWeight.SemiBold) } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar", color = Primary) } },
        containerColor = Surface,
        shape = TiendaShapes.Dialog,
    )
}
