package com.piccolifield.ui.asistencia;

public class AsistenciaUiState {

    public enum Estado {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Estado estado;
    private final String mensaje;

    public AsistenciaUiState(Estado estado, String mensaje) {
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public Estado getEstado() {
        return estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public static AsistenciaUiState idle() {
        return new AsistenciaUiState(Estado.IDLE, null);
    }

    public static AsistenciaUiState loading() {
        return new AsistenciaUiState(Estado.LOADING, null);
    }

    public static AsistenciaUiState success(String mensaje) {
        return new AsistenciaUiState(Estado.SUCCESS, mensaje);
    }

    public static AsistenciaUiState error(String mensaje) {
        return new AsistenciaUiState(Estado.ERROR, mensaje);
    }
}
