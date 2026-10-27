package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class ReasignarImpulsadorRequest {
    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("idPuntosVenta")
    private int idPuntosVenta;

    @SerializedName("motivo")
    private String motivo;

    public ReasignarImpulsadorRequest(int idUsuario, int idPuntosVenta, String motivo) {
        this.idUsuario = idUsuario;
        this.idPuntosVenta = idPuntosVenta;
        this.motivo = motivo;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdPuntosVenta() { return idPuntosVenta; }
    public void setIdPuntosVenta(int idPuntosVenta) { this.idPuntosVenta = idPuntosVenta; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}