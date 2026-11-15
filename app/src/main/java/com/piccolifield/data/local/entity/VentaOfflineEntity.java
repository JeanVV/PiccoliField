package com.piccolifield.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ventas_offline")
public class VentaOfflineEntity {
    @PrimaryKey(autoGenerate = true)
    private int idLocal;

    private String codigoBarras;
    private int cantidad;
    private String fecha;
    private boolean sincronizado;

    public VentaOfflineEntity(String codigoBarras, int cantidad, String fecha) {
        this.codigoBarras = codigoBarras;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.sincronizado = false;
    }

    public int getIdLocal() { return idLocal; }
    public void setIdLocal(int idLocal) { this.idLocal = idLocal; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}