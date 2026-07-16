package com.example.tienda

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.tienda.navigation.TiendaNavGraph
import com.example.tienda.core.ui.theme.TiendaTheme

/**
 * Única Activity. Extiende [FragmentActivity] porque el BiometricPrompt lo
 * requiere. Monta el grafo de navegación con el AppContainer de la Application.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tema claro: barras transparentes con iconos OSCUROS (light style),
        // sin importar el modo del sistema.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )

        val appContainer = (application as TiendaApplication).container

        setContent {
            TiendaTheme {
                TiendaNavGraph(appContainer = appContainer)
            }
        }
    }
}
