package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class PuntoVentaHistorial {
    @SerializedName("idHistorial")
    private int idHistorial;

    @SerializedName("idPuntosVenta")
    private int idPuntosVenta;

    @SerializedName("puntoVenta")
    private String puntoVenta;

    @SerializedName("fecha_inicio")
    private String fechaInicio;

    @SerializedName("fecha_fin")
    private String fechaFin;

    @SerializedName("motivo")
    private String motivo;

    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("impulsador_nombre")
    private String impulsadorNombre;

    @SerializedName("impulsador_correo")
    private String impulsadorCorreo;

    @SerializedName("impulsador_foto")
    private String impulsadorFoto;

    @SerializedName("impulsador_rol")
    private String impulsadorRol;

    @SerializedName("idUsuarioRegistra")
    private int idUsuarioRegistra;

    @SerializedName("registrado_por")
    private String registradoPor;

    @SerializedName("es_actual")
    private int esActual;

    public int getIdHistorial() { return idHistorial; }
    public void setIdHistorial(int idHistorial) { this.idHistorial = idHistorial; }

    public int getIdPuntosVenta() { return idPuntosVenta; }
    public void setIdPuntosVenta(int idPuntosVenta) { this.idPuntosVenta = idPuntosVenta; }

    public String getPuntoVenta() { return puntoVenta; }
    public void setPuntoVenta(String puntoVenta) { this.puntoVenta = puntoVenta; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getImpulsadorNombre() { return impulsadorNombre; }
    public void setImpulsadorNombre(String impulsadorNombre) { this.impulsadorNombre = impulsadorNombre; }

    public String getImpulsadorCorreo() { return impulsadorCorreo; }
    public void setImpulsadorCorreo(String impulsadorCorreo) { this.impulsadorCorreo = impulsadorCorreo; }

    public String getImpulsadorFoto() { return impulsadorFoto; }
    public void setImpulsadorFoto(String impulsadorFoto) { this.impulsadorFoto = impulsadorFoto; }

    public String getImpulsadorRol() { return impulsadorRol; }
    public void setImpulsadorRol(String impulsadorRol) { this.impulsadorRol = impulsadorRol; }

    public int getIdUsuarioRegistra() { return idUsuarioRegistra; }
    public void setIdUsuarioRegistra(int idUsuarioRegistra) { this.idUsuarioRegistra = idUsuarioRegistra; }

    public String getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }

    public int getEsActual() { return esActual; }
    public void setEsActual(int esActual) { this.esActual = esActual; }
}