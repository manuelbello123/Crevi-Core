package com.example.tienda.feature.corte.ui
import com.example.tienda.feature.corte.domain.CorteSucursal
import com.example.tienda.feature.corte.domain.CorteUsuario

import com.example.tienda.core.util.UiText
import java.math.BigDecimal

/** Vista activa del corte: agregado por sucursal o desglose por usuario. */
enum class CorteVista { SUCURSAL, USUARIO }

/** Archivo Excel listo para guardar (evento de una sola vez; lo consume la pantalla). */
class ExcelDescarga(val nombre: String, val bytes: ByteArray)

data class CorteUiState(
    val esAdministrador: Boolean = false,

    // Semana ISO seleccionada
    val anio: Int = 0,
    val semana: Int = 0,
    /** true cuando la semana seleccionada es la actual (no se navega al futuro). */
    val esSemanaActual: Boolean = true,

    val vista: CorteVista = CorteVista.SUCURSAL,

    // Datos
    val porSucursal: List<CorteSucursal> = emptyList(),
    val porUsuario: List<CorteUsuario> = emptyList(),

    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,

    // Exportar Excel
    val exportando: Boolean = false,
    val excel: ExcelDescarga? = null,

    val mensaje: UiText? = null,
) {
    // Totales de la semana = suma del desglose por sucursal.
    val totalContado: BigDecimal get() = porSucursal.fold(BigDecimal.ZERO) { a, c -> a + c.totalContado }
    val totalAbonos: BigDecimal get() = porSucursal.fold(BigDecimal.ZERO) { a, c -> a + c.totalAbonos }
    val totalAnticipos: BigDecimal get() = porSucursal.fold(BigDecimal.ZERO) { a, c -> a + c.totalAnticipos }
    val totalIngresos: BigDecimal get() = porSucursal.fold(BigDecimal.ZERO) { a, c -> a + c.totalIngresos }

    /** Rango de fechas de la semana (tomado de cualquier fila del corte). */
    val desde: String? get() = porSucursal.firstOrNull()?.desde ?: porUsuario.firstOrNull()?.desde
    val hasta: String? get() = porSucursal.firstOrNull()?.hasta ?: porUsuario.firstOrNull()?.hasta

    /**
     * Desglose por usuario con las sucursales sumadas: un operador que registró en
     * varias sucursales aparece UNA sola vez con sus totales combinados.
     */
    val porUsuarioAgregado: List<CorteUsuario>
        get() = porUsuario.groupBy { it.usuarioId }.values.map { filas ->
            filas.reduce { acc, f ->
                acc.copy(
                    contadoVendido = acc.contadoVendido + f.contadoVendido,
                    cobradoAbonos = acc.cobradoAbonos + f.cobradoAbonos,
                    anticipos = acc.anticipos + f.anticipos,
                    creditoColocado = acc.creditoColocado + f.creditoColocado,
                    devoluciones = acc.devoluciones + f.devoluciones,
                    numMovimientos = acc.numMovimientos + f.numMovimientos,
                )
            }
        }

    /** Porcentaje (0..1) de una parte sobre el total de ingresos (para las cards). */
    fun proporcion(parte: BigDecimal): Float {
        if (totalIngresos <= BigDecimal.ZERO) return 0f
        return parte.divide(totalIngresos, 4, java.math.RoundingMode.HALF_UP).toFloat()
    }
}
