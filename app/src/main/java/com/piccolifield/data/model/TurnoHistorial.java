package com.piccolifield.data.model;

public class TurnoHistorial {
    private String id_turno;
    private String tipo;
    private String fecha_hora;
    private int idUsuario;

    public String getId_turno() { return id_turno; }
    public String getTipo() { return tipo; }
    public String getFechaHora() { return fecha_hora; }
    public int getIdUsuario() { return idUsuario; }

    public String getTipoEvento() {
        return tipo.equals("INGRESO") ? "Entrada" : "Salida";
    }
}