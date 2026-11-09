package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VendedorRendimientoHistoricoResponse {
    @SerializedName("avance_mensual")
    private List<RendimientoMensualItem> avanceMensual;

    @SerializedName("desempeno_mensual")
    private List<RendimientoMensualItem> desempenoMensual;

    @SerializedName("historial_asignaciones")
    private List<PuntoVentaHistorial> historialAsignaciones;

    public List<RendimientoMensualItem> getDesempenoMensual() {
        return avanceMensual != null ? avanceMensual : desempenoMensual;
    }

    public List<PuntoVentaHistorial> getHistorialAsignaciones() {
        return historialAsignaciones;
    }
}