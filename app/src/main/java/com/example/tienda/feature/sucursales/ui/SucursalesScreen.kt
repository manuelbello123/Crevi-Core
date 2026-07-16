package com.example.tienda.feature.sucursales.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tienda.feature.sucursales.domain.Sucursal
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
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.Danger
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import com.example.tienda.core.ui.theme.PrimarySoft
import com.example.tienda.core.ui.theme.Surface
import com.example.tienda.core.ui.theme.SurfaceMuted
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SucursalesScreen(viewModel: SucursalesViewModel, onOpenMenu: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    ShowUiMessage(state.mensaje, snackbarHostState, viewModel::clearMensaje)
    ShowUiMessage(if (state.sucursales.isNotEmpty()) state.error else null, snackbarHostState, viewModel::clearError)

    BackHandler(enabled = state.formAbierto) { viewModel.cerrarForm() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TiendaTopBar(title = "Sucursales", onNavigationClick = onOpenMenu)
            TiendaSearchBar(state.query, viewModel::onQueryChange, placeholder = "Buscar sucursal")

            TiendaPullRefresh(
                refreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f),
            ) {
                when {
                    state.isLoading && state.sucursales.isEmpty() ->
                        Centrado { CircularProgressIndicator(color = Primary, strokeWidth = 2.dp) }

                    state.error != null && state.sucursales.isEmpty() ->
                        PantallaError("No se pudieron cargar las sucursales", viewModel::retry)

                    state.sucursalesVisibles.isEmpty() ->
                        PantallaEstadoVacio(if (state.query.isNotBlank()) "Sin resultados" else "No hay sucursales")

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.sucursalesVisibles, key = { it.id }) { s ->
                            SucursalCard(s, onEditar = { viewModel.abrirEditar(s) })
                        }
                    }
                }
            }
        }

        TiendaFab(
            text = "Nueva sucursal",
            onClick = viewModel::abrirNueva,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )

        TiendaSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    if (state.formAbierto) {
        SucursalFormSheet(state = state, viewModel = viewModel)
    }
}

@Composable
private fun SucursalCard(s: Sucursal, onEditar: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().tarjeta().clickable(onClick = onEditar).padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (s.esMatriz) PrimarySoft else SurfaceMuted),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (s.esMatriz) Icons.Filled.Store else Icons.Filled.Storefront,
                    contentDescription = null,
                    tint = if (s.esMatriz) Primary else TextSecondary,
                )
            }
            Spacer(Modifier.weight(1f))
            if (s.esMatriz) {
                Box(Modifier.clip(RoundedCornerShape(999.dp)).background(Accent.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("MATRIZ", style = MaterialTheme.typography.labelMedium, color = Accent, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(s.nombre, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        s.direccion?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(4.dp))
            IconoTexto(Icons.Filled.LocationOn, it)
        }
        s.telefono?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(2.dp))
            IconoTexto(Icons.Filled.Phone, it)
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth().clip(TiendaShapes.Field).border(1.dp, Primary, TiendaShapes.Field)
                .clickable(onClick = onEditar).padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("Editar detalles", style = MaterialTheme.typography.labelLarge, color = Primary)
        }
    }
}

@Composable
private fun IconoTexto(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.size(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SucursalFormSheet(state: SucursalesUiState, viewModel: SucursalesViewModel) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = viewModel::cerrarForm, sheetState = sheetState, containerColor = Surface) {
        Column(Modifier.fillMaxWidth().imePadding().padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Text(
                if (state.esEdicion) "Editar sucursal" else "Nueva sucursal",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))
            Campo(state.nombre, viewModel::onNombreChange, "Nombre")
            Spacer(Modifier.height(10.dp))
            Campo(state.direccion, viewModel::onDireccionChange, "Dirección (opcional)")
            Spacer(Modifier.height(10.dp))
            Campo(state.telefono, viewModel::onTelefonoChange, "Teléfono (opcional)", KeyboardType.Phone)
            state.formError?.let {
                Spacer(Modifier.height(10.dp))
                Text(it.asString(), style = MaterialTheme.typography.bodyMedium, color = Danger)
            }
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.fillMaxWidth().height(52.dp).clip(TiendaShapes.Field).background(Primary)
                    .clickable(enabled = !state.saving, onClick = viewModel::guardar),
                contentAlignment = Alignment.Center,
            ) {
                if (state.saving) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = OnPrimary, strokeWidth = 2.dp)
                } else {
                    Text(if (state.esEdicion) "Guardar cambios" else "Crear sucursal", style = MaterialTheme.typography.labelLarge, color = OnPrimary)
                }
            }
        }
    }
}

@Composable
private fun Campo(value: String, onValueChange: (String) -> Unit, label: String, keyboardType: KeyboardType = KeyboardType.Text) {
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
        colors = campoColores(),
    )
}

@Composable
private fun campoColores() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = Border,
    focusedLabelColor = Primary,
    unfocusedLabelColor = TextSecondary,
    cursorColor = Primary,
    focusedContainerColor = Surface,
    unfocusedContainerColor = Surface,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
)

@Composable
private fun Centrado(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) { content() }
}

