package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class ImpulsadorDashboard {
    @SerializedName("tiene_asignacion")
    private boolean tieneAsignacion;

    @SerializedName("punto_venta")
    private PuntoVentaDashboard puntoVenta;

    @SerializedName("horario")
    private HorarioDashboard horario;

    @SerializedName("impulsador")
    private ImpulsadorInfo impulsador;

    @SerializedName("progreso_ventas")
    private ProgresoVentasDashboard progresoVentas;

    public boolean isTieneAsignacion() { return tieneAsignacion; }
    public PuntoVentaDashboard getPuntoVenta() { return puntoVenta; }
    public HorarioDashboard getHorario() { return horario; }
    public ImpulsadorInfo getImpulsador() { return impulsador; }
    public ProgresoVentasDashboard getProgresoVentas() { return progresoVentas; }
}