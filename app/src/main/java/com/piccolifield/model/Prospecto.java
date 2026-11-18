package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class Prospecto {

    @SerializedName(value = "idProspectos", alternate = {"id", "id_prospecto", "idProspecto"})
    private int id;

    @SerializedName(value = "nombre_comercial", alternate = {"nombre", "nombreComercial", "puntoVenta", "puntosVenta"})
    private String nombre;

    @SerializedName(value = "tipo_local", alternate = {"tipoLocal", "tipo", "tipo_negocio", "tipoNegocio"})
    private String tipo;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName(value = "contacto", alternate = {"nombre_contacto", "nombreContacto"})
    private String contacto;

    @SerializedName(value = "telefono", alternate = {"celular"})
    private String telefono;

    @SerializedName("potencial") // ENUM('ALTO', 'MEDIO', 'BAJO')
    private String potencial;

    @SerializedName("estado") // ENUM('NUEVO', 'EN_NEGOCIACION', 'CONVERTIDO', 'DESCARTADO')
    private String estado;

    @SerializedName("notas")
    private String notas;

    @SerializedName("motivo_descarte")
    private String motivoDescarte;

    @SerializedName(value = "url_foto", alternate = {"fotoUrl", "foto_fachada", "foto"})
    private String fotoUrl;

    @SerializedName("latitud")
    private double latitud;

    @SerializedName("longitud")
    private double longitud;

    @SerializedName(value = "distrito", alternate = {"zona", "nombre_distrito"})
    private String zona;

    @SerializedName("idDistrito")
    private int idDistrito;

    @SerializedName("idUsuario")
    private int idUsuario;

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
    public String getContacto() { return contacto; }
    public String getEstado() { return estado; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getZona() { return zona; }
    public int getIdDistrito() { return idDistrito; }
    public int getIdUsuario() { return idUsuario; }
    public String getTipo() { return tipo; }
    public String getPotencial() { return potencial; }
    public String getNotas() { return notas; }
    public String getMotivoDescarte() { return motivoDescarte; }
    public String getFotoUrl() { return fotoUrl; }

    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public void setZona(String zona) { this.zona = zona; }
    public void setIdDistrito(int idDistrito) { this.idDistrito = idDistrito; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setPotencial(String potencial) { this.potencial = potencial; }
    public void setNotas(String notas) { this.notas = notas; }
    public void setMotivoDescarte(String motivoDescarte) { this.motivoDescarte = motivoDescarte; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
