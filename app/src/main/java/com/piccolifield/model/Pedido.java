package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class Pedido {

    @SerializedName(value = "idPedido", alternate = {"id", "id_pedido", "idPedidos"})
    private int idPedido;

    @SerializedName(value = "cantSolicitada", alternate = {"cantidad", "cant_solicitada"})
    private int cantSolicitada;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName(value = "idPuntosVenta", alternate = {"idPdv", "id_puntos_venta"})
    private int idPuntosVenta;

    @SerializedName(value = "idProductos", alternate = {"idProducto", "id_producto"})
    private int idProductos;

    @SerializedName("estado")
    private String estado;

    private double totalMonto;
    private String resumenProductos;
    private String criterioNegocio;

    public Pedido() {}

    public Pedido(int idPedido, int cantSolicitada, String fecha, int idPuntosVenta, int idProductos) {
        this.idPedido = idPedido;
        this.cantSolicitada = cantSolicitada;
        this.fecha = fecha;
        this.idPuntosVenta = idPuntosVenta;
        this.idProductos = idProductos;
        this.estado = "Registrado";
    }

    public Pedido(int idPedido, int cantSolicitada, String fecha, int idPuntosVenta, int idProductos, double totalMonto, String resumenProductos, String criterioNegocio) {
        this(idPedido, cantSolicitada, fecha, idPuntosVenta, idProductos);
        this.totalMonto = totalMonto;
        this.resumenProductos = resumenProductos;
        this.criterioNegocio = criterioNegocio;
    }

    public int getIdPedido() { return idPedido; }
    public int getCantSolicitada() { return cantSolicitada; }
    public String getFecha() { return fecha; }
    public int getIdPuntosVenta() { return idPuntosVenta; }
    public int getIdProductos() { return idProductos; }
    public String getEstado() { return estado != null && !estado.isEmpty() ? estado : "Registrado"; }
    public double getTotalMonto() { return totalMonto; }
    public String getResumenProductos() { return resumenProductos; }
    public String getCriterioNegocio() { return criterioNegocio; }

    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public void setCantSolicitada(int cantSolicitada) { this.cantSolicitada = cantSolicitada; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setIdPuntosVenta(int idPuntosVenta) { this.idPuntosVenta = idPuntosVenta; }
    public void setIdProductos(int idProductos) { this.idProductos = idProductos; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setTotalMonto(double totalMonto) { this.totalMonto = totalMonto; }
    public void setResumenProductos(String resumenProductos) { this.resumenProductos = resumenProductos; }
    public void setCriterioNegocio(String criterioNegocio) { this.criterioNegocio = criterioNegocio; }
}
