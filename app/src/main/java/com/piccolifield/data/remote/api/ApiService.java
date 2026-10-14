package com.piccolifield.data.remote.api;

import java.util.List;

import com.piccolifield.data.model.Usuario;
import com.piccolifield.data.remote.dto.EstadoRequest;
import com.piccolifield.data.remote.dto.EstadoUsuarioRequest;
import com.piccolifield.data.remote.dto.FcmTokenRequest;
import com.piccolifield.data.remote.dto.request.LoginRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Alerta;
import com.piccolifield.model.Asistencia;
import com.piccolifield.model.Pedido;
import com.piccolifield.model.Producto;
import com.piccolifield.model.PuntoVenta;
import com.piccolifield.model.StockRegistro;

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

    @GET("perfil/foto")
    Call<ResponseBody> obtenerFotoPerfil();

    @PUT("perfil/fcm-token")
    Call<ApiResponse<Object>> actualizarFcmToken(@Body FcmTokenRequest request);

    @GET("usuarios")
    Call<ApiResponse<List<Usuario>>> listarUsuarios(
            @Query("rol") String rol,
            @Query("estado") String estado
    );

    @PATCH("usuarios/{idUsuario}/estado")
    Call<ApiResponse<Object>> cambiarEstadoUsuario(
            @Path("idUsuario") int idUsuario,
            @Body EstadoUsuarioRequest request
    );

    // =============================================================
    // 1. PUNTOS DE DISTRIBUCIÓN
    // =============================================================
    @GET("puntos-venta")
    Call<ApiResponse<List<PuntoVenta>>> listarPuntosVenta(
            @Query("search") String search,
            @Query("categoria") String categoria,
            @Query("distrito") Integer distrito,
            @Query("estado") String estado
    );

    @GET("puntos-venta/cercanos")
    Call<ApiResponse<List<PuntoVenta>>> listarPuntosCercanos(
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("radio_km") double radioKm
    );

    @GET("puntos-venta/{id}")
    Call<ApiResponse<PuntoVenta>> obtenerDetallePuntoVenta(@Path("id") int id);

    @GET("puntos-venta/{id}/productos")
    Call<ApiResponse<List<Producto>>> obtenerProductosPunto(@Path("id") int id);

    @POST("puntos-venta")
    Call<ApiResponse<PuntoVenta>> registrarPuntoVenta(@Body PuntoVenta puntoVenta);

    @PUT("puntos-venta/{id}")
    Call<ApiResponse<PuntoVenta>> actualizarPuntoVenta(@Path("id") int id, @Body PuntoVenta puntoVenta);

    @PATCH("puntos-venta/{id}/estado")
    Call<ApiResponse<Object>> cambiarEstadoPuntoVenta(@Path("id") int id, @Body EstadoRequest request);

    @DELETE("puntos-venta/{id}")
    Call<ApiResponse<Object>> eliminarPuntoVenta(@Path("id") int id);

    // =============================================================
    // 2. MAPA DE CALOR Y DEMANDA
    // =============================================================
    @GET("mapa-calor/pedidos")
    Call<ApiResponse<List<Object>>> obtenerMapaCalorPedidos();

    @GET("mapa-calor/zonas")
    Call<ApiResponse<List<Object>>> obtenerZonasDemanda(@Query("distrito") Integer distrito);

    @GET("puntos-venta/{id}/historial-pedidos")
    Call<ApiResponse<List<Pedido>>> obtenerHistorialPedidosPunto(@Path("id") int id);

    @GET("puntos-venta/{id}/resumen-demanda")
    Call<ApiResponse<Object>> obtenerResumenDemandaPunto(@Path("id") int id);

    // =============================================================
    // 3. PEDIDOS Y REABASTECIMIENTO
    // =============================================================
    @GET("pedidos")
    Call<ApiResponse<List<Pedido>>> listarPedidos(@Query("estado") String estado);

    @POST("pedidos")
    Call<ApiResponse<Pedido>> crearPedido(@Body Object request);

    @GET("pedidos/{id}")
    Call<ApiResponse<Pedido>> obtenerPedido(@Path("id") int id);

    @PUT("pedidos/{id}")
    Call<ApiResponse<Pedido>> modificarPedido(@Path("id") int id, @Body Object request);

    @PATCH("pedidos/{id}/cancelar")
    Call<ApiResponse<Object>> cancelarPedido(@Path("id") int id, @Body Object request);

    @DELETE("pedidos/{id}")
    Call<ApiResponse<Object>> eliminarPedido(@Path("id") int id);

    // =============================================================
    // 4. DESPACHO Y REPARTIDORES
    // =============================================================
    @GET("repartidores/disponibles")
    Call<ApiResponse<List<Usuario>>> listarRepartidoresDisponibles();

    @GET("despachos")
    Call<ApiResponse<List<Object>>> listarDespachos(@Query("estado") String estado);

    @POST("despachos")
    Call<ApiResponse<Object>> crearDespacho(@Body Object request);

    @POST("despachos/{id}/asignar-repartidor")
    Call<ApiResponse<Object>> asignarRepartidor(@Path("id") int id, @Body Object request);

    @GET("repartidor/mis-despachos")
    Call<ApiResponse<List<Object>>> listarMisDespachos();

    @PATCH("despachos/{id}/iniciar-ruta")
    Call<ApiResponse<Object>> iniciarRutaDespacho(@Path("id") int id, @Body Object request);

    @PATCH("despachos/{id}/completar-entrega")
    Call<ApiResponse<Object>> completarEntregaDespacho(@Path("id") int id, @Body Object request);

    @POST("repartidor/ubicacion-actual")
    Call<ApiResponse<Object>> transmitirUbicacionRepartidor(@Body Object request);

    @DELETE("despachos/{id}")
    Call<ApiResponse<Object>> anularDespacho(@Path("id") int id);

    // =============================================================
    // 5. CONTROL DE SUPERVISIÓN (JEFA Y PROMOTORES)
    // =============================================================
    @GET("control/promotores")
    Call<ApiResponse<List<Usuario>>> listarPromotoresControl();

    @POST("control/asignar-punto")
    Call<ApiResponse<Object>> asignarPromotorPunto(@Body Object request);

    @GET("control/promotores/{id}/puntos")
    Call<ApiResponse<List<PuntoVenta>>> listarPuntosPromotor(@Path("id") int id);

    @GET("control/asistencias-puntos")
    Call<ApiResponse<List<Asistencia>>> supervisarAsistenciasPuntos(@Query("fecha") String fecha);

    @GET("control/alertas-stock")
    Call<ApiResponse<List<Alerta>>> listarAlertasStockControl(@Query("estado") String estado);

    @PATCH("control/alertas-stock/{id}/atender")
    Call<ApiResponse<Object>> atenderAlertaStock(@Path("id") int id, @Body Object request);

    @GET("control/dashboard-kpis")
    Call<ApiResponse<Object>> obtenerDashboardKpis();

    @DELETE("control/desasignar-punto/{id}")
    Call<ApiResponse<Object>> desasignarPromotorPunto(@Path("id") int id);

    // =============================================================
    // 6. CATÁLOGO AUXILIAR
    // =============================================================
    @GET("categorias-locales")
    Call<ApiResponse<List<String>>> listarCategoriasLocales();

    @GET("productos/destacados")
    Call<ApiResponse<List<Producto>>> listarProductosDestacados();

    @GET("geografia/distritos-activos")
    Call<ApiResponse<List<Object>>> listarDistritosActivos();

    // -------------------------------------------------------------
    // ASISTENCIAS Y STOCK (Módulos adicionales)
    // -------------------------------------------------------------
    @POST("asistencias")
    Call<ApiResponse<Asistencia>> registrarAsistencia(@Body Asistencia asistencia);

    @GET("asistencias/usuario/{idUsuario}")
    Call<ApiResponse<List<Asistencia>>> listarAsistenciasUsuario(@Path("idUsuario") int idUsuario);

    @GET("stock/punto/{idPuntosVenta}")
    Call<ApiResponse<List<StockRegistro>>> obtenerStockPuntoVenta(@Path("idPuntosVenta") int idPuntosVenta);

    @GET("productos")
    Call<ApiResponse<List<Producto>>> listarProductos();
}