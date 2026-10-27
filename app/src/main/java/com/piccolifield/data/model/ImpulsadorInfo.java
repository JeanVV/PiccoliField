package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class ImpulsadorInfo {
    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("correo")
    private String correo;

    @SerializedName("meta_ventas")
    private double metaVentas;

    @SerializedName("url_foto")
    private String urlFoto;

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public double getMetaVentas() { return metaVentas; }
    public String getUrlFoto() { return urlFoto; }
}