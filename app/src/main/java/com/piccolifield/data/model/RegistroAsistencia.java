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

    // Getters
    public int getIdAsistencias() { return idAsistencias; }
    public String getId_turno() { return id_turno; }
    public String getTipo() { return tipo; }
    public int getIdUsuario() { return idUsuario; }
    public String getNombreCompleto() {
        String n = nombres != null ? nombres : "";
        String ap = apePaterno != null ? apePaterno : "";
        String am = apeMaterno != null ? apeMaterno : "";
        String completo = (n + " " + ap + " " + am).trim();
        return completo.isEmpty() ? "Colaborador" : completo;
    }
    public String getFotoPerfil() { return foto_perfil; }
    public String getSede() { return sede != null ? sede : "Sede Principal"; }
    public String getEstado() { return estado != null ? estado : "A_TIEMPO"; }
    public String getHoraMarcado() { return fecha_hora_dispositivo; }
    public String getFechaHoraServidor() { return fecha_hora_servidor; }
    public int getMinutosTardanza() { return minutos_tardanza; }
}
