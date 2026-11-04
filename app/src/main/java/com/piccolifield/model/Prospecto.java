package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class Prospecto {

    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("telefono")
    private String telefono;

    @SerializedName("estado") // NUEVO, CONTACTADO, NEGOCIACION, CONVERTIDO, RECHAZADO
    private String estado;

    @SerializedName("latitud")
    private double latitud;

    @SerializedName("longitud")
    private double longitud;

    @SerializedName("zona")
    private String zona;

    @SerializedName("tipo") // BODEGA, MINIMARKET, LICORERIA, etc.
    private String tipo;

    @SerializedName("fotoUrl")
    private String fotoUrl;

    public Prospecto() {}

    public Prospecto(int id, String nombre, String direccion, String estado, String zona, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.estado = estado;
        this.zona = zona;
        this.tipo = tipo;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getEstado() { return estado; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getZona() { return zona; }
    public String getTipo() { return tipo; }
    public String getFotoUrl() { return fotoUrl; }

    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public void setZona(String zona) { this.zona = zona; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}