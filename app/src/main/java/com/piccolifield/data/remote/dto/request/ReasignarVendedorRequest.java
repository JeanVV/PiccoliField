package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class ReasignarVendedorRequest {
    @SerializedName("idPuntosVenta")
    private int idPuntosVenta;

    @SerializedName("motivo")
    private String motivo;

    public ReasignarVendedorRequest(int idPuntosVenta, String motivo) {
        this.idPuntosVenta = idPuntosVenta;
        this.motivo = motivo;
    }

    public int getIdPuntosVenta() { return idPuntosVenta; }
    public String getMotivo() { return motivo; }
}