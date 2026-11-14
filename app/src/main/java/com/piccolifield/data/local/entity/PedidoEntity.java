package com.piccolifield.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pedidos_offline")
public class PedidoEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int cantSolicitada;
    private String fecha;
    private int idPuntosVenta;
    private int idProductos;
    private String estadoSincronizacion; // "PENDIENTE", "SINCRONIZADO", "ERROR"

    public PedidoEntity() {}

    public PedidoEntity(int cantSolicitada, String fecha, int idPuntosVenta, int idProductos, String estadoSincronizacion) {
        this.cantSolicitada = cantSolicitada;
        this.fecha = fecha;
        this.idPuntosVenta = idPuntosVenta;
        this.idProductos = idProductos;
        this.estadoSincronizacion = estadoSincronizacion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCantSolicitada() { return cantSolicitada; }
    public void setCantSolicitada(int cantSolicitada) { this.cantSolicitada = cantSolicitada; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public int getIdPuntosVenta() { return idPuntosVenta; }
    public void setIdPuntosVenta(int idPuntosVenta) { this.idPuntosVenta = idPuntosVenta; }

    public int getIdProductos() { return idProductos; }
    public void setIdProductos(int idProductos) { this.idProductos = idProductos; }

    public String getEstadoSincronizacion() { return estadoSincronizacion; }
    public void setEstadoSincronizacion(String estadoSincronizacion) { this.estadoSincronizacion = estadoSincronizacion; }
}