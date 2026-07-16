package com.example.tienda.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tienda.core.enums.UserRole
import com.example.tienda.core.ui.components.BottomBarItem
import com.example.tienda.core.ui.components.PantallaError
import com.example.tienda.core.ui.components.PantallaEstadoVacio
import com.example.tienda.core.ui.components.TiendaBottomBar
import com.example.tienda.core.ui.components.TiendaPullRefresh
import com.example.tienda.core.ui.components.TiendaTopBar
import com.example.tienda.core.ui.components.tarjeta
import com.example.tienda.core.ui.theme.TiendaShapes
import com.example.tienda.core.ui.theme.Accent
import com.example.tienda.core.ui.theme.Background
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.BorderSoft
import com.example.tienda.core.ui.theme.Danger
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import com.example.tienda.core.ui.theme.PrimarySoft
import com.example.tienda.core.ui.theme.Surface
import com.example.tienda.core.ui.theme.SurfaceMuted
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary
import com.example.tienda.core.ui.theme.Warning
import com.example.tienda.core.util.aMoneda
import com.example.tienda.feature.home.domain.ActividadItem
import com.example.tienda.feature.home.domain.ActividadTipo
import kotlinx.coroutines.launch

/**
 * Shell de la app: drawer + bottom bar. NO tiene topbar — cada pantalla monta
 * su propio [TiendaTopBar] (con la hamburguesa que abre el drawer, o un back en
 * pantallas de detalle). Las secciones se reciben como slots.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onSucursalSelected: (Long) -> Unit,
    onLogout: () -> Unit,
    onRefreshResumen: () -> Unit,
    clientesContent: @Composable (onOpenMenu: () -> Unit) -> Unit,
    usuariosContent: @Composable (onOpenMenu: () -> Unit) -> Unit,
    cobranzasContent: @Composable (onOpenMenu: () -> Unit) -> Unit,
    ventasContent: @Composable (onOpenMenu: () -> Unit) -> Unit,
    corteContent: @Composable (onOpenMenu: () -> Unit) -> Unit,
    sucursalesContent: @Composable (onOpenMenu: () -> Unit) -> Unit,
    configuracionesContent: @Composable (onOpenMenu: () -> Unit) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var section by remember { mutableStateOf(HomeSection.HOME) }

    val onOpenMenu: () -> Unit = { scope.launch { drawerState.open() } }

    val bottomItems = listOf(
        BottomBarItem("Inicio", Icons.Filled.Home, section == HomeSection.HOME) { section = HomeSection.HOME },
        BottomBarItem("Cobranzas", Icons.Filled.Payments, section == HomeSection.COBRANZAS) { section = HomeSection.COBRANZAS },
        BottomBarItem("Ventas", Icons.Filled.PointOfSale, section == HomeSection.VENTAS) { section = HomeSection.VENTAS },
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawer(
                state = state,
                currentSection = section,
                onNavigate = { target -> section = target; scope.launch { drawerState.close() } },
                onSucursalSelected = onSucursalSelected,
            )
        },
    ) {
        Scaffold(
            containerColor = Background,
            bottomBar = { TiendaBottomBar(items = bottomItems) },
        ) { padding ->
            // Solo padding inferior (bottom bar); el inset superior lo maneja cada
            // pantalla con su TiendaTopBar.
            Box(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
                when (section) {
                    HomeSection.HOME -> ResumenContent(state, onOpenMenu, onRefreshResumen)
                    HomeSection.COBRANZAS -> cobranzasContent(onOpenMenu)
                    HomeSection.VENTAS -> ventasContent(onOpenMenu)
                    HomeSection.CORTE -> corteContent(onOpenMenu)
                    HomeSection.CLIENTES -> clientesContent(onOpenMenu)
                    HomeSection.USUARIOS -> usuariosContent(onOpenMenu)
                    HomeSection.SUCURSALES -> sucursalesContent(onOpenMenu)
                    HomeSection.CONFIGURACIONES -> configuracionesContent(onOpenMenu)
                }
            }
        }
    }
}

// ── Drawer ──

@Composable
private fun HomeDrawer(
    state: HomeUiState,
    currentSection: HomeSection,
    onNavigate: (HomeSection) -> Unit,
    onSucursalSelected: (Long) -> Unit,
) {
    ModalDrawerSheet(drawerContainerColor = Surface) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {

            Column(Modifier.padding(start = 8.dp, end = 8.dp, top = 20.dp, bottom = 12.dp)) {
                Text(state.nombre, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(rolLabel(state.rol), style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                if (state.esAdministrador) {
                    Spacer(Modifier.height(12.dp))
                    SucursalSwitcher(state = state, onSucursalSelected = onSucursalSelected)
                }
            }
            HorizontalDivider(color = BorderSoft)
            Spacer(Modifier.height(8.dp))

            DrawerItem("Corte", Icons.Filled.Summarize, currentSection == HomeSection.CORTE) {
                onNavigate(HomeSection.CORTE)
            }
            DrawerItem("Clientes", Icons.Filled.People, currentSection == HomeSection.CLIENTES) {
                onNavigate(HomeSection.CLIENTES)
            }
            if (state.esAdministrador) {
                DrawerItem("Usuarios", Icons.Filled.ManageAccounts, currentSection == HomeSection.USUARIOS) {
                    onNavigate(HomeSection.USUARIOS)
                }
                DrawerItem("Sucursales", Icons.Filled.Storefront, currentSection == HomeSection.SUCURSALES) {
                    onNavigate(HomeSection.SUCURSALES)
                }
            }
            DrawerItem("Configuraciones", Icons.Filled.Settings, currentSection == HomeSection.CONFIGURACIONES) {
                onNavigate(HomeSection.CONFIGURACIONES)
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SucursalSwitcher(
    state: HomeUiState,
    onSucursalSelected: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TiendaShapes.Card)
                .background(SurfaceMuted)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Storefront, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(
                text = state.selectedSucursalNombre ?: "Selecciona sucursal",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextSecondary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.sucursales.forEach { sucursal ->
                DropdownMenuItem(
                    text = { Text(sucursal.nombre) },
                    onClick = {
                        onSucursalSelected(sucursal.id)
                        expanded = false
                    },
                    leadingIcon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
                )
            }
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(label) },
        icon = { Icon(icon, contentDescription = null) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = PrimarySoft,
            unselectedContainerColor = Surface,
            selectedTextColor = Primary,
            unselectedTextColor = TextSecondary,
            selectedIconColor = Primary,
            unselectedIconColor = TextSecondary,
        ),
    )
}

// ── Secciones simples del shell (con su propia barra) ──

// ── Resumen (dashboard del Home) ──

@Composable
private fun ResumenContent(state: HomeUiState, onOpenMenu: () -> Unit, onRefresh: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TiendaTopBar(title = "Inicio", onNavigationClick = onOpenMenu)
        when {
            state.sinSucursal ->
                PantallaEstadoVacio("Selecciona una sucursal para ver el resumen")

            state.resumenLoading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                }

            state.resumenError != null && state.actividad.isEmpty() ->
                PantallaError("No se pudo cargar el resumen", onRefresh)

            else -> TiendaPullRefresh(refreshing = state.resumenRefreshing, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { Saludo(state.nombre, state.selectedSucursalNombre) }
                    item { RecaudadoCard(state.totalRecaudado) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MiniStat("Contado", state.totalContado, Modifier.weight(1f))
                            MiniStat("Abonos", state.totalAbonos, Modifier.weight(1f))
                            MiniStat("Anticipos", state.totalAnticipos, Modifier.weight(1f))
                        }
                    }
                    item { PendientesCard(state.pendientesCount) }
                    item { Text("Actividad reciente", style = MaterialTheme.typography.titleMedium, color = TextPrimary) }
                    if (state.actividad.isEmpty()) {
                        item { Text("Sin actividad esta semana", style = MaterialTheme.typography.bodyMedium, color = TextSecondary) }
                    } else {
                        // Toda la actividad va dentro de UNA sola tarjeta con divisores
                        // tenues entre filas — no parece un módulo aparte por movimiento.
                        item {
                            Column(Modifier.fillMaxWidth().tarjeta()) {
                                state.actividad.forEachIndexed { i, mov ->
                                    if (i > 0) HorizontalDivider(color = BorderSoft)
                                    ActividadRow(mov)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun Saludo(nombre: String, sucursal: String?) {
    Column {
        Text("Hola, ${nombre.substringBefore(' ')}", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Text(sucursal ?: "Tu sucursal", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun RecaudadoCard(total: java.math.BigDecimal) {
    Column(Modifier.fillMaxWidth().clip(TiendaShapes.Card).background(Primary).padding(20.dp)) {
        Text("RECAUDADO ESTA SEMANA", style = MaterialTheme.typography.labelMedium, color = OnPrimary.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(total.aMoneda(), style = MaterialTheme.typography.displaySmall, color = OnPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MiniStat(label: String, monto: java.math.BigDecimal, modifier: Modifier = Modifier) {
    Column(modifier.tarjeta().padding(12.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(monto.aMoneda(), style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PendientesCard(count: Int) {
    val hay = count > 0
    Row(Modifier.fillMaxWidth().tarjeta().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background((if (hay) Warning else Accent).copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.PendingActions, contentDescription = null, tint = if (hay) Warning else Accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Ventas pendientes", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(if (hay) "Requieren consolidación" else "Todo al día", style = MaterialTheme.typography.bodySmall, color = if (hay) Warning else TextSecondary)
        }
        Text("$count", style = MaterialTheme.typography.displaySmall, color = if (hay) Warning else TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActividadRow(item: ActividadItem) {
    // Fila plana — el fondo/tarjeta lo pone el contenedor de la lista.
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        val color = colorActividad(item.tipo)
        Box(Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(iconoActividad(item.tipo), contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.titulo, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1)
            Text(item.subtitulo, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Spacer(Modifier.size(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(montoActividad(item), style = MaterialTheme.typography.titleMedium, color = colorMonto(item.tipo), fontWeight = FontWeight.SemiBold)
            Text(item.cuando, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
    }
}

private fun iconoActividad(tipo: ActividadTipo) = when (tipo) {
    ActividadTipo.VENTA, ActividadTipo.PENDIENTE, ActividadTipo.CANCELADA -> Icons.Filled.PointOfSale
    else -> Icons.Filled.Payments
}

private fun colorActividad(tipo: ActividadTipo) = when (tipo) {
    ActividadTipo.VENTA -> Primary
    ActividadTipo.PENDIENTE -> Warning
    ActividadTipo.CANCELADA, ActividadTipo.DEVOLUCION -> Danger
    else -> Accent
}

private fun colorMonto(tipo: ActividadTipo) = when (tipo) {
    ActividadTipo.ABONO, ActividadTipo.ANTICIPO -> Accent
    ActividadTipo.DEVOLUCION -> Danger
    else -> TextPrimary
}

private fun montoActividad(item: ActividadItem): String = when (item.tipo) {
    ActividadTipo.ABONO, ActividadTipo.ANTICIPO -> "+${item.monto.aMoneda()}"
    ActividadTipo.DEVOLUCION -> "-${item.monto.aMoneda()}"
    else -> item.monto.aMoneda()
}

@Composable
private fun rolLabel(rol: UserRole?): String = when (rol) {
    UserRole.ADMINISTRADOR -> "Administrador"
    UserRole.GERENTE -> "Gerente"
    null -> ""
}
