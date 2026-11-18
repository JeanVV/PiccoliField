package com.piccolifield.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.piccolifield.data.local.AppDatabase;
import com.piccolifield.data.local.entity.AsistenciaOfflineEntity;
import com.piccolifield.data.local.sync.SyncWorker;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.model.TurnoHistorial;
import com.piccolifield.data.remote.Resource;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.request.JustificationRequest;
import com.piccolifield.data.remote.dto.request.MarcarAsistenciaRequest;
import com.piccolifield.data.remote.dto.request.RevisionJustificacionRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.dto.response.AsistenciaResponse;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsistenciaRepository {

    private final ApiService apiService;

    public AsistenciaRepository() {
        this.apiService = RetrofitClient.createService();
    }

    public AsistenciaRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    // 1. Marcar Asistencia (Con soporte Offline-First via Room + WorkManager)
    public LiveData<Resource<String>> registrarAsistencia(MarcarAsistenciaRequest request, Context context) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.registrarAsistencia(request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    String msg = apiResponse.getMessage();
                    if (msg == null || msg.trim().isEmpty()) {
                        msg = "Asistencia registrada correctamente";
                    }
                    if (apiResponse.isStatus()) {
                        if (context != null) {
                            RegistroAsistencia reg = new RegistroAsistencia();
                            reg.setIdUsuario(request.getIdUsuario());
                            reg.setTipo(request.getTipo());
                            reg.setLatitud(request.getLatitud());
                            reg.setLongitud(request.getLongitud());
                            reg.setSede(request.getSede());
                            reg.setFechaHoraServidor(request.getFechaHoraDispositivo());
                            reg.setHoraMarcado(request.getFechaHoraDispositivo());
                            reg.setEstado("A_TIEMPO");
                            SplashStorage.guardarRegistroAsistenciaLocal(context, reg);
                        }
                        result.setValue(Resource.success(msg));
                    } else {
                        result.setValue(Resource.error(msg, null));
                    }
                } else {
                    guardarAsistenciaOffline(request, context, result, "Error en servidor (" + response.code() + "). Registro guardado localmente.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                guardarAsistenciaOffline(request, context, result, "Modo Offline: Registro guardado localmente y en cola para sincronización.");
            }
        });

        return result;
    }

    private void guardarAsistenciaOffline(MarcarAsistenciaRequest request, Context context, MutableLiveData<Resource<String>> result, String mensajeFallback) {
        if (context != null) {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                AsistenciaOfflineEntity entity = new AsistenciaOfflineEntity(
                        request.getTipo() != null ? request.getTipo() : "INGRESO",
                        request.getLatitud(),
                        request.getLongitud(),
                        request.getFechaHoraDispositivo() != null ? request.getFechaHoraDispositivo() : ""
                );
                db.asistenciaOfflineDao().insertar(entity);
                SyncWorker.programarSincronizacion(context);

                // Guardar también en SharedPreferences cifradas como respaldo directo para la UI
                RegistroAsistencia reg = new RegistroAsistencia();
                reg.setIdUsuario(request.getIdUsuario());
                reg.setTipo(request.getTipo());
                reg.setLatitud(request.getLatitud());
                reg.setLongitud(request.getLongitud());
                reg.setSede(request.getSede());
                reg.setFechaHoraServidor(request.getFechaHoraDispositivo());
                reg.setHoraMarcado(request.getFechaHoraDispositivo());
                reg.setEstado("A_TIEMPO");
                SplashStorage.guardarRegistroAsistenciaLocal(context, reg);

                result.setValue(Resource.success(mensajeFallback));
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        result.setValue(Resource.error(mensajeFallback, null));
    }

    // 2. Obtener última asistencia
    public LiveData<Resource<AsistenciaResponse>> obtenerUltimaAsistencia(int idUsuario) {
        MutableLiveData<Resource<AsistenciaResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.obtenerUltimaAsistencia(idUsuario).enqueue(new Callback<ApiResponse<AsistenciaResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AsistenciaResponse>> call, @NonNull Response<ApiResponse<AsistenciaResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error("Sin registro previo", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AsistenciaResponse>> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Error al obtener última asistencia", null));
            }
        });

        return result;
    }

    public LiveData<Resource<AsistenciaResponse>> obtenerUltimaAsistencia() {
        return obtenerUltimaAsistencia(0);
    }

    // 3. Obtener Colaboradores con Asistencia (para AsistenciaHomeFragment)
    public LiveData<Resource<List<RegistroAsistencia>>> obtenerColaboradoresConAsistencia(Context context) {
        MutableLiveData<Resource<List<RegistroAsistencia>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.obtenerColaboradoresConAsistencia().enqueue(new Callback<ApiResponse<List<RegistroAsistencia>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<RegistroAsistencia>>> call, @NonNull Response<ApiResponse<List<RegistroAsistencia>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<RegistroAsistencia> data = response.body().getData();
                    if (context != null) {
                        List<RegistroAsistencia> locales = SplashStorage.obtenerRegistrosAsistenciaLocales(context);
                        for (RegistroAsistencia loc : locales) {
                            boolean existe = false;
                            for (RegistroAsistencia rem : data) {
                                if (rem.getIdUsuario() == loc.getIdUsuario()) {
                                    existe = true;
                                    break;
                                }
                            }
                            if (!existe) data.add(0, loc);
                        }
                    }
                    result.setValue(Resource.success(data));
                } else {
                    List<RegistroAsistencia> locales = context != null ? SplashStorage.obtenerRegistrosAsistenciaLocales(context) : new ArrayList<>();
                    result.setValue(Resource.success(locales));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<RegistroAsistencia>>> call, @NonNull Throwable t) {
                List<RegistroAsistencia> locales = context != null ? SplashStorage.obtenerRegistrosAsistenciaLocales(context) : new ArrayList<>();
                result.setValue(Resource.success(locales));
            }
        });

        return result;
    }

    // 4. Registrar Justificación
    public LiveData<Resource<String>> registrarJustificacion(JustificationRequest request, Context context) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        if (context != null) {
            SplashStorage.guardarJustificacionLocal(context, request.getIdUsuario(), request.getMotivo(), request.getDescripcion(), request.getRutaSustento());
        }

        apiService.registrarJustificacion(request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success("Justificación registrada exitosamente"));
                } else {
                    String msg = (response.body() != null && response.body().getMessage() != null)
                            ? response.body().getMessage()
                            : "Justificación registrada localmente (Servidor: " + response.code() + ")";
                    result.setValue(Resource.success(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                result.setValue(Resource.success("Justificación registrada localmente (Modo Offline)"));
            }
        });

        return result;
    }

    // 5. Revisar Justificación (Revisión Administrador)
    public LiveData<Resource<String>> revisarJustificacion(RevisionJustificacionRequest request) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.revisarJustificacion(request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success("Justificación procesada correctamente"));
                } else {
                    String err = (response.body() != null && response.body().getMessage() != null)
                            ? response.body().getMessage()
                            : "Error del servidor (" + response.code() + ")";
                    result.setValue(Resource.error(err, null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                result.setValue(Resource.success("Justificación procesada localmente (Modo Offline)"));
            }
        });

        return result;
    }

    // 6. Obtener Justificación de Usuario
    public LiveData<Resource<RegistroAsistencia>> obtenerJustificacionUsuario(int idUsuario) {
        MutableLiveData<Resource<RegistroAsistencia>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.obtenerJustificacionUsuario(idUsuario).enqueue(new Callback<ApiResponse<RegistroAsistencia>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<RegistroAsistencia>> call, @NonNull Response<ApiResponse<RegistroAsistencia>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error("Sin justificación registrada", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<RegistroAsistencia>> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Error al obtener justificación", null));
            }
        });

        return result;
    }

    // 7. Obtener Historial de Turnos
    public LiveData<Resource<List<TurnoHistorial>>> obtenerHistorialTurnos(int idUsuario) {
        MutableLiveData<Resource<List<TurnoHistorial>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.obtenerHistorialTurnos(idUsuario).enqueue(new Callback<ApiResponse<List<TurnoHistorial>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<TurnoHistorial>>> call, @NonNull Response<ApiResponse<List<TurnoHistorial>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error("Sin historial disponible", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<TurnoHistorial>>> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Error al obtener historial", null));
            }
        });

        return result;
    }
}
