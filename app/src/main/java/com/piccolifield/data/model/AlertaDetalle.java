package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class AlertaDetalle {

    @SerializedName("estado_resolucion")
    private String estadoResolucion;

    @SerializedName("local_direccion")
    private String localDireccion;

    @SerializedName("local_nombre")
    private String localNombre;

    @SerializedName("nivel_alerta")
    private String nivelAlerta;

    @SerializedName("nombre_producto")
    private String nombreProducto;

    @SerializedName("responsable_nombre")
    private String responsableNombre;

    @SerializedName("responsable_rol")
    private String responsableRol;

    @SerializedName("stock_actual")
    private int stockActual;

    @SerializedName("stock_minimo")
    private int stockMinimo;

    @SerializedName("url_foto_producto")
    private String urlFotoProducto;

    @SerializedName("responsable_foto")
    private String responsableFoto;

    @SerializedName("idProductos")
    private int idProductos;

    @SerializedName("responsable_id")
    private int responsableId;
    
    public String getEstadoResolucion() { return estadoResolucion; }
    public String getLocalDireccion() { return localDireccion; }
    public String getLocalNombre() { return localNombre; }
    public String getNivelAlerta() { return nivelAlerta; }
    public String getNombreProducto() { return nombreProducto; }
    public String getResponsableNombre() { return responsableNombre; }
    public String getResponsableRol() { return responsableRol; }
    public int getStockActual() { return stockActual; }
    public int getStockMinimo() { return stockMinimo; }
    public String getUrlFotoProducto() { return urlFotoProducto; }
    public String getResponsableFoto() { return responsableFoto; }
    public int getIdProductos() { return idProductos; }
    public int getResponsableId() { return responsableId; }
}