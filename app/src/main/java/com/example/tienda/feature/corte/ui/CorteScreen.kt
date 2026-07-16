package com.example.tienda.feature.corte.ui
import com.example.tienda.feature.corte.domain.CorteSucursal
import com.example.tienda.feature.corte.domain.CorteUsuario

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tienda.core.util.aMoneda
import com.example.tienda.core.ui.components.PantallaError
import com.example.tienda.core.ui.components.ShowUiMessage
import com.example.tienda.core.ui.components.TiendaPullRefresh
import com.example.tienda.core.ui.components.TiendaSnackbarHost
import com.example.tienda.core.ui.components.TiendaTopBar
import com.example.tienda.core.ui.components.tarjeta
import com.example.tienda.core.ui.theme.TiendaShapes
import com.example.tienda.core.ui.theme.Accent
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import com.example.tienda.core.ui.theme.PrimaryDark
import com.example.tienda.core.ui.theme.PrimarySoft
import com.example.tienda.core.ui.theme.Surface
import com.example.tienda.core.ui.theme.SurfaceMuted
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary
import com.example.tienda.core.ui.theme.Warning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorteScreen(viewModel: CorteViewModel, onOpenMenu: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ShowUiMessage(state.mensaje, snackbarHostState, viewModel::clearMensaje)
    ShowUiMessage(if (state.porSucursal.isNotEmpty()) state.error else null, snackbarHostState, viewModel::clearError)

    // Al llegar los bytes del Excel: guardar en Descargas y avisar.
    LaunchedEffect(state.excel) {
        val excel = state.excel ?: return@LaunchedEffect
        val ok = withContext(Dispatchers.IO) { guardarEnDescargas(context, excel.nombre, excel.bytes) }
        snackbarHostState.showSnackbar(if (ok) "Guardado en Descargas: ${excel.nombre}" else "No se pudo guardar el archivo")
        viewModel.excelDescargado()
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TiendaTopBar(title = "Corte", onNavigationClick = onOpenMenu)

            TiendaPullRefresh(
                refreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f),
            ) {
                when {
                    state.isLoading && state.porSucursal.isEmpty() ->
                        Centrado { CircularProgressIndicator(color = Primary, strokeWidth = 2.dp) }

                    state.error != null && state.porSucursal.isEmpty() ->
                        PantallaError("No se pudo cargar el corte", viewModel::retry)

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            NavegadorSemana(
                                anio = state.anio,
                                semana = state.semana,
                                rango = rangoLabel(state.desde, state.hasta),
                                esSemanaActual = state.esSemanaActual,
                                onAnterior = viewModel::semanaAnterior,
                                onSiguiente = viewModel::semanaSiguiente,
                            )
                        }
                        item { VistaToggle(state.vista, viewModel::setVista) }
                        item {
                            TotalCard(
                                total = state.totalIngresos,
                                anio = state.anio,
                                semana = state.semana,
                                rango = rangoLabel(state.desde, state.hasta),
                                exportando = state.exportando,
                                onExportar = viewModel::exportarExcel,
                            )
                        }
                        item {
                            ResumenCard("Ventas de contado", state.totalContado, state.proporcion(state.totalContado), Icons.Filled.PointOfSale, Accent)
                        }
                        item {
                            ResumenCard("Abonos", state.totalAbonos, state.proporcion(state.totalAbonos), Icons.Filled.Payments, Primary)
                        }
                        item {
                            ResumenCard("Anticipos", state.totalAnticipos, state.proporcion(state.totalAnticipos), Icons.Filled.RequestQuote, Warning)
                        }

                        item {
                            Text(
                                if (state.vista == CorteVista.SUCURSAL) "Desglose por sucursal" else "Desglose por usuario",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                        }

                        if (state.vista == CorteVista.SUCURSAL) {
                            if (state.porSucursal.isEmpty()) {
                                item { Text("Sin movimientos esta semana", style = MaterialTheme.typography.bodyMedium, color = TextSecondary) }
                            } else {
                                items(state.porSucursal) { SucursalCard(it) }
                            }
                        } else {
                            when {
                                state.porUsuario.isEmpty() ->
                                    item {
                                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                                        }
                                    }
                                else -> items(state.porUsuarioAgregado) { UsuarioCard(it) }
                            }
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
        TiendaSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }
}

// ── Encabezado / semana ──

@Composable
private fun NavegadorSemana(
    anio: Int,
    semana: Int,
    rango: String,
    esSemanaActual: Boolean,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onAnterior) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Semana anterior", tint = Primary)
        }
        Column(Modifier.weight(1f)) {
            Text(
                "Semana $semana · $anio",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (rango.isNotEmpty()) {
                Text(rango, style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
        IconButton(onClick = onSiguiente, enabled = !esSemanaActual) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Semana siguiente", tint = if (esSemanaActual) Border else Primary)
        }
    }
}

@Composable
private fun VistaToggle(vista: CorteVista, onSelect: (CorteVista) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(SurfaceMuted).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TogglePill("Por sucursal", vista == CorteVista.SUCURSAL, Modifier.weight(1f)) { onSelect(CorteVista.SUCURSAL) }
        TogglePill("Por usuario", vista == CorteVista.USUARIO, Modifier.weight(1f)) { onSelect(CorteVista.USUARIO) }
    }
}

@Composable
private fun TogglePill(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(TiendaShapes.Field)
            .background(if (selected) Surface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Primary else TextSecondary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Cards de resumen ──

@Composable
private fun TotalCard(
    total: BigDecimal,
    anio: Int,
    semana: Int,
    rango: String,
    exportando: Boolean,
    onExportar: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        TarjetaCredito(total = total, anio = anio, semana = semana, rango = rango)
        Spacer(Modifier.height(12.dp))
        // Acción secundaria (botón fuera de la card, coherente con el resto de la app).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TiendaShapes.Button)
                .border(1.dp, Primary, TiendaShapes.Button)
                .clickable(enabled = !exportando, onClick = onExportar)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (exportando) {
                CircularProgressIndicator(Modifier.size(18.dp), color = Primary, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.Download, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.size(8.dp))
            Text("Exportar Excel", style = MaterialTheme.typography.labelLarge, color = Primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Card estilo tarjeta de crédito: gradiente diagonal navy, chip dorado,
 * número ficticio (año+semana como referencia visual) y "titular / válido".
 * Es puramente decorativa — la info real (rango) va en el titular/vigencia.
 */
@Composable
private fun TarjetaCredito(total: BigDecimal, anio: Int, semana: Int, rango: String) {
    val gradient = Brush.linearGradient(
        colors = listOf(PrimaryDark, Primary, Primary.copy(alpha = 0.85f)),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1000f, 700f),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.586f) // proporción real de una tarjeta bancaria
            .clip(TiendaShapes.Card)
            .background(gradient)
            .padding(20.dp),
    ) {
        // Fila superior: chip + brand mark
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChipTarjeta()
            Spacer(Modifier.weight(1f))
            BrandTarjeta()
        }

        // Bloque central-superior: monto grande
        Column(Modifier.align(Alignment.CenterStart)) {
            Text(
                "RECAUDADO",
                style = MaterialTheme.typography.labelMedium,
                color = OnPrimary.copy(alpha = 0.65f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.15.em,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                total.aMoneda(),
                style = MaterialTheme.typography.displaySmall,
                color = OnPrimary,
                fontWeight = FontWeight.Bold,
            )
        }

        // Fila inferior: "número" ficticio + titular / vigencia
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth()) {
            Text(
                numeroTarjeta(anio, semana),
                style = MaterialTheme.typography.titleMedium,
                color = OnPrimary,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.15.em,
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("TITULAR", style = MaterialTheme.typography.labelSmall, color = OnPrimary.copy(alpha = 0.55f), letterSpacing = 0.1.em)
                    Text(
                        "CORTE SEMANAL",
                        style = MaterialTheme.typography.labelLarge,
                        color = OnPrimary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.05.em,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("VIGENCIA", style = MaterialTheme.typography.labelSmall, color = OnPrimary.copy(alpha = 0.55f), letterSpacing = 0.1.em)
                    Text(
                        rango.ifBlank { "S$semana / $anio" },
                        style = MaterialTheme.typography.labelLarge,
                        color = OnPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipTarjeta() {
    // Chip metálico ficticio con degradado y "líneas de contacto".
    Box(
        modifier = Modifier
            .size(width = 42.dp, height = 30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFE9C77B), Color(0xFFB8912D), Color(0xFFE9C77B)),
                )
            ),
    ) {
        // Líneas horizontales tenues para simular contactos del chip.
        Column(Modifier.align(Alignment.Center)) {
            repeat(3) {
                Box(
                    Modifier
                        .padding(vertical = 2.dp)
                        .size(width = 26.dp, height = 1.dp)
                        .background(Color(0xFF7A5B15).copy(alpha = 0.6f))
                )
            }
        }
    }
}

@Composable
private fun BrandTarjeta() {
    // Marca de la app en la esquina superior derecha (círculo con inicial).
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(OnPrimary.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "T",
            style = MaterialTheme.typography.titleMedium,
            color = OnPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Genera un "número" con estilo tarjeta usando año/semana, resto oculto. */
private fun numeroTarjeta(anio: Int, semana: Int): String {
    val bloque = "%02d%02d".format(anio % 100, semana)
    return "•••• •••• •••• $bloque"
}

@Composable
private fun ResumenCard(label: String, monto: BigDecimal, proporcion: Float, icon: ImageVector, tint: Color) {
    Column(Modifier.fillMaxWidth().tarjeta().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.clip(RoundedCornerShape(999.dp)).background(SurfaceMuted).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("${(proporcion * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(monto.aMoneda(), style = MaterialTheme.typography.titleLarge, color = Primary, fontWeight = FontWeight.Bold)
    }
}

// ── Desglose ──

@Composable
private fun SucursalCard(item: CorteSucursal) {
    Column(Modifier.fillMaxWidth().tarjeta().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBox(Icons.Filled.Store)
            Spacer(Modifier.size(12.dp))
            Text(item.sucursal, style = MaterialTheme.typography.titleMedium, color = TextPrimary, modifier = Modifier.weight(1f))
            Text(item.totalIngresos.aMoneda(), style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        StatsRow(
            "Contado" to item.totalContado,
            "Abonos" to item.totalAbonos,
            "Anticipos" to item.totalAnticipos,
        )
    }
}

@Composable
private fun UsuarioCard(item: CorteUsuario) {
    val ingresos = item.contadoVendido + item.cobradoAbonos + item.anticipos
    Column(Modifier.fillMaxWidth().tarjeta().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBox(Icons.Filled.Person)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.usuario, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("${item.rol.lowercase().replaceFirstChar { it.uppercase() }} · ${item.numMovimientos} mov.", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
            Text(ingresos.aMoneda(), style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        StatsRow(
            "Contado" to item.contadoVendido,
            "Abonos" to item.cobradoAbonos,
            "Anticipos" to item.anticipos,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Crédito colocado ${item.creditoColocado.aMoneda()} · Devoluciones ${item.devoluciones.aMoneda()}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

@Composable
private fun StatsRow(vararg stats: Pair<String, BigDecimal>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.forEach { (label, monto) ->
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text(monto.aMoneda(), style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun IconBox(icon: ImageVector) {
    Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(PrimarySoft), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
    }
}

// ── Reutilizables ──

@Composable
private fun Centrado(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) { content() }
}

private val LOCALE_ES = java.util.Locale.forLanguageTag("es")
private val DIA_MES = java.time.format.DateTimeFormatter.ofPattern("d MMM", LOCALE_ES)

private fun rangoLabel(desde: String?, hasta: String?): String {
    val d = desde?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val h = hasta?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    if (d == null || h == null) return ""
    return "${d.format(DIA_MES)} – ${h.format(DIA_MES)}"
}

/** Guarda el .xlsx en la carpeta Descargas (MediaStore, API 29+). */
private fun guardarEnDescargas(context: Context, nombre: String, bytes: ByteArray): Boolean = runCatching {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, nombre)
        put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        put(MediaStore.Downloads.IS_PENDING, 1)
    }
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
    resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return false
    values.clear()
    values.put(MediaStore.Downloads.IS_PENDING, 0)
    resolver.update(uri, values, null, null)
    true
}.getOrDefault(false)
