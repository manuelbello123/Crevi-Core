package com.example.tienda.feature.corte.data.dto
import com.example.tienda.feature.corte.domain.CorteSucursal
import com.example.tienda.feature.corte.domain.CorteUsuario

import com.example.tienda.core.util.toMoney
import kotlinx.serialization.Serializable

@Serializable
data class CorteSucursalDto(
    val sucursalId: Long,
    val sucursal: String = "",
    val anioIso: Int = 0,
    val semanaIso: Int = 0,
    val desde: String = "",
    val hasta: String = "",
    val totalContado: String = "0",
    val totalAbonos: String = "0",
    val totalAnticipos: String = "0",
    val totalIngresos: String = "0",
)

@Serializable
data class CorteUsuarioDto(
    val sucursalId: Long,
    val sucursal: String = "",
    val anioIso: Int = 0,
    val semanaIso: Int = 0,
    val desde: String = "",
    val hasta: String = "",
    val usuarioId: Long = 0,
    val usuario: String = "",
    val rol: String = "",
    val contadoVendido: String = "0",
    val cobradoAbonos: String = "0",
    val anticipos: String = "0",
    val creditoColocado: String = "0",
    val devoluciones: String = "0",
    val numMovimientos: Int = 0,
)

fun CorteSucursalDto.toDomain(): CorteSucursal = CorteSucursal(
    sucursalId = sucursalId,
    sucursal = sucursal,
    anioIso = anioIso,
    semanaIso = semanaIso,
    desde = desde,
    hasta = hasta,
    totalContado = totalContado.toMoney(),
    totalAbonos = totalAbonos.toMoney(),
    totalAnticipos = totalAnticipos.toMoney(),
    totalIngresos = totalIngresos.toMoney(),
)

fun CorteUsuarioDto.toDomain(): CorteUsuario = CorteUsuario(
    sucursalId = sucursalId,
    sucursal = sucursal,
    anioIso = anioIso,
    semanaIso = semanaIso,
    desde = desde,
    hasta = hasta,
    usuarioId = usuarioId,
    usuario = usuario,
    rol = rol,
    contadoVendido = contadoVendido.toMoney(),
    cobradoAbonos = cobradoAbonos.toMoney(),
    anticipos = anticipos.toMoney(),
    creditoColocado = creditoColocado.toMoney(),
    devoluciones = devoluciones.toMoney(),
    numMovimientos = numMovimientos,
)
