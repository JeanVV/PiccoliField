package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class PuntoVentaProducto {
    @SerializedName(value = "idProductos", alternate = {"id_producto", "idProducto"})
    private int idProductos;

    @SerializedName("sku")
    private String sku;

    @SerializedName("codigo_ean")
    private String codigoEan;

    @SerializedName(value = "producto", alternate = {"nombre", "nombre_producto"})
    private String producto;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("url_foto")
    private String urlFoto;

    @SerializedName("estado")
    private String estado;

    @SerializedName("stock_minimo")
    private int stockMinimo;

    @SerializedName(value = "stock_actual", alternate = {"stockActual", "stock"})
    private int stockActual;

    public int getIdProductos() { return idProductos; }
    public void setIdProductos(int idProductos) { this.idProductos = idProductos; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getCodigoEan() { return codigoEan; }
    public void setCodigoEan(String codigoEan) { this.codigoEan = codigoEan; }

    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }
}