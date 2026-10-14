package com.piccolifield.data.remote.api;

import java.util.List;

import com.piccolifield.data.model.Usuario;
import com.piccolifield.data.remote.dto.request.LoginRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.dto.EstadoUsuarioRequest;
import com.piccolifield.data.remote.dto.FcmTokenRequest;
import com.piccolifield.data.remote.dto.response.LoginResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Endpoint para autenticar usuario contra la BD
    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> iniciarSesion(@Body LoginRequest request);

    // 1. Obtener perfil del usuario autenticado
    @GET("perfil")
    Call<ApiResponse<Usuario>> obtenerPerfil();

    // 2. Obtener foto de perfil
    @GET("perfil/foto")
    Call<ResponseBody> obtenerFotoPerfil();

    // 3. Actualizar token FCM para notificaciones e ID de dispositivo
    @PUT("perfil/fcm-token")
    Call<ApiResponse<Object>> actualizarFcmToken(@Body FcmTokenRequest request);

    // 4. Listar usuarios (con filtros opcionales de rol y estado)
    @GET("usuarios")
    Call<ApiResponse<List<Usuario>>> listarUsuarios(
            @Query("rol") String rol,
            @Query("estado") String estado
    );

    // 5. Cambiar el estado de un usuario por su ID
    @PATCH("usuarios/{idUsuario}/estado")
    Call<ApiResponse<Object>> cambiarEstadoUsuario(
            @Path("idUsuario") int idUsuario,
            @Body EstadoUsuarioRequest request
    );
}