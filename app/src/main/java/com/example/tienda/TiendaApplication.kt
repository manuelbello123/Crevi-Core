package com.example.tienda

import android.app.Application
import com.example.tienda.di.AppContainer
import com.example.tienda.di.createAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Punto de entrada de la app. Construye el [AppContainer] (DI manual) y
 * restaura la sesión guardada en segundo plano al arrancar.
 */
class TiendaApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = createAppContainer(context = this, isDebug = BuildConfig.DEBUG)
        appScope.launch { container.initialize() }
    }
}
