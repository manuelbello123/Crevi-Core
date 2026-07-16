package com.example.tienda.feature.ventas.data
import com.example.tienda.feature.ventas.domain.*
import com.example.tienda.feature.ventas.data.dto.*

import com.example.tienda.core.network.NetworkResult

interface VentaRepository {

    suspend fun listar(
        sucursalId: Long?,
        fechaDesde: String?,
        fechaHasta: String?,
        estado: EstadoVenta?,
        pagina: Int,
        tamano: Int,
    ): NetworkResult<List<VentaResumen>>

    suspend fun pendientes(): NetworkResult<List<VentaPendiente>>

    suspend fun obtener(id: Long): NetworkResult<Venta>

    suspend fun ventasDeCuenta(cuentaId: Long): NetworkResult<List<Venta>>

    suspend fun crear(
        cuentaId: Long?,
        nombreMostrador: String?,
        pendiente: Boolean,
        renglones: List<RenglonVentaInput>,
    ): NetworkResult<Venta>

    suspend fun editarRenglones(id: Long, renglones: List<RenglonVentaInput>): NetworkResult<Venta>

    suspend fun consolidar(id: Long): NetworkResult<Venta>

    suspend fun cancelar(id: Long): NetworkResult<Venta>
}
