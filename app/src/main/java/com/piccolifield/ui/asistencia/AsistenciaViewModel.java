package com.piccolifield.ui.asistencia;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.model.TurnoHistorial;
import com.piccolifield.data.remote.Resource;
import com.piccolifield.data.remote.dto.request.JustificationRequest;
import com.piccolifield.data.remote.dto.request.MarcarAsistenciaRequest;
import com.piccolifield.data.remote.dto.request.RevisionJustificacionRequest;
import com.piccolifield.data.remote.dto.response.AsistenciaResponse;
import com.piccolifield.data.repository.AsistenciaRepository;

import java.util.List;

public class AsistenciaViewModel extends ViewModel {

    private final AsistenciaRepository repository;
    private final MutableLiveData<AsistenciaUiState> uiState = new MutableLiveData<>(AsistenciaUiState.idle());
    private final MutableLiveData<AsistenciaResponse> ultimaAsistencia = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<RegistroAsistencia>>> colaboradoresLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> justificacionResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> revisionJustificacionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<RegistroAsistencia>> justificacionUsuarioLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<TurnoHistorial>>> historialTurnosLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<com.piccolifield.data.model.SedeDto>> sedePrincipalLiveData = new MutableLiveData<>();

    public AsistenciaViewModel() {
        this.repository = new AsistenciaRepository();
    }

    public AsistenciaViewModel(AsistenciaRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<com.piccolifield.data.model.SedeDto>> getSedePrincipalLiveData() {
        return sedePrincipalLiveData;
    }

    public void cargarSedePrincipal() {
        LiveData<Resource<com.piccolifield.data.model.SedeDto>> res = repository.obtenerSedePrincipal();
        res.observeForever(resource -> {
            if (resource != null) {
                sedePrincipalLiveData.setValue(resource);
            }
        });
    }

    public LiveData<AsistenciaUiState> getUiState() {
        return uiState;
    }

    public LiveData<AsistenciaResponse> getUltimaAsistencia() {
        return ultimaAsistencia;
    }

    public LiveData<Resource<List<RegistroAsistencia>>> getColaboradoresLiveData() {
        return colaboradoresLiveData;
    }

    public LiveData<Resource<String>> getJustificacionResultLiveData() {
        return justificacionResultLiveData;
    }

    public LiveData<Resource<String>> getRevisionJustificacionLiveData() {
        return revisionJustificacionLiveData;
    }

    public LiveData<Resource<RegistroAsistencia>> getJustificacionUsuarioLiveData() {
        return justificacionUsuarioLiveData;
    }

    public LiveData<Resource<List<TurnoHistorial>>> getHistorialTurnosLiveData() {
        return historialTurnosLiveData;
    }

    public void marcarAsistencia(MarcarAsistenciaRequest request, Context context) {
        uiState.setValue(AsistenciaUiState.loading());
        LiveData<Resource<String>> res = repository.registrarAsistencia(request, context);
        res.observeForever(resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    uiState.setValue(AsistenciaUiState.loading());
                    break;
                case SUCCESS:
                    String msg = resource.data != null ? resource.data : "Asistencia registrada correctamente";
                    uiState.setValue(AsistenciaUiState.success(msg));
                    cargarUltimaAsistencia(request.getIdUsuario());
                    break;
                case ERROR:
                    String err = resource.message != null ? resource.message : "Error al registrar asistencia";
                    uiState.setValue(AsistenciaUiState.error(err));
                    break;
            }
        });
    }

    public void marcarAsistencia(MarcarAsistenciaRequest request) {
        marcarAsistencia(request, null);
    }

    public void cargarUltimaAsistencia(int idUsuario) {
        LiveData<Resource<AsistenciaResponse>> res = repository.obtenerUltimaAsistencia(idUsuario);
        res.observeForever(resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                ultimaAsistencia.setValue(resource.data);
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                ultimaAsistencia.setValue(null);
            }
        });
    }

    public void cargarColaboradoresConAsistencia(Context context) {
        LiveData<Resource<List<RegistroAsistencia>>> res = repository.obtenerColaboradoresConAsistencia(context);
        res.observeForever(resource -> {
            if (resource != null) {
                colaboradoresLiveData.setValue(resource);
            }
        });
    }

    public void registrarJustificacion(JustificationRequest request, Context context) {
        LiveData<Resource<String>> res = repository.registrarJustificacion(request, context);
        res.observeForever(resource -> {
            if (resource != null) {
                justificacionResultLiveData.setValue(resource);
            }
        });
    }

    public void revisarJustificacion(RevisionJustificacionRequest request) {
        LiveData<Resource<String>> res = repository.revisarJustificacion(request);
        res.observeForever(resource -> {
            if (resource != null) {
                revisionJustificacionLiveData.setValue(resource);
            }
        });
    }

    public void cargarJustificacionUsuario(int idUsuario) {
        LiveData<Resource<RegistroAsistencia>> res = repository.obtenerJustificacionUsuario(idUsuario);
        res.observeForever(resource -> {
            if (resource != null) {
                justificacionUsuarioLiveData.setValue(resource);
            }
        });
    }

    public void cargarHistorialTurnos(int idUsuario) {
        LiveData<Resource<List<TurnoHistorial>>> res = repository.obtenerHistorialTurnos(idUsuario);
        res.observeForever(resource -> {
            if (resource != null) {
                historialTurnosLiveData.setValue(resource);
            }
        });
    }

    public void resetUiState() {
        uiState.setValue(AsistenciaUiState.idle());
    }

    public void resetRevisionJustificacionLiveData() {
        revisionJustificacionLiveData.setValue(null);
    }
}
