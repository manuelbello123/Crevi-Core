package com.example.tienda.feature.cobranzas.ui
import com.example.tienda.feature.cobranzas.domain.*
import com.example.tienda.core.util.aMoneda

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tienda.core.ui.components.ShowUiMessage
import com.example.tienda.core.ui.components.TiendaPullRefresh
import com.example.tienda.core.ui.components.PantallaError
import com.example.tienda.core.ui.components.PantallaEstadoVacio
import com.example.tienda.core.ui.components.TiendaSearchBar
import com.example.tienda.core.ui.components.TiendaSnackbarHost
import com.example.tienda.core.ui.components.TiendaTopBar
import com.example.tienda.core.ui.components.tarjeta
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.Danger
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import com.example.tienda.core.ui.theme.Success
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary
import com.example.tienda.core.ui.theme.TiendaShapes
import java.time.LocalDate

/**
 * Cobranza: lista de clientes activos con saldo global + semáforo. Al tocar un
 * cliente se abre el detalle (cuentas, saldo, abono e historial) como sub-pantalla.
 */
@Composable
fun CobranzasScreen(viewModel: CobranzasViewModel, onOpenMenu: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.clienteSeleccionado != null) { viewModel.volverAClientes() }

    if (state.clienteSeleccionado == null) {
        CobranzasLista(state = state, viewModel = viewModel, onOpenMenu = onOpenMenu)
    } else {
        ClienteDetalle(state = state, viewModel = viewModel)
    }
}

@Composable
private fun CobranzasLista(state: CobranzasUiState, viewModel: CobranzasViewModel, onOpenMenu: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    ShowUiMessage(state.mensaje, snackbarHostState, viewModel::clearMensaje)
    ShowUiMessage(if (state.clientes.isNotEmpty()) state.error else null, snackbarHostState, viewModel::clearError)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TiendaTopBar(title = "Cobranzas", onNavigationClick = onOpenMenu)
            TiendaSearchBar(state.query, viewModel::onQueryChange, placeholder = "Buscar por nombre o número")

            TiendaPullRefresh(
                refreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f),
            ) {
                when {
                    state.isLoading && state.clientes.isEmpty() ->
                        Centered { CircularProgressIndicator(color = Primary, strokeWidth = 2.dp) }

                    state.error != null && state.clientes.isEmpty() ->
                        PantallaError("No se pudo cargar la cobranza", viewModel::retry)

                    state.clientes.isEmpty() && state.query.isBlank() ->
                        PantallaEstadoVacio("No hay clientes activos en esta sucursal")

                    state.clientes.isEmpty() ->
                        PantallaEstadoVacio("Sin resultados para \"${state.query}\"", "Limpiar búsqueda", viewModel::limpiarBusqueda)

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.clientes, key = { it.cliente.id }) { item ->
                            CobranzaCard(item, onRegistrar = { viewModel.seleccionarCliente(item.cliente) })
                        }
                    }
                }
            }
        }

        TiendaSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun CobranzaCard(item: ClienteCobranza, onRegistrar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().tarjeta().clickable(onClick = onRegistrar).padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(item.cliente.nombreCompleto, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("Nº ${item.cliente.numeroCliente}", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
            if (item.conDeuda) {
                Spacer(Modifier.size(8.dp))
                BadgeCobranza(item.faltaSemana)
            }
        }

        Spacer(Modifier.height(14.dp))
        Text("Saldo total", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(item.saldoGlobal.aMoneda(), style = MaterialTheme.typography.displaySmall, color = Primary, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = Border)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.DateRange, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(4.dp))
            Text(ultimoPagoLabel(item.ultimoPago), style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.weight(1f))
            RegistrarAbonoBtn(destacado = item.faltaSemana, onClick = onRegistrar)
        }
    }
}

@Composable
private fun BadgeCobranza(falta: Boolean) {
    val (texto, color) = if (falta) "Falta semanal" to Danger else "Al día" to Success
    Box(
        modifier = Modifier.clip(TiendaShapes.Pill).background(color.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(texto.uppercase(), style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RegistrarAbonoBtn(destacado: Boolean, onClick: () -> Unit) {
    val base = Modifier.clip(TiendaShapes.Button).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp)
    if (destacado) {
        Box(Modifier.clip(TiendaShapes.Button).background(Primary).then(base), contentAlignment = Alignment.Center) {
            Text("Registrar abono", style = MaterialTheme.typography.labelLarge, color = OnPrimary, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Box(Modifier.clip(TiendaShapes.Button).border(1.dp, Primary, TiendaShapes.Button).then(base), contentAlignment = Alignment.Center) {
            Text("Registrar abono", style = MaterialTheme.typography.labelLarge, color = Primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

private val LOCALE_ES = java.util.Locale.forLanguageTag("es")
private val DIA_MES = java.time.format.DateTimeFormatter.ofPattern("d MMM", LOCALE_ES)
private fun ultimoPagoLabel(fecha: LocalDate?): String =
    if (fecha == null) "Sin abonos" else "Último: ${fecha.format(DIA_MES)}"

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) { content() }
}

