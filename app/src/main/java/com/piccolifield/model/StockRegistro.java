package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class StockRegistro {

    @SerializedName(value = "idStock", alternate = {"id"})
    private int idStock;

    @SerializedName("cant_exhibida")
    private int cantExhibida;

    @SerializedName("cant_almacen")
    private int cantAlmacen;

    @SerializedName("estado_stock") // ENUM('OPTIMO', 'BAJO', 'AGOTADO')
    private String estadoStock;

    @SerializedName("observacion")
    private String observacion;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("stock_minimo")
    private int stockMinimo;

    @SerializedName("idPuntosVenta")
    private int idPuntosVenta;

    @SerializedName("idProductos")
    private int idProductos;

    @SerializedName("PedidoidPedido")
    private Integer pedidoidPedido;

    public StockRegistro() {}

    public StockRegistro(int idStock, int cantExhibida, int cantAlmacen, String estadoStock, int stockMinimo, int idPuntosVenta, int idProductos) {
        this.idStock = idStock;
        this.cantExhibida = cantExhibida;
        this.cantAlmacen = cantAlmacen;
        this.estadoStock = estadoStock;
        this.stockMinimo = stockMinimo;
        this.idPuntosVenta = idPuntosVenta;
        this.idProductos = idProductos;
    }

    public int getIdStock() { return idStock; }
    public int getCantExhibida() { return cantExhibida; }
    public int getCantAlmacen() { return cantAlmacen; }
    public String getEstadoStock() { return estadoStock; }
    public String getObservacion() { return observacion; }
    public String getFecha() { return fecha; }
    public int getStockMinimo() { return stockMinimo; }
    public int getIdPuntosVenta() { return idPuntosVenta; }
    public int getIdProductos() { return idProductos; }
    public Integer getPedidoidPedido() { return pedidoidPedido; }

    public void setIdStock(int idStock) { this.idStock = idStock; }
    public void setCantExhibida(int cantExhibida) { this.cantExhibida = cantExhibida; }
    public void setCantAlmacen(int cantAlmacen) { this.cantAlmacen = cantAlmacen; }
    public void setEstadoStock(String estadoStock) { this.estadoStock = estadoStock; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    public void setIdPuntosVenta(int idPuntosVenta) { this.idPuntosVenta = idPuntosVenta; }
    public void setIdProductos(int idProductos) { this.idProductos = idProductos; }
    public void setPedidoidPedido(Integer pedidoidPedido) { this.pedidoidPedido = pedidoidPedido; }
}