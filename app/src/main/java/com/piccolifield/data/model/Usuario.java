package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class Usuario {

    @SerializedName(value = "idUsuario", alternate = {"usuarioId", "id_usuario", "id"})
    private int idUsuario;

    @SerializedName(value = "nombres", alternate = {"nombre"})
    private String nombres;

    @SerializedName("apePaterno")
    private String apePaterno;

    @SerializedName("apeMaterno")
    private String apeMaterno;

    @SerializedName(value = "correo", alternate = {"email"})
    private String correo;

    @SerializedName("foto_perfil")
    private String fotoPerfil;

    @SerializedName("rol") // ENUM('ADMINISTRADOR', 'PROMOTOR', 'IMPULSADOR')
    private String rol;

    @SerializedName("estado") // ENUM('ACTIVO', 'INACTIVO')
    private String estado;

    @SerializedName("idDistrito")
    private int idDistrito;

    @SerializedName("idSede")
    private int idSede;

    public Usuario() {}

    public Usuario(int idUsuario, String nombres, String apePaterno, String apeMaterno, String correo, String rol, String estado) {
        this.idUsuario = idUsuario;
        this.nombres = nombres;
        this.apePaterno = apePaterno;
        this.apeMaterno = apeMaterno;
        this.correo = correo;
        this.rol = rol;
        this.estado = estado;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getUsuarioId() {
        return idUsuario;
    }

    public String getNombres() {
        return nombres != null ? nombres : "";
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApePaterno() {
        return apePaterno != null ? apePaterno : "";
    }

    public void setApePaterno(String apePaterno) {
        this.apePaterno = apePaterno;
    }

    public String getApeMaterno() {
        return apeMaterno != null ? apeMaterno : "";
    }

    public void setApeMaterno(String apeMaterno) {
        this.apeMaterno = apeMaterno;
    }

    public String getApellidos() {
        String ap = ((apePaterno != null ? apePaterno : "") + " " + (apeMaterno != null ? apeMaterno : "")).trim();
        return ap;
    }

    public String getNombreCompleto() {
        return (getNombres() + " " + getApellidos()).trim();
    }

    public String getEmail() {
        return correo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setEmail(String email) {
        this.correo = email;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdDistrito() {
        return idDistrito;
    }

    public void setIdDistrito(int idDistrito) {
        this.idDistrito = idDistrito;
    }

    public int getIdSede() {
        return idSede;
    }

    public void setIdSede(int idSede) {
        this.idSede = idSede;
    }
}