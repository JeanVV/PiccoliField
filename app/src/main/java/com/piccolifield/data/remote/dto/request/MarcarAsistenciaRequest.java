package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class MarcarAsistenciaRequest {

    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("tipo") // "INGRESO" o "SALIDA"
    private String tipo;

    @SerializedName("latitud")
    private double latitud;

    @SerializedName("longitud")
    private double longitud;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("sede")
    private String sede;

    @SerializedName("fechaHoraDispositivo")
    private String fechaHoraDispositivo;

    public MarcarAsistenciaRequest() {}

    public MarcarAsistenciaRequest(double latitud, double longitud, String direccion) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccion = direccion;
    }

    public MarcarAsistenciaRequest(int idUsuario, String tipo, double latitud, double longitud, String sede, String fechaHoraDispositivo) {
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.latitud = latitud;
        this.longitud = longitud;
        this.sede = sede;
        this.fechaHoraDispositivo = fechaHoraDispositivo;
    }

    public MarcarAsistenciaRequest(int idUsuario, String tipo, double latitud, double longitud, String direccion, String sede, String fechaHoraDispositivo) {
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccion = direccion;
        this.sede = sede;
        this.fechaHoraDispositivo = fechaHoraDispositivo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public String getFechaHoraDispositivo() {
        return fechaHoraDispositivo;
    }

    public void setFechaHoraDispositivo(String fechaHoraDispositivo) {
        this.fechaHoraDispositivo = fechaHoraDispositivo;
    }
}
