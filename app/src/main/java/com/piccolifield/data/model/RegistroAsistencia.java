package com.piccolifield.data.model;

public class RegistroAsistencia {
    private int idAsistencias;
    private String id_turno;
    private String tipo;
    private int idUsuario;
    private String nombres;
    private String apePaterno;
    private String apeMaterno;
    private String foto_perfil;
    private String sede;
    private String estado;
    private String fecha_hora_dispositivo;
    private String fecha_hora_servidor;
    private int minutos_tardanza;

    // Getters
    public int getIdAsistencias() { return idAsistencias; }
    public String getId_turno() { return id_turno; }
    public String getTipo() { return tipo; }
    public int getIdUsuario() { return idUsuario; }
    public String getNombreCompleto() {
        return nombres + " " + apePaterno + " " + apeMaterno;
    }
    public String getFotoPerfil() { return foto_perfil; }
    public String getSede() { return sede; }
    public String getEstado() { return estado; }
    public String getHoraMarcado() { return fecha_hora_dispositivo; }
    public String getFechaHoraServidor() { return fecha_hora_servidor; }
    public int getMinutosTardanza() { return minutos_tardanza; }
}