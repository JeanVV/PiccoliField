package com.piccolifield.ui.asistencia;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.dto.response.AsistenciaResponse;
import com.piccolifield.data.repository.AsistenciaRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonitoreoMapaViewModel extends ViewModel {

    public static class SedeInfo {
        private final String nombre;
        private final double latitud;
        private final double longitud;
        private final float radioMetros;

        public SedeInfo(String nombre, double latitud, double longitud, float radioMetros) {
            this.nombre = nombre;
            this.latitud = latitud;
            this.longitud = longitud;
            this.radioMetros = radioMetros;
        }

        public String getNombre() { return nombre; }
        public double getLatitud() { return latitud; }
        public double getLongitud() { return longitud; }
        public float getRadioMetros() { return radioMetros; }
    }

    private final AsistenciaRepository repository;

    private final MutableLiveData<SedeInfo> sedeAsignada = new MutableLiveData<>();
    private final MutableLiveData<LatLng> ubicacionUsuario = new MutableLiveData<>();
    private final MutableLiveData<Integer> distanciaMetros = new MutableLiveData<>();
    private final MutableLiveData<Boolean> enGeocerca = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeEstado = new MutableLiveData<>();
    private final MutableLiveData<AsistenciaResponse> ultimaAsistencia = new MutableLiveData<>();
    private final MutableLiveData<Boolean> esAdministrador = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> colaboradoresEnZonaCount = new MutableLiveData<>(0);

    public MonitoreoMapaViewModel() {
        this.repository = new AsistenciaRepository();
        inicializarSedeAsignada();
    }

    private void inicializarSedeAsignada() {
        // Sede por defecto: Sede Principal Chiclayo
        SedeInfo sede = new SedeInfo("Sede Principal Chiclayo - Piccoli Cocktail", -6.7714, -79.8409, 100f);
        sedeAsignada.setValue(sede);
    }

    public LiveData<SedeInfo> getSedeAsignada() { return sedeAsignada; }
    public LiveData<LatLng> getUbicacionUsuario() { return ubicacionUsuario; }
    public LiveData<Integer> getDistanciaMetros() { return distanciaMetros; }
    public LiveData<Boolean> getEnGeocerca() { return enGeocerca; }
    public LiveData<String> getMensajeEstado() { return mensajeEstado; }
    public LiveData<AsistenciaResponse> getUltimaAsistencia() { return ultimaAsistencia; }
    public LiveData<Boolean> getEsAdministrador() { return esAdministrador; }
    public LiveData<Integer> getColaboradoresEnZonaCount() { return colaboradoresEnZonaCount; }

    public void setEsAdministrador(boolean esAdmin) {
        esAdministrador.setValue(esAdmin);
        if (esAdmin) {
            SedeInfo sede = sedeAsignada.getValue();
            String nombre = sede != null ? sede.getNombre() : "Sede Principal";
            mensajeEstado.setValue("Monitoreo de asistencia y personal en " + nombre);
        }
    }

    public void setColaboradoresEnZonaCount(int count) {
        colaboradoresEnZonaCount.setValue(count);
        Boolean isAdmin = esAdministrador.getValue();
        if (isAdmin != null && isAdmin) {
            SedeInfo sede = sedeAsignada.getValue();
            String nombre = sede != null ? sede.getNombre() : "Sede Principal";
            mensajeEstado.setValue("Supervisando " + count + " colaboradores en " + nombre);
        }
    }

    public void actualizarUbicacionUsuario(double userLat, double userLng) {
        LatLng userLatLng = new LatLng(userLat, userLng);
        ubicacionUsuario.setValue(userLatLng);

        SedeInfo sede = sedeAsignada.getValue();
        if (sede != null) {
            float[] results = new float[1];
            Location.distanceBetween(userLat, userLng, sede.getLatitud(), sede.getLongitud(), results);
            int dist = Math.round(results[0]);
            distanciaMetros.setValue(dist);

            boolean dentro = dist <= sede.getRadioMetros();
            enGeocerca.setValue(dentro);

            Boolean isAdmin = esAdministrador.getValue();
            if (isAdmin != null && isAdmin) {
                int count = colaboradoresEnZonaCount.getValue() != null ? colaboradoresEnZonaCount.getValue() : 0;
                mensajeEstado.setValue("Supervisando " + count + " colaboradores en la zona (" + dist + "m de la sede)");
            } else {
                AsistenciaResponse asistencia = ultimaAsistencia.getValue();
                if (asistencia != null) {
                    String horaStr = asistencia.getHora() != null ? asistencia.getHora() : "";
                    mensajeEstado.setValue("Asistencia registrada (" + horaStr + ")");
                } else if (dentro) {
                    mensajeEstado.setValue("Estás dentro de la zona de asistencia. Listo para marcar.");
                } else {
                    mensajeEstado.setValue("Te encuentras fuera del rango de asistencia (a " + dist + " metros de distancia)");
                }
            }
        }
    }

    public void cargarUltimaAsistencia() {
        repository.obtenerUltimaAsistencia(new Callback<ApiResponse<AsistenciaResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AsistenciaResponse>> call, @NonNull Response<ApiResponse<AsistenciaResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    AsistenciaResponse asistencia = response.body().getData();
                    ultimaAsistencia.setValue(asistencia);
                    if (ubicacionUsuario.getValue() != null) {
                        actualizarUbicacionUsuario(ubicacionUsuario.getValue().latitude, ubicacionUsuario.getValue().longitude);
                    } else {
                        mensajeEstado.setValue("Asistencia registrada");
                    }
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
}
