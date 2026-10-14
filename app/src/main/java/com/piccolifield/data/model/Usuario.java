package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class Usuario {

    @SerializedName("usuarioId")
    private int usuarioId;

    private String nombres;
    private String apellidos;
    private String email;
    private String rol;
    private String estado;

    public Usuario() {}

    public Usuario(int usuarioId, String nombres, String apellidos, String email, String rol, String estado) {
        this.usuarioId = usuarioId;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.rol = rol;
        this.estado = estado;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}