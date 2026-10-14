package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class Pedido {

    @SerializedName(value = "idPedido", alternate = {"id"})
    private int idPedido;

    @SerializedName("cantSolicitada")
    private int cantSolicitada;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("idPuntosVenta")
    private int idPuntosVenta;

    @SerializedName("idProductos")
    private int idProductos;

    public Pedido() {}

    public Pedido(int idPedido, int cantSolicitada, String fecha, int idPuntosVenta, int idProductos) {
        this.idPedido = idPedido;
        this.cantSolicitada = cantSolicitada;
        this.fecha = fecha;
        this.idPuntosVenta = idPuntosVenta;
        this.idProductos = idProductos;
    }

    public int getIdPedido() { return idPedido; }
    public int getCantSolicitada() { return cantSolicitada; }
    public String getFecha() { return fecha; }
    public int getIdPuntosVenta() { return idPuntosVenta; }
    public int getIdProductos() { return idProductos; }

    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public void setCantSolicitada(int cantSolicitada) { this.cantSolicitada = cantSolicitada; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setIdPuntosVenta(int idPuntosVenta) { this.idPuntosVenta = idPuntosVenta; }
    public void setIdProductos(int idProductos) { this.idProductos = idProductos; }
}