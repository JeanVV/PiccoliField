package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class ResumenDashboard {
    @SerializedName("monto_total_ventas")
    private double montoTotalVentas;

    @SerializedName("cantidad_pdv_activos")
    private int cantidadPdvActivos;

    @SerializedName("cantidad_alertas")
    private int cantidadAlertas;

    @SerializedName("cantidad_incidencias_abiertas")
    private int cantidadIncidenciasAbiertas;

    public double getMonto_total_ventas() { return montoTotalVentas; }
    public int getCantidad_pdv_activos() { return cantidadPdvActivos; }
    public int getCantidad_alertas() { return cantidadAlertas; }
    public int getCantidadIncidenciasAbiertas() { return cantidadIncidenciasAbiertas; }
}