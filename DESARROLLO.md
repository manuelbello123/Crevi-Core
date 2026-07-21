# Tienda — Guía de desarrollo (handoff)

Documento para que cualquier persona o agente de IA entienda el proyecto y siga
desarrollándolo sin contexto previo. Última actualización: julio 2026.

---

## 1. Qué es la app

App **Android nativa** (Kotlin + Jetpack Compose) para la **gestión operativa de un
negocio de venta a crédito/contado con cobranza**. La usan **operadores internos**
(administradores y gerentes), **no** clientes finales. Consume un backend **Ktor +
MariaDB** que vive en un repo aparte.

- **Usuarios:** ~5 personas (admin + gerentes). Distribución por **APK firmado**
  (no Play Store).
- **Roles:** `ADMINISTRADOR` (todas las sucursales) y `GERENTE` (acotado a su
  sucursal). Existe también rol `CLIENTE` en el backend, pero lo consume **otra app
  distinta** (proyecto `Cliente`, KMP), no esta.

> Hay una app hermana **`Cliente`** (`D:\Users\victo\AndroidStudioProjects\Cliente`,
> Kotlin Multiplatform) para los clientes finales. Comparte el mismo backend. Cuando
> se copian patrones (ej. biometría "gate"), se referencia esa app.

---

## 2. Stack técnico

| Componente | Versión / nota |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose (BOM `2026.02.01`), Material 3 |
| compileSdk / targetSdk / minSdk | 35 / 35 / **28** |
| HTTP | Ktor **client** 3.5.0 (engine OkHttp) + kotlinx.serialization |
| Navegación | navigation-compose 2.9.2 (uso mínimo, ver §4) |
| Persistencia local | DataStore Preferences 1.2.1 (cifrado con Keystore) |
| Biometría | androidx.biometric 1.1.0 |
| DI | **Manual** (sin Hilt/Koin) — `di/AppContainer.kt` |
| Dinero | **Siempre `BigDecimal`** (nunca float/double) |

**Backend:** repo separado `D:\Users\victo\AndroidStudioProjects\Ktor-Tienda`
(Ktor server + MariaDB + Flyway). Ver §8.

---

## 3. Arquitectura

- **MVVM** con **DI manual**. Todo el grafo se arma en
  [`di/AppContainer.kt`](app/src/main/java/com/example/tienda/di/AppContainer.kt),
  instanciado una vez en `TiendaApplication`.
- **Package-by-feature:** `feature/<x>/{data, domain, ui}` + un `core/` compartido.
- **Single-Activity:** `MainActivity : FragmentActivity` (FragmentActivity es
  requisito del `BiometricPrompt`).
- **Navegación basada en estado, no rutas:** el `NavHost`
  ([`navigation/TiendaNavGraph.kt`](app/src/main/java/com/example/tienda/navigation/TiendaNavGraph.kt))
  solo tiene **dos destinos: Login y Home**. Dentro de Home, las secciones (Clientes,
  Ventas, Cobranzas, Corte, etc.) se conmutan con un `enum HomeSection` en
  `HomeScreen`, no con rutas. La navegación Login↔Home la dispara el `sessionFlow`
  del `SessionManager` (fuente de verdad de la sesión).
- Cada pantalla monta su propio **`TiendaTopBar`** (con hamburguesa que abre el
  drawer, o un back en detalles). El Home es el "shell" (drawer + bottom bar).

### Flujo de datos por feature
```
UI (Composable) → ViewModel (StateFlow<UiState>) → Repository (interface)
                                                  → RepositoryImpl → Api → HttpClient
```
- Los repos devuelven `NetworkResult<T>` (nunca lanzan). Ver §7.
- Los ViewModels exponen `StateFlow<XUiState>` y funciones de intención.

---

## 4. Estructura de carpetas

```
core/
  enums/        UserRole, etc.
  network/      NetworkConstants (BASE_URL), NetworkUtils (HttpClient), NetworkResult, NetworkError, safeApiCall
  security/     CryptoManager (AES/GCM Keystore, no biométrico), CredentialStore (refresh token cifrado)
  session/      SessionManager / DataStoreSessionManager, SessionData, SessionPreferencesKeys
  ui/
    theme/      Color, Type, Shapes (TiendaShapes/TiendaSpacing), TiendaTheme
    components/ tarjeta(), TiendaSearchBar, TiendaFab, BotonPrimario/Secundario, TiendaTopBar,
                TiendaBottomBar, PantallaEstado (PantallaError/PantallaEstadoVacio), TiendaSnackbarHost, etc.
    biometric/  BiometricAuthenticator (gate, sin CryptoObject)
  util/         Money.kt (toMoney/aMoneda), UiText
di/             AppContainer.kt
navigation/     TiendaNavGraph.kt
feature/
  login/        Login + refresh token + biometría
  home/         Shell (drawer + bottom bar) + dashboard/resumen semanal
  clientes/     CRUD clientes (admin/gerente)
  cobranzas/    Cuentas, saldos, abonos/anticipos/devoluciones, historial
  ventas/       Registrar/editar/cancelar ventas (contado/crédito/mixto/mostrador)
  corte/        Corte semanal + exportar Excel
  sucursales/   CRUD sucursales
  usuarios/     CRUD operadores (solo admin)
  configuraciones/ Cambiar contraseña, biometría, tema (no funcional aún), bloquear login clientes, reset
```

---

## 5. Convenciones INVARIABLES (respetarlas siempre)

1. **Dinero = `String` ⇄ `BigDecimal`.** Nunca float/double. Parseo con
   `toBigDecimalOrNull()` (helpers en `core/util/Money.kt`). Al backend se manda como
   string (`toPlainString()`).
2. **Package-by-feature** + `core/`. Nada de "utils" gigantes ni lógica de red en la UI.
3. **Cada pantalla monta su `TiendaTopBar`.** El switch de sucursal vive en el **Home**
   (drawer), y se sincroniza a Cobranzas/Ventas/Clientes vía `setSucursal()` en el
   `LaunchedEffect` del NavGraph.
4. **Sistema de diseño unificado** (§6): usar los tokens/componentes `Tienda*`, no
   hardcodear radios/colores/espaciados.
5. **Sin código espagueti/duplicado.** Reutilizar componentes de `core/ui/components`.
6. **Diseños Stitch** se adaptan al tema global de la app, no se copian tal cual.
7. **Ante dudas que dependen del backend, PREGUNTAR antes de codear.** El patrón del
   proyecto es: si falta soporte de backend, se da un **"prompt para el backend"**, el
   dueño lo implementa en el repo Ktor, y luego se cablea la app.

---

## 6. Sistema de diseño

- **Formas** (`core/ui/theme/Shapes.kt`): `TiendaShapes.Card/Field/Button/Dialog =
  RoundedCornerShape(12.dp)`, `Pill = 999.dp`. Espaciados en `TiendaSpacing`.
- **Colores** (`Color.kt`): Primary `#1A2B48` (navy), PrimaryDark `#031632`, Accent/
  Success teal `#006A60`, Danger `#BA1A1A`, Warning, Background `#F7F9FC`, Surface,
  SurfaceMuted, Border, BorderSoft, TextPrimary/Secondary/Disabled, OnPrimary, PrimarySoft.
- **Tipografía** (`Type.kt`, roles Material3): `displaySmall` 30/700 (montos),
  `headlineMedium` 24/600, `titleLarge` 20/600 (título pantalla), `titleMedium` 16/600
  (título card), `bodyLarge` 16, `bodyMedium` 14, `bodySmall` 12, `labelLarge` 15/600
  (botones), `labelMedium` 12/500 (metas).
- **Componentes reutilizables** (`core/ui/components/`): `Modifier.tarjeta()` (única
  fuente de verdad para cards: Surface + Border 1dp + radio 12), `TiendaSearchBar`,
  `TiendaFab` (FAB extendido con texto "Nuevo/Nueva X"), `BotonPrimario/Secundario`,
  `TiendaTopBar`, `TiendaBottomBar`, `PantallaError`/`PantallaEstadoVacio` (estados
  vacíos/error centrados uniformes), `TiendaSnackbarHost`, `ShowUiMessage`.

---

## 7. Networking

- **Base URL:** `core/network/NetworkConstants.kt` →
  `https://18-222-150-9.sslip.io` (hostname **temporal** vía sslip.io que resuelve a la
  IP del EC2 `18.222.150.9`; el cert Let's Encrypt valida por ese hostname).
  **Reemplazar por dominio propio cuando exista.** La app solo habla **HTTPS**
  (`usesCleartextTraffic` eliminado del manifest).
- **HttpClient** (`NetworkUtils.createHttpClient`): engine OkHttp, timeouts, JSON
  (`ignoreUnknownKeys`), `defaultRequest` inyecta el `Authorization: Bearer <token>`
  fresco en cada request vía `tokenProvider`, y un `HttpResponseValidator` traduce
  4xx/5xx a `NetworkError` (401 → `onUnauthorized` que cierra sesión si había una).
- **`NetworkResult<T>`** = `Success(data)` | `Error(NetworkError)`. Los repos usan
  `safeApiCall { ... }` que atrapa excepciones y las mapea. La UI convierte
  `NetworkError.asUiText()` → `UiText` para mostrar.

---

## 8. Backend (repo aparte: `Ktor-Tienda`)

Ktor server + MariaDB + Flyway (migraciones en `server/src/main/resources/db/migration`).
Estructura: `routes/` → `service/` (lógica + `Resultado`/`TipoError`) → `repository/`
(JDBC con prepared statements). Config vía `.env`/entorno (`config/Env.kt`, `AppConfig.kt`).

**Puntos clave del backend (para saber qué esperar):**
- **JWT** HMAC256 (`security/GeneradorJwt.kt`), access token ~1h. El claim lleva
  `id`, `rol`, `tokenVersion`. La sesión se **re-resuelve desde BD en cada request**
  (`AutenticacionService.resolverSesion`): valida que el operador siga `activo`, el
  cliente tenga `acceso_app`, y que `tokenVersion` coincida → revocación efectiva.
- **Refresh tokens** (tabla `refresh_token`, migración V4): opacos, rotan en cada uso,
  revocables (logout, cambio de contraseña, desactivación) con **detección de reuso**.
  Endpoints: `POST /auth/login`, `/auth/login/cliente`, `/auth/refresh`, `/auth/logout`.
- **Contraseñas:** BCrypt costo 12. **Rate limit** login: 5/min por IP.
- **Autorización sin IDOR:** cada endpoint por-id valida que el gerente solo toque
  datos de su sucursal (`operadorConCliente` / `cargarCuentaAccesible` /
  `verificarOperadorSucursal`). El admin ve todo.
- **Flag global** `login_clientes_deshabilitado` (tabla `configuracion`, endpoints
  `GET/PUT /config/login-clientes`, solo admin) — bloquea el login de la app Cliente.
- **Búsqueda:** `util/BusquedaSql.kt` arma `LIKE ? ESCAPE '\\'` (ojo: el escape SQL es
  delicado; ya se corrigió un bug donde `ESCAPE '\'` rompía toda búsqueda por texto).

> **Workflow con el backend:** el dueño del proyecto implementa los cambios de backend
> él mismo. El rol del agente es dar un **prompt claro y preciso** para el backend, y
> luego cablear la app cuando esté listo. No editar el backend por SSH ni tocar
> nginx/certbot del servidor (es infraestructura compartida entre proyectos).

---

## 9. Autenticación (flujo completo — importante)

**Login con contraseña:** `POST /auth/login` → `{ token, refreshToken, ... }`. Se
persiste `SessionData` (token + refreshToken, ambos **cifrados** con `CryptoManager`
en el DataStore de sesión).

**Biometría — enfoque "gate"** (copiado de la app Cliente, para evitar la fragilidad
del `CryptoObject`/clave biométrica del Keystore):
- `BiometricAuthenticator` (`core/ui/biometric/`) solo **verifica** al usuario con
  `BIOMETRIC_STRONG`, **sin CryptoObject**. Devuelve `Success/Cancelled/Failed`.
- `CredentialStore` (`core/security/`) guarda el **refresh token** cifrado (no la
  contraseña — la contraseña **nunca** se persiste).
- **Entrar con huella:** prompt gate → `loginWithBiometric()` → `POST /auth/refresh`
  con el refresh token guardado → nueva sesión + **token rotado** que se re-persiste.
  Si el backend responde 401 (revocado/cambió contraseña) → borra el token local y
  pide contraseña.
- **Activar huella:** desde Configuraciones; guarda el refresh token de la sesión
  activa (que vive en `SessionData`).
- El login biométrico se **auto-dispara** al abrir la pantalla de login si hay token
  guardado (como la app Cliente). Hay guarda de re-entrada para no canjear el mismo
  token dos veces (reuse detection).

Archivos: `feature/login/ui/{LoginViewModel, LoginRoute, LoginScreen, LoginUiState}`,
`feature/login/data/{LoginApi(Impl), LoginRepository(Impl)}`, `core/security/*`,
`core/session/*`.

---

## 10. Seguridad y build de release

Estado (todo hecho, rama `tsl`):
- ✅ **HTTPS** (sin cleartext).
- ✅ **R8 + shrinkResources** en release (`isMinifyEnabled`/`isShrinkResources = true`).
  Reglas en [`app/proguard-rules.pro`](app/proguard-rules.pro) (keep de
  kotlinx.serialization + DTOs `@Serializable` + enums). **Bugs de R8 solo aparecen en
  runtime** → probar el APK release en device tras cada cambio grande de serialización.
  El `mapping.txt` queda en `app/build/outputs/mapping/release/`.
- ✅ **`FLAG_SECURE`** en `MainActivity` (bloquea screenshots + miniatura de recientes).
- ✅ **`allowBackup="false"`** (evita respaldar el DataStore con tokens).
- ✅ **Firma de release:** `signingConfig` en `app/build.gradle.kts` lee
  `keystore.properties` (que está **gitignored**, junto con `*.jks`). El keystore es
  `tienda-release.jks` (PKCS12 → store y key comparten contraseña). **Cada
  `assembleRelease` firma solo.**
- ✅ Refresh token revocable en vez de guardar contraseña (§9).

**Config cache + lint:** `gradle.properties` tiene
`org.gradle.configuration-cache.problems=warn` porque la task `lintVitalAnalyzeRelease`
de AGP 9.x no serializa con la config cache (bug conocido). No es de R8.

> ⚠️ **Backup crítico:** `tienda-release.jks` + su contraseña NO están en git. Si se
> pierden, no se pueden firmar actualizaciones con la misma identidad (los 5 usuarios
> tendrían que desinstalar/reinstalar). Guardarlos fuera del repo.

**Generar el APK release:** `./gradlew :app:assembleRelease` → sale firmado en
`app/build/outputs/apk/release/app-release.apk`.

---

## 11. Pendientes / próximos pasos conocidos

- **Dominio propio:** reemplazar `18-222-150-9.sslip.io` por un dominio real (backend
  ya tiene nginx + Let's Encrypt; solo cambiar `BASE_URL` cuando exista el dominio).
- **Tema oscuro:** en Configuraciones el selector Sistema/Claro/Oscuro **persiste la
  preferencia pero NO cambia la paleta** (la paleta es solo clara). Falta hacer que
  `TiendaTheme` observe `PreferenciasApp.modoTema` y aplique una paleta oscura.
- **Idioma (i18n):** los textos están en español hardcodeados en código, no en
  `strings.xml`. Un switch de idioma real requiere extraer ~200 strings + `values-en/`.
- **Certificate pinning:** opcional (no crítico para 5 usuarios internos), se descartó
  por ahora.
- **`minSdk = 28` vs `MediaStore.Downloads` (API 29):** la exportación a Excel usa
  `MediaStore.Downloads` (API 29+); en Android 9 falla en silencio (capturado por
  `runCatching`). Considerar `minSdk = 29` o guardar por versión.
- **Bloquear login de clientes:** el toggle en Configuraciones ya llama a los endpoints
  reales del backend (`/config/login-clientes`), solo visible para admin.

---

## 12. Gotchas / decisiones a recordar

- **`DecimalFormat` es thread-safe vía `ThreadLocal`** en `Money.kt` (era un landmine;
  `DecimalFormat` compartido no es thread-safe).
- **Rotación de refresh token no es atómica:** si matan la app entre el `/auth/refresh`
  exitoso y el guardado del token nuevo, el guardado queda viejo → la próxima huella
  dispara reuse-detection → el backend revoca todo → el usuario entra con contraseña.
  Es aceptable (se recupera solo), pero tenerlo presente.
- **Keys de `LazyColumn`:** usar IDs únicos de BD; keys duplicadas **crashean** la lista
  (ya pasó una vez).
- **Búsqueda por sucursal:** Clientes/Cobranzas/Ventas filtran por la sucursal del
  switch del Home. La sucursal de una venta se **deriva del cliente** en el backend,
  no del switch.
- **Cuentas pagadas (saldo 0):** en `ClienteDetalle` el selector oculta las cuentas en
  $0 tras un TextButton "Ver cuentas pagadas"; el hero cuenta solo cuentas con saldo.

---

## 13. Cómo continuar desarrollando con IA

1. Leer este documento + explorar `di/AppContainer.kt` (mapa del grafo) y
   `navigation/TiendaNavGraph.kt` (mapa de pantallas).
2. Respetar las **convenciones invariables** (§5) y el **sistema de diseño** (§6).
3. Para features que tocan datos nuevos: **primero backend** (dar el prompt, el dueño
   lo implementa), **luego** cablear la app (DTO → Api → Repository → ViewModel → UI).
4. Verificar compilación con `./gradlew :app:compileDebugKotlin` y probar en device
   (`./gradlew :app:installDebug`). Para cambios de serialización, probar también el
   **release** (R8 activo).
5. Money siempre `BigDecimal`. Estados vacíos/error con `PantallaError`/
   `PantallaEstadoVacio`. Cards con `Modifier.tarjeta()`. FABs con `TiendaFab`.
