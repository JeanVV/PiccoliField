package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class SupervisionPuntoVenta {

    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("estado")
    private String estado;

    public int getId() { return id; }
    public String getNombre() { return nombre != null ? nombre : "Sin nombre"; }
    public String getDireccion() { return direccion != null ? direccion : "Sin dirección"; }
    public String getEstado() { return estado != null ? estado : "DESCONOCIDO"; }
}