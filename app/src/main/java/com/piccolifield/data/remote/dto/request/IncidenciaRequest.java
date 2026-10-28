package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class IncidenciaRequest {
    @SerializedName("tipo")
    private String tipo;

    @SerializedName("severidad")
    private String severidad;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("foto_base64")
    private String fotoBase64;

    public IncidenciaRequest(String tipo, String severidad, String descripcion, String fotoBase64) {
        this.tipo = tipo;
        this.severidad = severidad;
        this.descripcion = descripcion;
        this.fotoBase64 = fotoBase64;
    }
}