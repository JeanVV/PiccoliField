package com.piccolifield.data.repository;

import com.piccolifield.data.model.ImpulsadorDashboard;
import com.piccolifield.data.model.Incidencia;
import com.piccolifield.data.model.ResumenDashboard;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Alerta;
import com.piccolifield.model.SupervisionPuntoVenta;
import com.piccolifield.model.Vendedor;

import java.util.List;

import retrofit2.Callback;

public class SupervisionRepository {

    private final ApiService apiService;

    public SupervisionRepository() {
        this.apiService = RetrofitClient.createService();
    }

    public void obtenerResumenDashboard(Callback<ApiResponse<ResumenDashboard>> callback) {
        apiService.obtenerResumenDashboard().enqueue(callback);
    }

    public void listarMejoresVendedores(Callback<ApiResponse<List<Vendedor>>> callback) {
        apiService.listarMejoresVendedores().enqueue(callback);
    }

    public void listarAlertas(Callback<ApiResponse<List<Alerta>>> callback) {
        apiService.listarAlertasSupervision().enqueue(callback);
    }

    public void listarPuntosVenta(Callback<ApiResponse<List<SupervisionPuntoVenta>>> callback) {
        apiService.listarPuntosVentaSupervision(null).enqueue(callback);
    }

    public void listarIncidencias(Callback<ApiResponse<List<Incidencia>>> callback) {
        apiService.listarIncidenciasSupervision(null).enqueue(callback);
    }

    public void obtenerDashboardImpulsador(Callback<ApiResponse<ImpulsadorDashboard>> callback) {
        apiService.obtenerDashboardImpulsador().enqueue(callback);
    }
}