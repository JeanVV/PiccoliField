package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class ProgresoVentasDashboard {
    @SerializedName("meta_mensual")
    private double metaMensual;

    @SerializedName("monto_hoy")
    private double montoHoy;

    @SerializedName("monto_mes")
    private double montoMes;

    @SerializedName("porcentaje_alcanzado")
    private double porcentajeAlcanzado;

    @SerializedName("transacciones_hoy")
    private int transaccionesHoy;

    @SerializedName("unidades_hoy")
    private int unidadesHoy;

    @SerializedName("unidades_mes")
    private int unidadesMes;

    public double getMetaMensual() { return metaMensual; }
    public double getMontoHoy() { return montoHoy; }
    public double getMontoMes() { return montoMes; }
    public double getPorcentajeAlcanzado() { return porcentajeAlcanzado; }
    public int getTransaccionesHoy() { return transaccionesHoy; }
    public int getUnidadesHoy() { return unidadesHoy; }
    public int getUnidadesMes() { return unidadesMes; }
}