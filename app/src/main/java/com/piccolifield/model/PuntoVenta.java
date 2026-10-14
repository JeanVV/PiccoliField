package com.piccolifield.model;

public class PuntoVenta {
    private int id;
    private String nombre;
    private String direccion;
    private String estado;

    public PuntoVenta(int id, String nombre, String direccion, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.estado = estado;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getEstado() { return estado; }
}
