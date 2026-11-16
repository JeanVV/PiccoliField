package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class Vendedor {
    @SerializedName(value = "idUsuario", alternate = {"idUsuarios", "id_usuario", "id_usuarios", "usuarioId", "id"})
    private int idUsuario;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("local")
    private String local;

    @SerializedName("cantidadVendida")
    private double cantVentas;

    @SerializedName("posicion")
    private int posicion;

    @SerializedName("meta_asignada")
    private int meta_asignada;

    @SerializedName(value = "url_foto", alternate = {"foto_perfil", "urlFoto", "foto", "fotoPerfil", "imagen", "avatar", "foto_base64"})
    private String urlFoto;

    public Vendedor(int idUsuario, int posicion, String nombre, String local, int cantVentas, int meta_asignada) {
        this.idUsuario = idUsuario;
        this.posicion = posicion;
        this.nombre = nombre;
        this.local = local;
        this.cantVentas = cantVentas;
        this.meta_asignada = meta_asignada;
    }

    public int getIdUsuario() { return idUsuario; }
    public int getPosicion() { return posicion; }
    public String getNombre() { return nombre; }
    public String getLocal() { return local; }
    public double getCantVentas() { return cantVentas; }
    public int getMeta_asignada() { return meta_asignada; }
    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }
}
