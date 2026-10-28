package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class Impulsador {
    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("nombre_completo")
    private String nombreCompleto;

    @SerializedName("correo")
    private String correo;

    @SerializedName("rol")
    private String rol;

    @SerializedName("url_foto")
    private String urlFoto;

    @SerializedName("meta_ventas")
    private int metaVentas;

    @SerializedName("idPuntosVenta")
    private Integer idPuntosVenta;

    @SerializedName("local_actual")
    private String localActual;

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }

    public int getMetaVentas() { return metaVentas; }
    public void setMetaVentas(int metaVentas) { this.metaVentas = metaVentas; }

    public Integer getIdPuntosVenta() { return idPuntosVenta; }
    public void setIdPuntosVenta(Integer idPuntosVenta) { this.idPuntosVenta = idPuntosVenta; }

    public String getLocalActual() { return localActual; }
    public void setLocalActual(String localActual) { this.localActual = localActual; }
}