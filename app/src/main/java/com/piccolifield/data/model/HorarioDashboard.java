package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class HorarioDashboard {
    @SerializedName("hora_inicio")
    private String horaInicio;

    @SerializedName("hora_fin")
    private String horaFin;

    @SerializedName("jornada")
    private String jornada;

    public String getHoraInicio() { return horaInicio; }
    public String getHoraFin() { return horaFin; }
    public String getJornada() { return jornada; }
}