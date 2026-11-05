package com.piccolifield.data.repository;

import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.request.DispositivoRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import retrofit2.Callback;

public class DispositivoRepository {

    private final ApiService apiService;

    public DispositivoRepository() {
        apiService = RetrofitClient.createService();
    }

    public void actualizarDispositivo(String token, String deviceName, Callback<ApiResponse<Object>> callback) {
        DispositivoRequest request = new DispositivoRequest(token, "ANDROID", deviceName);
        apiService.actualizarDispositivo(request).enqueue(callback);
    }
}