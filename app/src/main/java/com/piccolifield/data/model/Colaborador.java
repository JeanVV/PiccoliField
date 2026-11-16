package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class Colaborador {
    @SerializedName(value = "idUsuario", alternate = {"id_usuario", "usuarioId", "id"})
    private int id;

    @SerializedName(value = "nombres", alternate = {"nombre", "nombre_completo"})
    private String nombres;

    @SerializedName(value = "apePaterno", alternate = {"ape_paterno", "apellidoPaterno"})
    private String apePaterno;

    @SerializedName(value = "url_foto", alternate = {"foto_perfil", "foto_url", "urlFoto", "foto"})
    private String fotoPerfil;

    @SerializedName("sede")
    private String sede;

    @SerializedName("estado")
    private String estado;

    @SerializedName("fecha_hora_dispositivo")
    private String horaMarcado;

    public int getId() { return id; }
    public String getNombreCompleto() { return ((nombres != null ? nombres : "") + " " + (apePaterno != null ? apePaterno : "")).trim(); }
    public String getFotoPerfil() { return fotoPerfil; }
    public String getSede() { return sede; }
    public String getEstado() { return estado; }
    public String getHoraMarcado() { return horaMarcado; }
}
