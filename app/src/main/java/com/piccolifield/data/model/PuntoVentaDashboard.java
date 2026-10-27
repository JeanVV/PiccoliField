package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class PuntoVentaDashboard {
    @SerializedName("idPuntosVenta")
    private int idPuntosVenta;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("distrito")
    private String distrito;

    public int getIdPuntosVenta() { return idPuntosVenta; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getDistrito() { return distrito; }
}