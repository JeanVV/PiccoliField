package com.piccolifield.ui.supervisionPdv;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.piccolifield.data.model.ImpulsadorDashboard;
import com.piccolifield.data.model.Incidencia;
import com.piccolifield.data.model.ResumenDashboard;
import com.piccolifield.data.remote.Resource;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.repository.SupervisionRepository;
import com.piccolifield.model.Alerta;
import com.piccolifield.model.SupervisionPuntoVenta;
import com.piccolifield.model.Vendedor;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupervisionViewModel extends ViewModel {

    private final SupervisionRepository repository;

    private final MutableLiveData<Resource<ResumenDashboard>> resumenState = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Vendedor>>> vendedoresState = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Alerta>>> alertasState = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<SupervisionPuntoVenta>>> puntosVentaState = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Incidencia>>> incidenciasState = new MutableLiveData<>();
    private final MutableLiveData<Resource<ImpulsadorDashboard>> impulsadorDashboardState = new MutableLiveData<>();

    public SupervisionViewModel() {
        this.repository = new SupervisionRepository();
    }

    public LiveData<Resource<ResumenDashboard>> getResumenState() { return resumenState; }
    public LiveData<Resource<List<Vendedor>>> getVendedoresState() { return vendedoresState; }
    public LiveData<Resource<List<Alerta>>> getAlertasState() { return alertasState; }
    public LiveData<Resource<List<SupervisionPuntoVenta>>> getPuntosVentaState() { return puntosVentaState; }
    public LiveData<Resource<List<Incidencia>>> getIncidenciasState() { return incidenciasState; }
    public LiveData<Resource<ImpulsadorDashboard>> getImpulsadorDashboardState() { return impulsadorDashboardState; }

    public void cargarResumen() {
        resumenState.setValue(Resource.loading(null));
        repository.obtenerResumenDashboard(new Callback<ApiResponse<ResumenDashboard>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ResumenDashboard>> call, @NonNull Response<ApiResponse<ResumenDashboard>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    resumenState.setValue(Resource.success(response.body().getData()));
                } else {
                    resumenState.setValue(Resource.error("Error " + response.code(), null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ResumenDashboard>> call, @NonNull Throwable t) {
                resumenState.setValue(Resource.error("Error de red: " + t.getMessage(), null));
            }
        });
    }

    public void cargarMejoresVendedores() {
        vendedoresState.setValue(Resource.loading(null));
        repository.listarMejoresVendedores(new Callback<ApiResponse<List<Vendedor>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Vendedor>>> call, @NonNull Response<ApiResponse<List<Vendedor>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    vendedoresState.setValue(Resource.success(response.body().getData()));
                } else {
                    vendedoresState.setValue(Resource.error("Error al cargar vendedores", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Vendedor>>> call, @NonNull Throwable t) {
                vendedoresState.setValue(Resource.error("Error de red: " + t.getMessage(), null));
            }
        });
    }

    public void cargarAlertas() {
        alertasState.setValue(Resource.loading(null));
        repository.listarAlertas(new Callback<ApiResponse<List<Alerta>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Alerta>>> call, @NonNull Response<ApiResponse<List<Alerta>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    alertasState.setValue(Resource.success(response.body().getData()));
                } else {
                    alertasState.setValue(Resource.error("Error al cargar alertas", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Alerta>>> call, @NonNull Throwable t) {
                alertasState.setValue(Resource.error("Error de red: " + t.getMessage(), null));
            }
        });
    }

    public void cargarPuntosVenta() {
        puntosVentaState.setValue(Resource.loading(null));
        repository.listarPuntosVenta(new Callback<ApiResponse<List<SupervisionPuntoVenta>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<SupervisionPuntoVenta>>> call, @NonNull Response<ApiResponse<List<SupervisionPuntoVenta>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    puntosVentaState.setValue(Resource.success(response.body().getData()));
                } else {
                    puntosVentaState.setValue(Resource.error("Error al cargar puntos de venta", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SupervisionPuntoVenta>>> call, @NonNull Throwable t) {
                puntosVentaState.setValue(Resource.error("Error de red: " + t.getMessage(), null));
            }
        });
    }

    public void cargarIncidencias() {
        incidenciasState.setValue(Resource.loading(null));
        repository.listarIncidencias(new Callback<ApiResponse<List<Incidencia>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Incidencia>>> call, @NonNull Response<ApiResponse<List<Incidencia>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    incidenciasState.setValue(Resource.success(response.body().getData()));
                } else {
                    incidenciasState.setValue(Resource.error("Error al cargar incidencias", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Incidencia>>> call, @NonNull Throwable t) {
                incidenciasState.setValue(Resource.error("Error de red: " + t.getMessage(), null));
            }
        });
    }

    public void cargarDashboardImpulsador() {
        impulsadorDashboardState.setValue(Resource.loading(null));
        repository.obtenerDashboardImpulsador(new Callback<ApiResponse<ImpulsadorDashboard>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ImpulsadorDashboard>> call, @NonNull Response<ApiResponse<ImpulsadorDashboard>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    impulsadorDashboardState.setValue(Resource.success(response.body().getData()));
                } else {
                    impulsadorDashboardState.setValue(Resource.error("Error al cargar dashboard", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ImpulsadorDashboard>> call, @NonNull Throwable t) {
                impulsadorDashboardState.setValue(Resource.error("Error de red: " + t.getMessage(), null));
            }
        });
    }
}