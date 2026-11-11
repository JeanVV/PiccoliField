package com.piccolifield.data.remote.api;

import java.util.List;

import com.piccolifield.data.model.AlertaDetalle;
import com.piccolifield.data.model.AsistenciaResumen;
import com.piccolifield.data.model.Impulsador;
import com.piccolifield.data.model.PuntoVentaDetalle;
import com.piccolifield.data.model.PuntoVentaHistorial;
import com.piccolifield.data.model.ReasignarImpulsadorResponse;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.model.ResumenDashboard;
import com.piccolifield.data.model.TurnoHistorial;
import com.piccolifield.data.model.Usuario;
import com.piccolifield.data.model.VendedorDetalle;
import com.piccolifield.data.model.VendedorRendimientoHistoricoResponse;
import com.piccolifield.data.remote.dto.request.ReasignarVendedorRequest;
import com.piccolifield.data.remote.dto.request.ResolverAlertaRequest;
import com.piccolifield.data.remote.dto.request.DispositivoRequest;
import com.piccolifield.data.remote.dto.request.FcmTokenRequest;
import com.piccolifield.data.remote.dto.request.JustificationRequest;
import com.piccolifield.data.remote.dto.request.LoginRequest;
import com.piccolifield.data.remote.dto.request.ReasignarImpulsadorRequest;
import com.piccolifield.data.remote.dto.request.MarcarAsistenciaRequest;
import com.piccolifield.data.remote.dto.request.RevisionJustificacionRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.dto.response.AsistenciaResponse;
import com.piccolifield.model.Alerta;
import com.piccolifield.data.model.ImpulsadorDashboard;
import com.piccolifield.data.model.ProductoBuscado;
import com.piccolifield.model.PuntoDistribucion;
import com.piccolifield.data.remote.dto.request.IncidenciaRequest;
import com.piccolifield.data.remote.dto.request.RegistrarVentaRequest;
import com.piccolifield.data.remote.dto.request.ReponerStockRequest;
import com.piccolifield.model.Vendedor;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.piccolifield.model.Prospecto;
import com.piccolifield.model.Seguimiento;

public interface ApiService {

    // =============================================================
    // 0. VERIFICACIÓN Y AUTENTICACIÓN
    // =============================================================
    @GET("/")
    Call<ResponseBody> pingApi();

    @POST("auth/login")
    Call<ResponseBody> iniciarSesion(@Body LoginRequest request);

    @GET("perfil")
    Call<ApiResponse<Usuario>> obtenerPerfil();

    @PUT("perfil/fcm-token")
    Call<ApiResponse<Object>> actualizarFcmToken(@Body FcmTokenRequest request);


    // =============================================================
    // 1. PUNTOS DE DISTRIBUCIÓN
    // =============================================================
    @GET("puntos-venta")
    Call<ApiResponse<List<PuntoDistribucion>>> listarPuntosVenta(
            @Query("search") String search,
            @Query("categoria") String categoria,
            @Query("distrito") Integer distrito,
            @Query("estado") String estado
    );
    
    @GET("puntos-venta/{id}")
    Call<ApiResponse<PuntoDistribucion>> obtenerDetallePunto(
            @Path("id") int id
    );

    @GET("pedidos")
    Call<ApiResponse<List<com.piccolifield.model.Pedido>>> obtenerPedidosPorPdv(
            @Query("idPdv") int idPdv
    );

    @POST("pedidos")
    Call<ApiResponse<Object>> registrarPedido(
            @Body com.piccolifield.model.Pedido pedido
    );

    @PUT("pedidos/{id}/estado")
    Call<ApiResponse<Object>> actualizarEstadoPedido(
            @Path("id") int idPedido,
            @Body com.piccolifield.data.remote.dto.request.EstadoRequest request
    );

    // -------------------------------------------------------------
    // ASISTENCIAS Y STOCK (Módulos adicionales)
    // -------------------------------------------------------------

    // ASISTENCIAS
    @GET("asistencias/resumen")
    Call<ApiResponse<AsistenciaResumen>> obtenerResumenAsistencias();

    @GET("asistencias/recientes")
    Call<ApiResponse<List<RegistroAsistencia>>> obtenerPersonalReciente();

    @GET("asistencias/colaboradores")
    Call<ApiResponse<List<RegistroAsistencia>>> obtenerColaboradoresConAsistencia();

    @GET("asistencias/historial/{idUsuario}")
    Call<ApiResponse<List<TurnoHistorial>>> obtenerHistorialTurnos(@Path("idUsuario") int idUsuario);

    @POST("asistencias/marcar")
    Call<ApiResponse<Object>> registrarAsistencia(@Body MarcarAsistenciaRequest request);

    @GET("asistencias/ultima")
    Call<ApiResponse<AsistenciaResponse>> obtenerUltimaAsistencia();

    @POST("asistencias/justificar")
    Call<ApiResponse<Object>> registrarJustificacion(@Body JustificationRequest request);

    @GET("asistencias/justificacion/{idUsuario}")
    Call<ApiResponse<RegistroAsistencia>> obtenerJustificacionUsuario(@Path("idUsuario") int idUsuario);

    @POST("asistencias/justificacion/revisar")
    Call<ApiResponse<Object>> revisarJustificacion(@Body RevisionJustificacionRequest request);


    // -------------------------------------------------------------
    // Superivisión de PDV
    // -------------------------------------------------------------
    @GET("supervision/resumen")
    Call<ApiResponse<ResumenDashboard>> obtenerResumenDashboard();

    @GET("supervision/vendedores")
    Call<ApiResponse<List<Vendedor>>> listarMejoresVendedores();

    @GET("supervision/vendedores/{id}")
    Call<ApiResponse<VendedorDetalle>> obtenerDetalleVendedor(
            @Path("id") int idUsuario
    );

    @GET("supervision/vendedores/{id}/rendimiento-historico")
    Call<ApiResponse<VendedorRendimientoHistoricoResponse>> obtenerRendimientoHistoricoVendedor(
            @Path("id") int idUsuario,
            @Query("anio") Integer anio
    );

    @POST("supervision/vendedores/{id}/reasignar")
    Call<ApiResponse<Object>> reasignarVendedor(
            @Path("id") int idUsuario,
            @Body ReasignarVendedorRequest request
    );

    @GET("supervision/alertas/{id}")
    Call<ApiResponse<AlertaDetalle>> obtenerDetalleAlerta(
            @Path("id") int idAlerta
    );

    @PUT("supervision/alertas/{id}/atender")
    Call<ApiResponse<Object>> atenderAlerta(
            @Path("id") int idAlerta,
            @Body ResolverAlertaRequest request
    );

    @PUT("supervision/alertas/{id}/ignorar")
    Call<ApiResponse<Object>> ignorarAlerta(
            @Path("id") int idAlerta,
            @Body ResolverAlertaRequest request
    );

    @POST("impulsador/stock/reponer")
    Call<ApiResponse<Object>> reponerStock(
            @Body ReponerStockRequest request
    );

    @GET("supervision/alertas")
    Call<ApiResponse<List<Alerta>>> listarAlertasSupervision();

    @GET("supervision/puntos-venta")
    Call<ApiResponse<List<com.piccolifield.model.SupervisionPuntoVenta>>> listarPuntosVentaSupervision();

    @GET("supervision/puntos-venta")
    Call<ApiResponse<List<com.piccolifield.model.SupervisionPuntoVenta>>> listarPuntosVentaSupervision(
            @Query("estado") String estado
    );

    @GET("supervision/impulsadores")
    Call<ApiResponse<List<Impulsador>>> listarImpulsadores();

    @GET("supervision/impulsadores/{id}/foto")
    Call<ResponseBody> obtenerFotoImpulsador(
            @Path("id") int idUsuario
    );

    @GET("supervision/puntos-venta/{id}")
    Call<ApiResponse<PuntoVentaDetalle>> obtenerDetallePuntoVentaSupervision(
            @Path("id") int idPuntosVenta
    );

    @GET("supervision/puntos-venta/{id}/historial")
    Call<ApiResponse<List<PuntoVentaHistorial>>> obtenerHistorialPuntoVenta(
            @Path("id") int idPuntosVenta
    );

    @POST("supervision/reasignar-impulsador")
    Call<ApiResponse<ReasignarImpulsadorResponse>> reasignarImpulsador(
            @Body ReasignarImpulsadorRequest request
    );

    @GET("supervision/productos/{id}/foto")
    Call<ResponseBody> obtenerFotoProducto(
            @Path("id") int idProducto
    );

    // =============================================================
    // 2. PROSPECTOS
    // =============================================================
    @GET("prospectos")
    Call<ApiResponse<List<Prospecto>>> listarProspectos(
            @Query("estado") String estado,
            @Query("zona") String zona,
            @Query("tipo") String tipo
    );

    @GET("prospectos/{id}")
    Call<ApiResponse<Prospecto>> obtenerProspecto(@Path("id") int id);

    @POST("prospectos")
    Call<ApiResponse<Prospecto>> registrarProspecto(@Body Prospecto prospecto);

    @PUT("prospectos/{id}/estado")
    Call<ApiResponse<Object>> actualizarEstadoProspecto(
            @Path("id") int id,
            @Body com.piccolifield.data.remote.dto.request.EstadoRequest request
    );

    @PUT("prospectos/{id}/asignar")
    Call<ApiResponse<Object>> asignarProspecto(
            @Path("id") int id,
            @Body com.piccolifield.data.remote.dto.request.ReasignarImpulsadorRequest request
    );

    @GET("prospectos/{id}/seguimientos")
    Call<ApiResponse<List<Seguimiento>>> listarSeguimientos(
            @Path("id") int id
    );

    @POST("prospectos/{id}/seguimientos")
    Call<ApiResponse<Seguimiento>> registrarSeguimiento(
            @Path("id") int id,
            @Body Seguimiento seguimiento
    );

    // =============================================================
    // 3. IMPULSADOR / VENDEDOR
    // =============================================================
    @GET("impulsador/dashboard")
    Call<ApiResponse<ImpulsadorDashboard>> obtenerDashboardImpulsador();

    @GET("impulsador/historial")
    Call<ApiResponse<List<PuntoVentaHistorial>>> obtenerHistorialAsignacionesImpulsador();

    @GET("impulsador/productos/buscar")
    Call<ApiResponse<ProductoBuscado>> buscarProductoPorCodigo(
            @Query("codigo") String codigo
    );

    @POST("impulsador/ventas/registrar")
    Call<ApiResponse<Object>> registrarVenta(
            @Body RegistrarVentaRequest request
    );

    @POST("impulsador/incidencias")
    Call<ApiResponse<Object>> reportarIncidencia(
            @Body IncidenciaRequest request
    );

    @PUT("dispositivo")
    Call<ApiResponse<Object>> actualizarDispositivo(@Body DispositivoRequest request);
}