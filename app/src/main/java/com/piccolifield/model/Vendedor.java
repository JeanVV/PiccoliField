package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class Vendedor {
    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("local")
    private String local;

    @SerializedName("cantidadVendida")
    private double cantVentas;

    @SerializedName("posicion")
    private int posicion;

    public Vendedor(int idUsuario, int posicion, String nombre, String local,int cantVentas) {
        this.idUsuario = idUsuario;
        this.posicion = posicion;
        this.nombre = nombre;
        this.local = local;
        this.cantVentas = cantVentas;
    }

    public int getIdUsuario(){ return idUsuario; }
    public int getPosicion() { return posicion; }
    public String getNombre() { return nombre; }
    public String getLocal() { return local; }
    public double getCantVentas(){ return cantVentas;}
}