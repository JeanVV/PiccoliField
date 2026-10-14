package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class Producto {

    @SerializedName(value = "idProductos", alternate = {"id", "id_producto"})
    private int idProductos;

    @SerializedName("sku")
    private String sku;

    @SerializedName("codigo_ean")
    private String codigoEan;

    @SerializedName(value = "producto", alternate = {"nombre"})
    private String producto;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("presentacion")
    private String presentacion;

    @SerializedName("estado") // ENUM('ACTIVO', 'INACTIVO')
    private String estado;

    public Producto() {}

    public Producto(int idProductos, String sku, String codigoEan, String producto, String categoria, String presentacion, String estado) {
        this.idProductos = idProductos;
        this.sku = sku;
        this.codigoEan = codigoEan;
        this.producto = producto;
        this.categoria = categoria;
        this.presentacion = presentacion;
        this.estado = estado;
    }

    public int getIdProductos() { return idProductos; }
    public String getSku() { return sku; }
    public String getCodigoEan() { return codigoEan; }
    public String getProducto() { return producto; }
    public String getCategoria() { return categoria; }
    public String getPresentacion() { return presentacion; }
    public String getEstado() { return estado; }

    public void setIdProductos(int idProductos) { this.idProductos = idProductos; }
    public void setSku(String sku) { this.sku = sku; }
    public void setCodigoEan(String codigoEan) { this.codigoEan = codigoEan; }
    public void setProducto(String producto) { this.producto = producto; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
    public void setEstado(String estado) { this.estado = estado; }
}