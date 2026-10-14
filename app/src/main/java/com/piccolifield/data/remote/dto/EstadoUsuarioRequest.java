package com.piccolifield.data.remote.dto;

public class EstadoUsuarioRequest {
    private String estado;

    public EstadoUsuarioRequest(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}