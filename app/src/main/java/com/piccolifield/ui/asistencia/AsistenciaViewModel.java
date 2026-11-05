package com.piccolifield.ui.asistencia;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.piccolifield.data.remote.dto.request.MarcarAsistenciaRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.dto.response.AsistenciaResponse;
import com.piccolifield.data.repository.AsistenciaRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsistenciaViewModel extends ViewModel {

    private final AsistenciaRepository repository;
    private final MutableLiveData<AsistenciaUiState> uiState = new MutableLiveData<>(AsistenciaUiState.idle());
    private final MutableLiveData<AsistenciaResponse> ultimaAsistencia = new MutableLiveData<>();

    public AsistenciaViewModel() {
        this.repository = new AsistenciaRepository();
    }

    public AsistenciaViewModel(AsistenciaRepository repository) {
        this.repository = repository;
    }

    public LiveData<AsistenciaUiState> getUiState() {
        return uiState;
    }

    public LiveData<AsistenciaResponse> getUltimaAsistencia() {
        return ultimaAsistencia;
    }

    public void marcarAsistencia(MarcarAsistenciaRequest request) {
        uiState.setValue(AsistenciaUiState.loading());
        repository.registrarAsistencia(request, new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    String msg = apiResponse.getMessage();
                    if (msg == null || msg.trim().isEmpty()) {
                        msg = "Asistencia registrada correctamente";
                    }

                    if (apiResponse.isStatus()) {
                        uiState.setValue(AsistenciaUiState.success(msg));
                        cargarUltimaAsistencia();
                    } else {
                        uiState.setValue(AsistenciaUiState.error(msg));
                    }
                } else {
                    String errorMsg = "Error al registrar asistencia (" + response.code() + ")";
                    uiState.setValue(AsistenciaUiState.error(errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                String errorMsg = t.getMessage() != null ? t.getMessage() : "Error de red al registrar asistencia";
                uiState.setValue(AsistenciaUiState.error(errorMsg));
            }
        });
    }

    public void cargarUltimaAsistencia() {
        repository.obtenerUltimaAsistencia(new Callback<ApiResponse<AsistenciaResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AsistenciaResponse>> call, @NonNull Response<ApiResponse<AsistenciaResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ultimaAsistencia.setValue(response.body().getData());
                } else {
                    ultimaAsistencia.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AsistenciaResponse>> call, @NonNull Throwable t) {
                ultimaAsistencia.setValue(null);
            }
        });
    }

    public void resetUiState() {
        uiState.setValue(AsistenciaUiState.idle());
    }
}
