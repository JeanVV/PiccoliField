package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class ReportarIncidenciaRequest {
    @SerializedName("tipo")
    private String tipo;

    @SerializedName("severidad")
    private String severidad;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("idPuntosVenta")
    private Integer idPuntosVenta;

    @SerializedName("foto_base64")
    private String fotoBase64;

    public ReportarIncidenciaRequest(String tipo, String severidad, String descripcion, Integer idPuntosVenta, String fotoBase64) {
        this.tipo = tipo;
        this.severidad = severidad;
        this.descripcion = descripcion;
        this.idPuntosVenta = idPuntosVenta;
        this.fotoBase64 = fotoBase64;
    }
}