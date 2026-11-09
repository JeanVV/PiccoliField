package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class RendimientoMensualItem {
    @SerializedName("mes_nombre")
    private String mesNombre;

    @SerializedName("mes_anio")
    private String mesAnio;

    @SerializedName("anio")
    private int anio;

    @SerializedName("mes")
    private int mes;

    @SerializedName("monto_vendido")
    private double montoVendido;

    @SerializedName("meta_mensual")
    private double metaMensual;

    @SerializedName("meta_ventas_asignada")
    private double metaVentasAsignada;

    @SerializedName("meta_asignada")
    private double metaAsignada;

    @SerializedName("porcentaje_cumplimiento")
    private double porcentajeCumplimiento;

    @SerializedName("porcentaje")
    private double porcentaje;

    @SerializedName("cumplio_meta")
    private boolean cumplioMeta;

    @SerializedName("estado_meta")
    private String estadoMeta;

    @SerializedName("total_asistencias")
    private int totalAsistencias;

    @SerializedName("total_tardanzas")
    private int totalTardanzas;

    @SerializedName("total_faltas")
    private int totalFaltas;

    public String getMesAnioFormatted() {
        if (mesNombre != null && !mesNombre.isEmpty()) {
            return mesNombre + (anio > 0 ? " " + anio : "");
        }
        if (mesAnio != null && !mesAnio.isEmpty()) {
            return mesAnio;
        }
        return "Mes " + mes + " " + anio;
    }

    public double getMontoVendido() { return montoVendido; }

    public double getMetaAsignadaEfectiva() {
        if (metaMensual > 0) return metaMensual;
        if (metaVentasAsignada > 0) return metaVentasAsignada;
        return metaAsignada;
    }

    public double getPorcentajeEfectivo() {
        return porcentajeCumplimiento > 0 ? porcentajeCumplimiento : porcentaje;
    }

    public boolean isMetaCumplida() {
        if ("CUMPLIDA".equalsIgnoreCase(estadoMeta) || cumplioMeta) {
            return true;
        }
        return getPorcentajeEfectivo() >= 100.0;
    }

    public String getEstadoMeta() { return estadoMeta; }
    public int getTotalAsistencias() { return totalAsistencias; }
    public int getTotalTardanzas() { return totalTardanzas; }
    public int getTotalFaltas() { return totalFaltas; }
}