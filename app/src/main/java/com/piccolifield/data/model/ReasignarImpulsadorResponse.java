package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class ReasignarImpulsadorResponse {
    @SerializedName("idHistorial")
    private int idHistorial;

    @SerializedName("idPuntosVenta")
    private int idPuntosVenta;

    @SerializedName("puntoVenta")
    private String puntoVenta;

    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("impulsador")
    private String impulsador;

    @SerializedName("motivo")
    private String motivo;

    @SerializedName("fecha_inicio")
    private String fechaInicio;

    public int getIdHistorial() { return idHistorial; }
    public void setIdHistorial(int idHistorial) { this.idHistorial = idHistorial; }

    public int getIdPuntosVenta() { return idPuntosVenta; }
    public void setIdPuntosVenta(int idPuntosVenta) { this.idPuntosVenta = idPuntosVenta; }

    public String getPuntoVenta() { return puntoVenta; }
    public void setPuntoVenta(String puntoVenta) { this.puntoVenta = puntoVenta; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getImpulsador() { return impulsador; }
    public void setImpulsador(String impulsador) { this.impulsador = impulsador; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
}