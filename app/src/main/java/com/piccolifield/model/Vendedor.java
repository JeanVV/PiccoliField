package com.piccolifield.model;

public class Vendedor {
    private int posicion;
    private String nombre;
    private String local;
    private int cantVentas;
    public Vendedor(int posicion, String nombre, String local,int cantVentas) {
        this.posicion = posicion;
        this.nombre = nombre;
        this.local = local;
        this.cantVentas = cantVentas;
    }

    public int getPosicion() { return posicion; }
    public String getNombre() { return nombre; }
    public String getLocal() { return local; }
    public int getCantVentas(){ return cantVentas;}
}