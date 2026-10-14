package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class Colaborador {
    @SerializedName("idUsuario")
    private int id;

    @SerializedName("nombres")
    private String nombres;

    @SerializedName("apePaterno")
    private String apePaterno;

    @SerializedName("foto_perfil")
    private String fotoPerfil;

    @SerializedName("sede") // Asumiendo que tu API hace un JOIN y devuelve el nombre de la sede
    private String sede;

    @SerializedName("estado") // "A_TIEMPO", "TARDANZA", "FALTA"
    private String estado;

    @SerializedName("fecha_hora_dispositivo") // La hora del marcado
    private String horaMarcado;

    // Getters
    public int getId() { return id; }
    public String getNombreCompleto() { return nombres + " " + apePaterno; }
    public String getFotoPerfil() { return fotoPerfil; }
    public String getSede() { return sede; }
    public String getEstado() { return estado; }
    public String getHoraMarcado() { return horaMarcado; }
}