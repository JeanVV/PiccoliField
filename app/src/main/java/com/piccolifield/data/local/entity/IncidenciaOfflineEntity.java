package com.piccolifield.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "incidencias_offline")
public class IncidenciaOfflineEntity {
    @PrimaryKey(autoGenerate = true)
    private int idLocal;

    private String tipo;
    private String severidad;
    private String descripcion;
    private Integer idPuntosVenta;
    private String fotoBase64;
    private String fecha;
    private boolean sincronizado;

    public IncidenciaOfflineEntity(String tipo, String severidad, String descripcion, Integer idPuntosVenta, String fotoBase64, String fecha) {
        this.tipo = tipo;
        this.severidad = severidad;
        this.descripcion = descripcion;
        this.idPuntosVenta = idPuntosVenta;
        this.fotoBase64 = fotoBase64;
        this.fecha = fecha;
        this.sincronizado = false;
    }

    public int getIdLocal() { return idLocal; }
    public void setIdLocal(int idLocal) { this.idLocal = idLocal; }

    public String getTipo() { return tipo; }
    public String getSeveridad() { return severidad; }
    public String getDescripcion() { return descripcion; }
    public Integer getIdPuntosVenta() { return idPuntosVenta; }
    public String getFotoBase64() { return fotoBase64; }
    public String getFecha() { return fecha; }
    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}