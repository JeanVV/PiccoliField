package com.piccolifield.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class EstadoRequest {

    @SerializedName("estado")
    private String estado;

    public EstadoRequest() {}

    public EstadoRequest(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}