package com.piccolifield.data.remote.api;

import java.util.List;

import com.piccolifield.data.model.Usuario;
import com.piccolifield.data.remote.dto.ApiResponse;
import com.piccolifield.data.remote.dto.EstadoUsuarioRequest;
import com.piccolifield.data.remote.dto.FcmTokenRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // 1. Obtener perfil del usuario autenticado
    @GET("api/perfil")
    Call<ApiResponse<Usuario>> obtenerPerfil();

    // 2. Obtener foto de perfil
    @GET("api/perfil/foto")
    Call<ResponseBody> obtenerFotoPerfil();

    // 3. Actualizar token FCM para notificaciones e ID de dispositivo
    @PUT("api/perfil/fcm-token")
    Call<ApiResponse<Object>> actualizarFcmToken(@Body FcmTokenRequest request);

    // 4. Listar usuarios (con filtros opcionales de rol y estado)
    @GET("api/usuarios")
    Call<ApiResponse<List<Usuario>>> listarUsuarios(
            @Query("rol") String rol,
            @Query("estado") String estado
    );

    // 5. Cambiar el estado de un usuario por su ID
    @PATCH("api/usuarios/{idUsuario}/estado")
    Call<ApiResponse<Object>> cambiarEstadoUsuario(
            @Path("idUsuario") int idUsuario,
            @Body EstadoUsuarioRequest request
    );
}