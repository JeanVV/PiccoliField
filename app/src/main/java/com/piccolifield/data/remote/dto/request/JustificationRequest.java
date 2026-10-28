package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class JustificationRequest {

    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("motivo")
    private String motivo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName(value = "ruta_sustento", alternate = {"rutaSustento", "fotoSustento"})
    private String rutaSustento;

    public JustificationRequest() {}

    public JustificationRequest(int idUsuario, String motivo, String descripcion, String fecha) {
        this.idUsuario = idUsuario;
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public JustificationRequest(int idUsuario, String motivo, String descripcion, String fecha, String rutaSustento) {
        this.idUsuario = idUsuario;
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.rutaSustento = rutaSustento;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getRutaSustento() {
        return rutaSustento;
    }

    public void setRutaSustento(String rutaSustento) {
        this.rutaSustento = rutaSustento;
    }
}
