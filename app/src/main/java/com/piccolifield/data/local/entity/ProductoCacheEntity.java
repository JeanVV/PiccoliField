package com.piccolifield.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "productos_cache")
public class ProductoCacheEntity {
    @PrimaryKey
    private int idProductos;

    private String codigoEan;
    private String producto;
    private double precio;
    private int stockActual;
    private int stockMinimo;

    public ProductoCacheEntity(int idProductos, String codigoEan, String producto, double precio, int stockActual, int stockMinimo) {
        this.idProductos = idProductos;
        this.codigoEan = codigoEan;
        this.producto = producto;
        this.precio = precio;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
    }

    public int getIdProductos() { return idProductos; }
    public String getCodigoEan() { return codigoEan; }
    public String getProducto() { return producto; }
    public double getPrecio() { return precio; }
    public int getStockActual() { return stockActual; }
    public int getStockMinimo() { return stockMinimo; }
}