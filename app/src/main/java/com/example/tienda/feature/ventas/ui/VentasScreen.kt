package com.example.tienda.feature.ventas.ui
import com.example.tienda.feature.ventas.domain.*

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tienda.core.util.aMoneda
import com.example.tienda.core.ui.components.DayHeader
import com.example.tienda.core.ui.components.ShowUiMessage
import com.example.tienda.core.ui.components.TiendaFab
import com.example.tienda.core.ui.components.TiendaPullRefresh
import com.example.tienda.core.ui.components.PantallaError
import com.example.tienda.core.ui.components.PantallaEstadoVacio
import com.example.tienda.core.ui.components.TiendaSearchBar
import com.example.tienda.core.ui.components.TiendaSnackbarHost
import com.example.tienda.core.ui.components.TiendaTopBar
import com.example.tienda.core.ui.components.tarjeta
import com.example.tienda.core.ui.theme.Accent
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.Danger
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import com.example.tienda.core.ui.theme.PrimarySoft
import com.example.tienda.core.ui.theme.Surface
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary
import com.example.tienda.core.ui.theme.Warning

@Composable
fun VentasScreen(viewModel: VentasViewModel, onOpenMenu: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.mode != VentasMode.LISTA) { viewModel.cerrarForm() }

    when (state.mode) {
        VentasMode.LISTA -> VentasLista(state = state, viewModel = viewModel, onOpenMenu = onOpenMenu)
        VentasMode.NUEVA -> VentaForm(state = state, viewModel = viewModel)
        VentasMode.EDITAR -> EditarRenglonesForm(state = state, viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VentasLista(state: VentasUiState, viewModel: VentasViewModel, onOpenMenu: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    ShowUiMessage(state.mensaje, snackbarHostState, viewModel::clearMensaje)
    // Solo muestra el error por snackbar si ya hay lista visible; si está vacía, la pantalla de error se encarga.
    ShowUiMessage(if (state.ventas.isNotEmpty()) state.error else null, snackbarHostState, viewModel::clearError)

    var sheetVentaId by remember { mutableStateOf<Long?>(null) }
    var sheetEsPendiente by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val verPendientes = state.filtroEstado == EstadoVenta.PENDIENTE

    val visibles = state.ventasVisibles
    val pendientesVisibles = state.pendientes.filter { p ->
        state.query.isBlank() || p.nombreCliente?.contains(state.query.trim(), ignoreCase = true) == true
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TiendaTopBar(title = "Ventas", onNavigationClick = onOpenMenu)
            TiendaSearchBar(state.query, viewModel::onQueryChange, placeholder = "Buscar por cliente o mostrador")
            FiltroChips(state.filtroEstado, viewModel::onFiltroEstado)
            // La semana y el tipo solo aplican a la lista fechada (no al worklist de pendientes).
            if (!verPendientes) {
                NavegadorSemana(
                    inicio = state.semanaInicio,
                    fin = state.semanaFin,
                    esSemanaActual = state.esSemanaActual,
                    onAnterior = viewModel::semanaAnterior,
                    onSiguiente = viewModel::semanaSiguiente,
                )
                TipoChips(state.filtroTipo, viewModel::onFiltroTipo)
            }

            TiendaPullRefresh(
                refreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f),
            ) {
                when {
                    state.isLoading && state.ventas.isEmpty() && state.pendientes.isEmpty() ->
                        Centered { CircularProgressIndicator(color = Primary, strokeWidth = 2.dp) }

                    state.error != null && state.ventas.isEmpty() ->
                        PantallaError("No se pudieron cargar las ventas", viewModel::retry)

                    verPendientes && pendientesVisibles.isEmpty() ->
                        PantallaEstadoVacio(if (state.query.isNotBlank()) "Sin resultados" else "No hay ventas pendientes")

                    !verPendientes && visibles.isEmpty() ->
                        PantallaEstadoVacio(if (state.ventas.isNotEmpty()) "Sin resultados" else "No hay ventas esta semana")

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        // Bottom extra para que el FAB "Nueva venta" no tape la última card.
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (verPendientes) {
                            items(pendientesVisibles, key = { it.ventaId }) { p ->
                                VentaPendienteCard(p, onClick = { sheetVentaId = p.ventaId; sheetEsPendiente = true })
                            }
                        } else {
                            // Agrupado por día; más reciente primero (domingo→lunes). No se listan
                            // días futuros: van apareciendo conforme avanza la semana.
                            val porDia = visibles.groupBy { it.fechaCompra }
                            val hoy = java.time.LocalDate.now()
                            for (offset in 6 downTo 0) {
                                val dia = state.semanaInicio.plusDays(offset.toLong())
                                if (dia.isAfter(hoy)) continue
                                val delDia = porDia[dia.toString()].orEmpty()
                                item(key = "h-$dia") { DayHeader(dia) }
                                if (delDia.isEmpty()) {
                                    item(key = "e-$dia") {
                                        Text(
                                            "Sin ventas",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(start = 4.dp),
                                        )
                                    }
                                } else {
                                    items(delDia, key = { it.id }) { v ->
                                        VentaResumenCard(v, onClick = { sheetVentaId = v.id; sheetEsPendiente = v.estado == EstadoVenta.PENDIENTE })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        TiendaFab(
            text = "Nueva venta",
            onClick = viewModel::abrirNueva,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )

        TiendaSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    val ventaId = sheetVentaId
    if (ventaId != null) {
        LaunchedEffect(ventaId) { viewModel.cargarDetalle(ventaId) }
        ModalBottomSheet(
            onDismissRequest = { sheetVentaId = null; viewModel.cerrarDetalle() },
            sheetState = sheetState,
            containerColor = Surface,
        ) {
            val detalle = state.ventaDetalle
            val estaCancelada = detalle?.estado == EstadoVenta.CANCELADA
            Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Text("Detalle de la venta", style = MaterialTheme.typography.titleMedium, color = TextPrimary, modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp))
                DetalleVenta(detalle, state.detalleLoading)
                HorizontalDivider(color = Border)
                // Editar artículos aplica a pendientes y consolidadas (backend valida saldo negativo).
                if (!estaCancelada) {
                    SheetItem("Editar artículos", habilitado = detalle != null) {
                        if (detalle != null) { sheetVentaId = null; viewModel.cerrarDetalle(); viewModel.abrirEditarRenglones(detalle) }
                    }
                }
                if (sheetEsPendiente) {
                    SheetItem("Consolidar") { sheetVentaId = null; viewModel.cerrarDetalle(); viewModel.consolidar(ventaId) }
                }
                if (!estaCancelada) {
                    SheetItem("Cancelar venta", peligro = true) { sheetVentaId = null; viewModel.cerrarDetalle(); viewModel.cancelar(ventaId) }
                }
            }
        }
    }
}

@Composable
private fun DetalleVenta(venta: Venta?, loading: Boolean) {
    when {
        loading && venta == null ->
            Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            }

        venta != null -> Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp)) {
            registroLabel(venta.registradaPorNombre).takeIf { it.isNotEmpty() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
            }
            if (venta.renglones.isEmpty()) {
                Text("Sin renglones", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            } else {
                venta.renglones.forEach { r -> RenglonRow(r) }
            }
        }
    }
}

@Composable
private fun RenglonRow(r: RenglonVenta) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(r.descripcion, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text(
                "${r.cantidad} × ${r.precioUnit.aMoneda()} · ${if (r.tipo == TipoRenglon.CREDITO) "Crédito" else "Contado"}",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary,
            )
        }
        Text(r.importe.aMoneda(), style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
private fun NavegadorSemana(
    inicio: java.time.LocalDate,
    fin: java.time.LocalDate,
    esSemanaActual: Boolean,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onAnterior) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Semana anterior", tint = Primary)
        }
        Text(
            rangoSemanaLabel(inicio, fin),
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onSiguiente, enabled = !esSemanaActual) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Semana siguiente",
                tint = if (esSemanaActual) Border else Primary,
            )
        }
    }
}

@Composable
private fun TipoChips(seleccionado: FiltroTipo, onSelect: (FiltroTipo) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Chip("Todos", seleccionado == FiltroTipo.TODOS) { onSelect(FiltroTipo.TODOS) }
        Chip("Mostrador", seleccionado == FiltroTipo.MOSTRADOR) { onSelect(FiltroTipo.MOSTRADOR) }
        Chip("Contado", seleccionado == FiltroTipo.CONTADO) { onSelect(FiltroTipo.CONTADO) }
        Chip("Crédito", seleccionado == FiltroTipo.CREDITO) { onSelect(FiltroTipo.CREDITO) }
        Chip("Mixto", seleccionado == FiltroTipo.MIXTO) { onSelect(FiltroTipo.MIXTO) }
    }
}

private val LOCALE_ES = java.util.Locale.forLanguageTag("es")
private fun rangoSemanaLabel(inicio: java.time.LocalDate, fin: java.time.LocalDate): String =
    "${fechaLarga(inicio)} ${inicio.year} - ${fechaLarga(fin)} ${fin.year}"

/** "22 de Junio" (mes con mayúscula inicial). */
private fun fechaLarga(d: java.time.LocalDate): String {
    val mes = d.month.getDisplayName(java.time.format.TextStyle.FULL, LOCALE_ES)
        .replaceFirstChar { it.uppercase() }
    return "${d.dayOfMonth} de $mes"
}

@Composable
private fun FiltroChips(seleccionado: EstadoVenta?, onSelect: (EstadoVenta?) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Chip("Todas", seleccionado == null) { onSelect(null) }
        Chip("Pendientes", seleccionado == EstadoVenta.PENDIENTE) { onSelect(EstadoVenta.PENDIENTE) }
        Chip("Consolidadas", seleccionado == EstadoVenta.CONSOLIDADA) { onSelect(EstadoVenta.CONSOLIDADA) }
        Chip("Canceladas", seleccionado == EstadoVenta.CANCELADA) { onSelect(EstadoVenta.CANCELADA) }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimarySoft,
            selectedLabelColor = Primary,
        ),
    )
}

@Composable
private fun VentaResumenCard(venta: VentaResumen, onClick: () -> Unit) {
    val (icono, iconColor) = iconoYColorVenta(venta.estado, venta.tipo, venta.cuentaId)
    Row(
        modifier = Modifier.fillMaxWidth().tarjeta().clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icono, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                tituloVenta(venta.nombreMostrador, venta.nombreCliente, venta.cuentaId),
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                metaVenta(venta),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                maxLines = 2,
            )
        }
        Spacer(Modifier.size(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(venta.total.aMoneda(), style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.SemiBold)
            // Badge de estado siempre visible (consolidada, pendiente, cancelada).
            Spacer(Modifier.height(4.dp))
            EstadoChipVenta(venta.estado)
        }
    }
}

@Composable
private fun VentaPendienteCard(p: VentaPendiente, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().tarjeta().clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(Warning.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.PointOfSale, contentDescription = null, tint = Warning, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                tituloVenta(null, p.nombreCliente, p.cuentaId),
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                "Vence el ${p.venceEl}",
                style = MaterialTheme.typography.labelMedium,
                color = Warning,
                maxLines = 1,
            )
        }
        Spacer(Modifier.size(8.dp))
        Text(p.total.aMoneda(), style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.SemiBold)
    }
}

/** Meta compacta para la card:
 * Tipo · Cuenta
 * Hora · Registró
 */
private fun metaVenta(v: VentaResumen): String {
    val linea1 = buildList {
        add(tipoLabel(v))
        v.nombreCuenta
            ?.takeIf { it.isNotBlank() }
            ?.let { add(it) }
    }.joinToString(" · ")

    val linea2 = buildList {
        add(v.horaCompra.take(5))
        v.registradaPorNombre
            ?.takeIf { it.isNotBlank() }
            ?.let { add(it) }
    }.joinToString(" · ")

    return "$linea1\n$linea2"
}

/** Etiqueta del tipo de venta para mostrar en la meta. */
private fun tipoLabel(v: VentaResumen): String = when {
    v.cuentaId == null -> "Mostrador"
    v.tipo == "credito" -> "Crédito"
    v.tipo == "mixto" -> "Mixta"
    else -> "Contado"
}

/** Icono y color según el estado y tipo de la venta. Coherente con Actividad reciente. */
private fun iconoYColorVenta(estado: EstadoVenta, tipo: String?, cuentaId: Long?): Pair<ImageVector, androidx.compose.ui.graphics.Color> {
    if (estado == EstadoVenta.CANCELADA) return Icons.Filled.PointOfSale to Danger
    if (estado == EstadoVenta.PENDIENTE) return Icons.Filled.PointOfSale to Warning
    return when {
        cuentaId == null -> Icons.Filled.PointOfSale to Primary        // Mostrador
        tipo == "credito" -> Icons.Filled.PointOfSale to Primary        // Solo crédito
        tipo == "mixto" -> Icons.Filled.PointOfSale to Primary          // Mixto
        else -> Icons.Filled.PointOfSale to Primary                     // Contado
    }
}

@Composable
private fun EstadoChipVenta(estado: EstadoVenta) {
    val (texto, color) = when (estado) {
        EstadoVenta.PENDIENTE -> "PENDIENTE" to Warning
        EstadoVenta.CONSOLIDADA -> "CONSOLIDADA" to Accent
        EstadoVenta.CANCELADA -> "CANCELADA" to Danger
        EstadoVenta.DESCONOCIDO -> "—" to TextSecondary
    }
    Box(Modifier.clip(RoundedCornerShape(999.dp)).background(color.copy(alpha = 0.10f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(texto, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SheetItem(label: String, peligro: Boolean = false, habilitado: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = habilitado, onClick = onClick).padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (!habilitado) TextSecondary else if (peligro) Danger else TextPrimary,
        )
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) { content() }
}


internal fun tituloVenta(nombreMostrador: String?, nombreCliente: String?, cuentaId: Long?): String = when {
    nombreMostrador != null -> "Mostrador · $nombreMostrador"
    !nombreCliente.isNullOrBlank() -> nombreCliente
    cuentaId != null -> "Cuenta #$cuentaId"
    else -> "Venta"
}

internal fun registroLabel(nombre: String?): String =
    if (nombre.isNullOrBlank()) "" else "Registró: $nombre"
