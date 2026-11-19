package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class AsistenciaResumen {
    @SerializedName("porcentaje")
    private int porcentaje;

    @SerializedName("presentes")
    private int presentes;

    @SerializedName("total")
    private int total;

    @SerializedName("aTiempo")
    private int aTiempo;

    @SerializedName("tardanzas")
    private int tardanzas;

    @SerializedName("faltas")
    private int faltas;

    @SerializedName("sinMarcar")
    private int sinMarcar;

    public int getPorcentaje() { return porcentaje; }
    public int getPresentes() { return presentes; }
    public int getTotal() { return total; }
    public int getATiempo() { return aTiempo; }
    public int getTardanzas() { return tardanzas; }
    public int getFaltas() { return faltas; }
    public int getSinMarcar() { return sinMarcar; }

    public void setFaltas(int faltas) { this.faltas = faltas; }
    public void setSinMarcar(int sinMarcar) { this.sinMarcar = sinMarcar; }
}
