package com.piccolifield.data.repository;

import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.request.MarcarAsistenciaRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.dto.response.AsistenciaResponse;

import retrofit2.Callback;

public class AsistenciaRepository {

    private final ApiService apiService;

    public AsistenciaRepository() {
        this.apiService = RetrofitClient.createService();
    }

    public AsistenciaRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void registrarAsistencia(MarcarAsistenciaRequest request, Callback<ApiResponse<Object>> callback) {
        apiService.registrarAsistencia(request).enqueue(callback);
    }

    public void obtenerUltimaAsistencia(Callback<ApiResponse<AsistenciaResponse>> callback) {
        apiService.obtenerUltimaAsistencia().enqueue(callback);
    }
}
