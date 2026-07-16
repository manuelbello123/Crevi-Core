package com.example.tienda.feature.ventas.data
import com.example.tienda.feature.ventas.domain.*
import com.example.tienda.feature.ventas.data.dto.*

import com.example.tienda.core.network.NetworkResult
import com.example.tienda.core.network.safeApiCall

class VentaRepositoryImpl(
    private val api: VentaApi,
) : VentaRepository {

    override suspend fun listar(
        sucursalId: Long?,
        fechaDesde: String?,
        fechaHasta: String?,
        estado: EstadoVenta?,
        pagina: Int,
        tamano: Int,
    ): NetworkResult<List<VentaResumen>> =
        safeApiCall {
            api.listar(
                sucursalId = sucursalId,
                fechaDesde = fechaDesde,
                fechaHasta = fechaHasta,
                estado = estado?.name?.lowercase(), // el query param va en minúsculas
                pagina = pagina,
                tamano = tamano,
            ).datos.map { it.toDomain() }
        }

    override suspend fun pendientes(): NetworkResult<List<VentaPendiente>> =
        safeApiCall { api.pendientes().map { it.toDomain() } }

    override suspend fun obtener(id: Long): NetworkResult<Venta> =
        safeApiCall { api.obtener(id).toDomain() }

    override suspend fun ventasDeCuenta(cuentaId: Long): NetworkResult<List<Venta>> =
        safeApiCall { api.ventasDeCuenta(cuentaId).map { it.toDomain() } }

    override suspend fun crear(
        cuentaId: Long?,
        nombreMostrador: String?,
        pendiente: Boolean,
        renglones: List<RenglonVentaInput>,
    ): NetworkResult<Venta> =
        safeApiCall {
            api.crear(
                RegistrarVentaRequest(
                    cuentaId = cuentaId,
                    nombreMostrador = nombreMostrador?.trim()?.ifBlank { null },
                    pendiente = pendiente,
                    renglones = renglones.map { it.toRequest() },
                )
            ).toDomain()
        }

    override suspend fun editarRenglones(id: Long, renglones: List<RenglonVentaInput>): NetworkResult<Venta> =
        safeApiCall {
            api.editarRenglones(id, EditarRenglonesRequest(renglones.map { it.toRequest() })).toDomain()
        }

    override suspend fun consolidar(id: Long): NetworkResult<Venta> =
        safeApiCall { api.consolidar(id).toDomain() }

    override suspend fun cancelar(id: Long): NetworkResult<Venta> =
        safeApiCall { api.cancelar(id).toDomain() }
}
