package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class Producto {

    @SerializedName(value = "idProductos", alternate = {"id", "id_producto", "idProducto"})
    private int idProductos;

    @SerializedName("sku")
    private String sku;

    @SerializedName(value = "codigo_ean", alternate = {"codigoEan", "ean"})
    private String codigoEan;

    @SerializedName(value = "producto", alternate = {"nombre", "nombre_producto", "descripcion"})
    private String producto;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("presentacion")
    private String presentacion;

    @SerializedName("estado") // ENUM('ACTIVO', 'INACTIVO')
    private String estado;

    @SerializedName(value = "precio", alternate = {"precio_unitario", "precio_venta", "precio_minorista", "precioMinorista"})
    private double precioMinorista;

    @SerializedName(value = "precio_mayorista", alternate = {"precioMayorista", "precio_mayor"})
    private double precioMayorista;

    @SerializedName(value = "url_foto", alternate = {"urlFoto", "foto"})
    private String urlFoto;

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

    public Producto(int idProductos, String sku, String codigoEan, String producto, String categoria, String presentacion, String estado, double precioMinorista, double precioMayorista) {
        this(idProductos, sku, codigoEan, producto, categoria, presentacion, estado);
        this.precioMinorista = precioMinorista;
        this.precioMayorista = precioMayorista;
    }

    public int getIdProductos() { return idProductos; }
    public String getSku() { return sku != null && !sku.isEmpty() ? sku : String.format("SKU-%02d", Math.max(1, idProductos)); }
    public String getCodigoEan() { return codigoEan != null ? codigoEan : ""; }
    public String getProducto() { return producto != null && !producto.isEmpty() ? producto : "Producto Piccoli #" + idProductos; }
    public String getCategoria() { return categoria != null && !categoria.isEmpty() ? categoria : "Licores"; }
    public String getPresentacion() { return presentacion != null && !presentacion.isEmpty() ? presentacion : "Botella 750ml"; }
    public String getEstado() { return estado != null ? estado : "ACTIVO"; }
    public String getUrlFoto() { return urlFoto; }

    public double getPrecioMinorista() {
        if (precioMinorista > 0) {
            return precioMinorista;
        }
        String n = getProducto().toLowerCase();
        if (n.contains("pisco")) return 45.00;
        if (n.contains("chocolate")) return 42.00;
        if (n.contains("cacao")) return 40.00;
        if (n.contains("vainilla") || n.contains("crema")) return 38.00;
        if (n.contains("vino")) return 39.00;
        if (n.contains("cerveza")) return 14.00;
        if (n.contains("snack") || n.contains("papa")) return 9.50;
        return 35.00 + ((Math.max(1, idProductos) % 5) * 4.00);
    }

    public double getPrecioMayorista() {
        if (precioMayorista > 0) {
            return precioMayorista;
        }
        double base = getPrecioMinorista();
        return Math.round(base * 0.80 * 100.0) / 100.0;
    }

    public double getPrecioSegunCriterio(boolean esMayorista) {
        return esMayorista ? getPrecioMayorista() : getPrecioMinorista();
    }

    public void setIdProductos(int idProductos) { this.idProductos = idProductos; }
    public void setSku(String sku) { this.sku = sku; }
    public void setCodigoEan(String codigoEan) { this.codigoEan = codigoEan; }
    public void setProducto(String producto) { this.producto = producto; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setPrecioMinorista(double precioMinorista) { this.precioMinorista = precioMinorista; }
    public void setPrecioMayorista(double precioMayorista) { this.precioMayorista = precioMayorista; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }
}
