package com.piccolifield.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reposiciones_offline")
public class ReposicionOfflineEntity {
    @PrimaryKey(autoGenerate = true)
    private int idLocal;

    private String codigoBarras;
    private int cantidad;
    private String observacion;
    private String fecha;
    private boolean sincronizado;

    public ReposicionOfflineEntity(String codigoBarras, int cantidad, String observacion, String fecha) {
        this.codigoBarras = codigoBarras;
        this.cantidad = cantidad;
        this.observacion = observacion;
        this.fecha = fecha;
        this.sincronizado = false;
    }

    public int getIdLocal() { return idLocal; }
    public void setIdLocal(int idLocal) { this.idLocal = idLocal; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}