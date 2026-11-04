package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class Seguimiento {

    @SerializedName("id")
    private int id;

    @SerializedName("idProspecto")
    private int idProspecto;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("notas")
    private String notas;

    @SerializedName("estadoAnterior")
    private String estadoAnterior;

    @SerializedName("estadoNuevo")
    private String estadoNuevo;

    public Seguimiento() {}

    public Seguimiento(int id, int idProspecto, String fecha, String notas, String estadoAnterior, String estadoNuevo) {
        this.id = id;
        this.idProspecto = idProspecto;
        this.fecha = fecha;
        this.notas = notas;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
    }

    public int getId() { return id; }
    public int getIdProspecto() { return idProspecto; }
    public String getFecha() { return fecha; }
    public String getNotas() { return notas; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
}