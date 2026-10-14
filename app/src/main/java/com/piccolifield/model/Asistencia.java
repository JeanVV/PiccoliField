package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class Asistencia {

    @SerializedName(value = "idAsistencias", alternate = {"id", "id_asistencia"})
    private int idAsistencias;

    @SerializedName("id_turno")
    private String idTurno;

    @SerializedName("tipo") // ENUM('INGRESO', 'SALIDA')
    private String tipo;

    @SerializedName("fecha_hora_servidor")
    private String fechaHoraServidor;

    @SerializedName("fecha_hora_dispositivo")
    private String fechaHoraDispositivo;

    @SerializedName("estado") // ENUM('A_TIEMPO', 'TARDANZA', 'FALTA')
    private String estado;

    @SerializedName("minutos_tardanza")
    private int minutosTardanza;

    @SerializedName("idUsuario")
    private int idUsuario;

    public Asistencia() {}

    public Asistencia(int idAsistencias, String idTurno, String tipo, String fechaHoraDispositivo, String estado, int minutosTardanza, int idUsuario) {
        this.idAsistencias = idAsistencias;
        this.idTurno = idTurno;
        this.tipo = tipo;
        this.fechaHoraDispositivo = fechaHoraDispositivo;
        this.estado = estado;
        this.minutosTardanza = minutosTardanza;
        this.idUsuario = idUsuario;
    }

    public int getIdAsistencias() { return idAsistencias; }
    public String getIdTurno() { return idTurno; }
    public String getTipo() { return tipo; }
    public String getFechaHoraServidor() { return fechaHoraServidor; }
    public String getFechaHoraDispositivo() { return fechaHoraDispositivo; }
    public String getEstado() { return estado; }
    public int getMinutosTardanza() { return minutosTardanza; }
    public int getIdUsuario() { return idUsuario; }

    public void setIdAsistencias(int idAsistencias) { this.idAsistencias = idAsistencias; }
    public void setIdTurno(String idTurno) { this.idTurno = idTurno; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setFechaHoraServidor(String fechaHoraServidor) { this.fechaHoraServidor = fechaHoraServidor; }
    public void setFechaHoraDispositivo(String fechaHoraDispositivo) { this.fechaHoraDispositivo = fechaHoraDispositivo; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setMinutosTardanza(int minutosTardanza) { this.minutosTardanza = minutosTardanza; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
}