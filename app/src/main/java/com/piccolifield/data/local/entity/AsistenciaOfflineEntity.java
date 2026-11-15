package com.piccolifield.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "asistencias_offline")
public class AsistenciaOfflineEntity {
    @PrimaryKey(autoGenerate = true)
    private int idLocal;

    private String tipoEvento; // "ENTRADA", "SALIDA"
    private double latitud;
    private double longitud;
    private String fechaHora;
    private boolean sincronizado;

    public AsistenciaOfflineEntity(String tipoEvento, double latitud, double longitud, String fechaHora) {
        this.tipoEvento = tipoEvento;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaHora = fechaHora;
        this.sincronizado = false;
    }

    public int getIdLocal() { return idLocal; }
    public void setIdLocal(int idLocal) { this.idLocal = idLocal; }

    public String getTipoEvento() { return tipoEvento; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getFechaHora() { return fechaHora; }
    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}