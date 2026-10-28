package com.piccolifield.data.model;

import java.io.Serializable;

public class RegistroAsistencia implements Serializable {
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
    private double latitud;
    private double longitud;

    private String justificacion;
    private String motivoJustificacion;
    private String fechaJustificacion;
    private String estadoJustificacion;
    private String rutaSustento;

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public String getMotivoJustificacion() {
        return motivoJustificacion;
    }

    public void setMotivoJustificacion(String motivoJustificacion) {
        this.motivoJustificacion = motivoJustificacion;
    }

    public String getFechaJustificacion() {
        return fechaJustificacion;
    }

    public void setFechaJustificacion(String fechaJustificacion) {
        this.fechaJustificacion = fechaJustificacion;
    }

    public String getEstadoJustificacion() {
        return estadoJustificacion;
    }

    public void setEstadoJustificacion(String estadoJustificacion) {
        this.estadoJustificacion = estadoJustificacion;
    }

    public String getRutaSustento() {
        return rutaSustento;
    }

    public void setRutaSustento(String rutaSustento) {
        this.rutaSustento = rutaSustento;
    }

    // Getters y Setters
    public int getIdAsistencias() { return idAsistencias; }
    public void setIdAsistencias(int idAsistencias) { this.idAsistencias = idAsistencias; }

    public String getId_turno() { return id_turno; }
    public void setId_turno(String id_turno) { this.id_turno = id_turno; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public void setNombres(String nombres) { this.nombres = nombres; }
    public void setApePaterno(String apePaterno) { this.apePaterno = apePaterno; }
    public void setApeMaterno(String apeMaterno) { this.apeMaterno = apeMaterno; }

    public String getNombreCompleto() {
        String n = nombres != null ? nombres : "";
        String ap = apePaterno != null ? apePaterno : "";
        String am = apeMaterno != null ? apeMaterno : "";
        String completo = (n + " " + ap + " " + am).trim();
        return completo.isEmpty() ? "Colaborador" : completo;
    }

    public String getFotoPerfil() { return foto_perfil; }
    public void setFotoPerfil(String foto_perfil) { this.foto_perfil = foto_perfil; }

    public String getSede() { return sede != null ? sede : "Sede Principal"; }
    public void setSede(String sede) { this.sede = sede; }

    public String getEstado() { return estado != null ? estado : "A_TIEMPO"; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getHoraMarcado() { return fecha_hora_dispositivo; }
    public void setHoraMarcado(String fecha_hora_dispositivo) { this.fecha_hora_dispositivo = fecha_hora_dispositivo; }

    public String getFechaHoraServidor() { return fecha_hora_servidor; }
    public void setFechaHoraServidor(String fecha_hora_servidor) { this.fecha_hora_servidor = fecha_hora_servidor; }

    public int getMinutosTardanza() { return minutos_tardanza; }
    public void setMinutosTardanza(int minutos_tardanza) { this.minutos_tardanza = minutos_tardanza; }
}
