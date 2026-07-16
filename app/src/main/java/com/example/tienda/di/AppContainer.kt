package com.example.tienda.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.tienda.R
import com.example.tienda.core.network.NetworkUtils
import com.example.tienda.feature.clientes.data.ClienteApiImpl
import com.example.tienda.feature.clientes.data.ClienteRepository
import com.example.tienda.feature.clientes.data.ClienteRepositoryImpl
import com.example.tienda.feature.cobranzas.data.CobranzaApiImpl
import com.example.tienda.feature.cobranzas.data.CobranzaRepository
import com.example.tienda.feature.cobranzas.data.CobranzaRepositoryImpl
import com.example.tienda.feature.configuraciones.data.AuthApiImpl
import com.example.tienda.feature.configuraciones.data.AuthRepository
import com.example.tienda.feature.configuraciones.data.AuthRepositoryImpl
import com.example.tienda.feature.configuraciones.data.ConfigApiImpl
import com.example.tienda.feature.configuraciones.data.ConfigRepository
import com.example.tienda.feature.configuraciones.data.ConfigRepositoryImpl
import com.example.tienda.feature.configuraciones.data.PreferenciasApp
import com.example.tienda.feature.corte.data.CorteApiImpl
import com.example.tienda.feature.corte.data.CorteRepository
import com.example.tienda.feature.corte.data.CorteRepositoryImpl
import com.example.tienda.feature.ventas.data.VentaApiImpl
import com.example.tienda.feature.ventas.data.VentaRepository
import com.example.tienda.feature.ventas.data.VentaRepositoryImpl
import com.example.tienda.feature.login.data.LoginApi
import com.example.tienda.feature.login.data.LoginApiImpl
import com.example.tienda.feature.login.data.LoginRepository
import com.example.tienda.feature.login.data.LoginRepositoryImpl
import com.example.tienda.feature.sucursales.data.SucursalApiImpl
import com.example.tienda.feature.sucursales.data.SucursalRepository
import com.example.tienda.feature.sucursales.data.SucursalRepositoryImpl
import com.example.tienda.feature.usuarios.data.UsuarioApiImpl
import com.example.tienda.feature.usuarios.data.UsuarioRepository
import com.example.tienda.feature.usuarios.data.UsuarioRepositoryImpl
import com.example.tienda.core.security.CredentialStore
import com.example.tienda.core.security.CryptoManager
import com.example.tienda.core.session.DataStoreSessionManager
import com.example.tienda.core.session.SessionManager
import com.example.tienda.core.util.UiText
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Contenedor de dependencias (DI manual). Vive en la Application y arma el
 * grafo: crypto → session/credentials → HttpClient → apis/repositories.
 */
class AppContainer(
    engine: HttpClientEngine,
    isDebug: Boolean,
    sessionDataStore: DataStore<Preferences>,
    credentialDataStore: DataStore<Preferences>,
    preferenciasDataStore: DataStore<Preferences>,
    val versionApp: String,
    crypto: CryptoManager = CryptoManager(),
) {

    val sessionManager: SessionManager = DataStoreSessionManager(sessionDataStore, crypto)

    // Credenciales del login biométrico cifradas en reposo con la MISMA clave
    // (Keystore, no biométrica): tras pasar la puerta biométrica se descifran solas.
    val credentialStore: CredentialStore = CredentialStore(credentialDataStore, crypto)

    val httpClient: HttpClient = NetworkUtils.createHttpClient(
        engine = engine,
        isDebug = isDebug,
        tokenProvider = sessionManager::getToken,
        onUnauthorized = {
            // Solo forzamos logout si YA había sesión (token expirado / usuario
            // desactivado). Un 401 sin sesión es un login fallido: lo maneja la
            // pantalla de login con su propio mensaje.
            if (sessionManager.getSession() != null) {
                sessionManager.endSessionWithReason(UiText.Resource(R.string.error_unauthorized))
            }
        },
    )

    // ── Login feature ──
    private val loginApi: LoginApi = LoginApiImpl(httpClient)
    val loginRepository: LoginRepository = LoginRepositoryImpl(loginApi, sessionManager)

    // ── Clientes / Sucursales / Usuarios ──
    val clienteRepository: ClienteRepository = ClienteRepositoryImpl(ClienteApiImpl(httpClient))
    val sucursalRepository: SucursalRepository = SucursalRepositoryImpl(SucursalApiImpl(httpClient))
    val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(UsuarioApiImpl(httpClient))

    // ── Cobranza ──
    val cobranzaRepository: CobranzaRepository = CobranzaRepositoryImpl(CobranzaApiImpl(httpClient))

    // ── Ventas ──
    val ventaRepository: VentaRepository = VentaRepositoryImpl(VentaApiImpl(httpClient))

    // ── Corte semanal ──
    val corteRepository: CorteRepository = CorteRepositoryImpl(CorteApiImpl(httpClient))

    // ── Configuraciones ──
    val authRepository: AuthRepository = AuthRepositoryImpl(AuthApiImpl(httpClient))
    val configRepository: ConfigRepository = ConfigRepositoryImpl(ConfigApiImpl(httpClient))
    val preferenciasApp: PreferenciasApp = PreferenciasApp(preferenciasDataStore)

    // Aquí se irán agregando más apis y repositories por feature.

    /** Restaura la sesión persistida. Llamar una vez al arrancar la app. */
    suspend fun initialize() = sessionManager.initialize()
}

/** Fábrica usada desde la Application. */
fun createAppContainer(
    context: Context,
    isDebug: Boolean,
): AppContainer {
    val versionApp = runCatching {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        "${info.versionName} (${info.longVersionCode})"
    }.getOrDefault("1.0")
    return AppContainer(
        engine = OkHttp.create(),
        isDebug = isDebug,
        sessionDataStore = createDataStore(context, "session"),
        credentialDataStore = createDataStore(context, "credentials"),
        preferenciasDataStore = createDataStore(context, "preferencias"),
        versionApp = versionApp,
    )
}
