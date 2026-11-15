package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class ProductoBuscado {
    @SerializedName(value = "id", alternate = {"idProductos", "id_producto", "idProducto"})
    private int id;
    
    @SerializedName(value = "nombre", alternate = {"producto", "nombre_producto", "descripcion"})
    private String nombre;
    
    @SerializedName(value = "precio", alternate = {"precio_unitario", "precio_venta"})
    private double precio;
    
    @SerializedName(value = "url_foto", alternate = {"urlFoto", "foto"})
    private String urlFoto;
    
    @SerializedName(value = "stock_actual", alternate = {"stockActual", "stock"})
    private int stockActual;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }
}