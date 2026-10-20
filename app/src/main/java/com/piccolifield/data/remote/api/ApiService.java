package com.piccolifield.data.remote.api;

import java.util.List;

import com.piccolifield.data.model.AlertaDetalle;
import com.piccolifield.data.model.AsistenciaResumen;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.model.ResumenDashboard;
import com.piccolifield.data.model.TurnoHistorial;
import com.piccolifield.data.model.Usuario;
import com.piccolifield.data.model.VendedorDetalle;
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
    Call<ApiResponse<List<PuntoVenta>>> listarPuntosVenta(
            @Query("search") String search,
            @Query("categoria") String categoria,
            @Query("distrito") Integer distrito,
            @Query("estado") String estado
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

    @GET("supervision/alertas/{id}")
    Call<ApiResponse<AlertaDetalle>> obtenerDetalleAlerta(
            @Path("id") int idAlerta
    );

    @GET("supervision/alertas")
    Call<ApiResponse<List<Alerta>>> listarAlertasSupervision();

    @GET("supervision/puntos-venta")
    Call<ApiResponse<List<com.piccolifield.model.SupervisionPuntoVenta>>> listarPuntosVentaSupervision();
}