package com.example.tienda.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tienda.di.AppContainer
import com.example.tienda.feature.clientes.ui.ClientesScreen
import com.example.tienda.feature.clientes.ui.ClientesViewModel
import com.example.tienda.feature.cobranzas.ui.CobranzasScreen
import com.example.tienda.feature.cobranzas.ui.CobranzasViewModel
import com.example.tienda.feature.configuraciones.ui.ConfiguracionesScreen
import com.example.tienda.feature.configuraciones.ui.ConfiguracionesViewModel
import com.example.tienda.feature.corte.ui.CorteScreen
import com.example.tienda.feature.corte.ui.CorteViewModel
import com.example.tienda.feature.sucursales.ui.SucursalesScreen
import com.example.tienda.feature.sucursales.ui.SucursalesViewModel
import com.example.tienda.feature.home.ui.HomeScreen
import com.example.tienda.feature.home.ui.HomeViewModel
import com.example.tienda.feature.login.ui.LoginRoute
import com.example.tienda.feature.login.ui.LoginViewModel
import com.example.tienda.feature.usuarios.ui.UsuariosScreen
import com.example.tienda.feature.usuarios.ui.UsuariosViewModel
import com.example.tienda.feature.ventas.ui.VentasScreen
import com.example.tienda.feature.ventas.ui.VentasViewModel
import com.example.tienda.core.ui.biometric.rememberBiometricAuthenticator
import com.example.tienda.core.ui.theme.Background
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import kotlinx.coroutines.launch

sealed class NavRoutes(val route: String) {
    data object Login : NavRoutes("login")
    data object Home : NavRoutes("home")
}

@Composable
fun TiendaNavGraph(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    // Splash hasta restaurar la sesión persistida (evita parpadeo de login).
    val isReady by appContainer.sessionManager.isReady.collectAsStateWithLifecycle()
    if (!isReady) {
        SplashScreen()
        return
    }

    val session by appContainer.sessionManager.sessionFlow.collectAsStateWithLifecycle()

    // ViewModel compartido por Login y Home (conserva credenciales para enrolar
    // la huella tras un login con contraseña).
    val loginViewModel = viewModel {
        LoginViewModel(
            loginRepository = appContainer.loginRepository,
            sessionManager = appContainer.sessionManager,
            credentialStore = appContainer.credentialStore,
        )
    }

    val startDestination = remember {
        if (appContainer.sessionManager.getSession() != null) NavRoutes.Home.route
        else NavRoutes.Login.route
    }

    // Auth guard: la PRIMERA emisión sólo fija el start destination (no navega);
    // las siguientes (login/logout reales) redirigen.
    var isFirstEmission by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(session) {
        if (isFirstEmission) {
            isFirstEmission = false
            return@LaunchedEffect
        }
        if (session != null) {
            navController.navigate(NavRoutes.Home.route) {
                popUpTo(NavRoutes.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(NavRoutes.Home.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(NavRoutes.Login.route) {
            LoginRoute(viewModel = loginViewModel)
        }

        composable(NavRoutes.Home.route) {
            val biometric = rememberBiometricAuthenticator()
            val loginState by loginViewModel.state.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()

            val homeViewModel = viewModel {
                HomeViewModel(
                    sucursalRepository = appContainer.sucursalRepository,
                    corteRepository = appContainer.corteRepository,
                    ventaRepository = appContainer.ventaRepository,
                    cobranzaRepository = appContainer.cobranzaRepository,
                    sessionManager = appContainer.sessionManager,
                )
            }
            val homeState by homeViewModel.state.collectAsStateWithLifecycle()

            val cobranzasViewModel = viewModel {
                CobranzasViewModel(
                    clienteRepository = appContainer.clienteRepository,
                    cobranzaRepository = appContainer.cobranzaRepository,
                    ventaRepository = appContainer.ventaRepository,
                )
            }
            val ventasViewModel = viewModel {
                VentasViewModel(
                    ventaRepository = appContainer.ventaRepository,
                    clienteRepository = appContainer.clienteRepository,
                    cobranzaRepository = appContainer.cobranzaRepository,
                    sessionManager = appContainer.sessionManager,
                )
            }
            val clientesViewModel = viewModel {
                ClientesViewModel(
                    clienteRepository = appContainer.clienteRepository,
                    sucursalRepository = appContainer.sucursalRepository,
                    usuarioRepository = appContainer.usuarioRepository,
                    sessionManager = appContainer.sessionManager,
                )
            }

            // El switch de sucursal vive en Home; lo sincronizamos con las pantallas
            // acotadas por sucursal (Cobranza, Ventas y Clientes).
            LaunchedEffect(homeState.selectedSucursalId) {
                cobranzasViewModel.setSucursal(homeState.selectedSucursalId)
                ventasViewModel.setSucursal(homeState.selectedSucursalId)
                clientesViewModel.setSucursal(homeState.selectedSucursalId)
            }

            val usuariosViewModel = viewModel {
                UsuariosViewModel(
                    usuarioRepository = appContainer.usuarioRepository,
                    sucursalRepository = appContainer.sucursalRepository,
                    sessionManager = appContainer.sessionManager,
                )
            }

            val corteViewModel = viewModel {
                CorteViewModel(
                    corteRepository = appContainer.corteRepository,
                    sessionManager = appContainer.sessionManager,
                )
            }

            val sucursalesViewModel = viewModel {
                SucursalesViewModel(sucursalRepository = appContainer.sucursalRepository)
            }

            // Se puede enrolar la huella si el device es capaz. El refresh token vive
            // en SessionData (persistido cifrado), así que siempre está disponible.
            val enrollable = remember(biometric) {
                biometric?.canAuthenticate() == true
            }

            val configuracionesViewModel = viewModel {
                ConfiguracionesViewModel(
                    authRepository = appContainer.authRepository,
                    configRepository = appContainer.configRepository,
                    preferencias = appContainer.preferenciasApp,
                    credentialStore = appContainer.credentialStore,
                    sessionManager = appContainer.sessionManager,
                    versionApp = appContainer.versionApp,
                )
            }
            // La View reporta al VM si se puede enrolar (device + contraseña en memoria).
            LaunchedEffect(enrollable) {
                configuracionesViewModel.setBiometricEnrollable(enrollable)
            }

            val onEnableBiometric: () -> Unit = {
                val authenticator = biometric
                if (authenticator != null) {
                    scope.launch {
                        // Enfoque "gate": el prompt solo verifica; luego guardamos las
                        // credenciales en memoria. El flow reactivo actualiza el estado.
                        val result = authenticator.authenticate(
                            title = "Activar acceso con huella",
                            subtitle = "Confirma para guardar tu acceso",
                        )
                        if (result is com.example.tienda.core.ui.biometric.BiometricAuthResult.Success) {
                            loginViewModel.enableBiometric()
                        }
                    }
                }
            }

            HomeScreen(
                state = homeState,
                onSucursalSelected = homeViewModel::onSucursalSelected,
                onLogout = { loginViewModel.logout() },
                onRefreshResumen = homeViewModel::refrescarResumen,
                clientesContent = { onOpenMenu -> ClientesScreen(viewModel = clientesViewModel, onOpenMenu = onOpenMenu) },
                usuariosContent = { onOpenMenu -> UsuariosScreen(viewModel = usuariosViewModel, onOpenMenu = onOpenMenu) },
                cobranzasContent = { onOpenMenu -> CobranzasScreen(viewModel = cobranzasViewModel, onOpenMenu = onOpenMenu) },
                ventasContent = { onOpenMenu -> VentasScreen(viewModel = ventasViewModel, onOpenMenu = onOpenMenu) },
                corteContent = { onOpenMenu -> CorteScreen(viewModel = corteViewModel, onOpenMenu = onOpenMenu) },
                sucursalesContent = { onOpenMenu -> SucursalesScreen(viewModel = sucursalesViewModel, onOpenMenu = onOpenMenu) },
                configuracionesContent = { onOpenMenu ->
                    ConfiguracionesScreen(
                        viewModel = configuracionesViewModel,
                        onOpenMenu = onOpenMenu,
                        onEnableBiometric = onEnableBiometric,
                        onLogout = { loginViewModel.logout() },
                    )
                },
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Primary),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(26.dp),
                color = OnPrimary,
                strokeWidth = 2.dp,
            )
        }
    }
}
