package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class TipoIncidenciaDto {
    @SerializedName("tipo")
    private String tipo;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("severidad_sugerida")
    private String severidadSugerida;

    public String getTipo() { return tipo; }
    public String getNombre() { return nombre; }
    public String getSeveridadSugerida() { return severidadSugerida; }

    @Override
    public String toString() {
        return nombre != null ? nombre : tipo;
    }
}