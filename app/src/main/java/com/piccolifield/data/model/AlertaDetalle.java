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

    @SerializedName("tipo_alerta")
    private String tipoAlerta;

    @SerializedName("nombre_producto")
    private String nombreProducto;

    @SerializedName("responsable_nombre")
    private String responsableNombre;

    @SerializedName("responsable_rol")
    private String responsableRol;

    @SerializedName("comentario")
    private String comentario;

    @SerializedName("fecha_emision")
    private String fechaEmision;

    @SerializedName("hora_emision")
    private String horaEmision;

    @SerializedName("fecha_atencion")
    private String fechaAtencion;

    @SerializedName("hora_atencion")
    private String horaAtencion;

    @SerializedName("stock_actual")
    private int stockActual;

    @SerializedName("stock_minimo")
    private int stockMinimo;

    @SerializedName("url_foto_producto")
    private String urlFotoProducto;

    @SerializedName("responsable_foto")
    private String responsableFoto;

    @SerializedName(value = "idAlertas", alternate = {"idAlerta", "id_alerta", "id"})
    private int idAlertas;

    @SerializedName(value = "idPuntosVenta", alternate = {"id_puntos_venta", "idPuntoVenta"})
    private int idPuntosVenta;

    @SerializedName(value = "idProductos", alternate = {"id_producto", "idProducto", "id_productos"})
    private int idProductos;

    @SerializedName(value = "responsable_id", alternate = {"idResponsable", "id_responsable", "idUsuario"})
    private int responsableId;
    
    public String getEstadoResolucion() { return estadoResolucion; }
    public String getLocalDireccion() { return localDireccion; }
    public String getLocalNombre() { return localNombre; }
    public String getNivelAlerta() { return nivelAlerta; }
    public String getTipoAlerta() { return tipoAlerta; }
    public String getNombreProducto() { return nombreProducto; }
    public String getResponsableNombre() { return responsableNombre; }
    public String getResponsableRol() { return responsableRol; }
    public String getComentario() { return comentario; }
    public String getFechaEmision() { return fechaEmision; }
    public String getHoraEmision() { return horaEmision; }
    public String getFechaAtencion() { return fechaAtencion; }
    public String getHoraAtencion() { return horaAtencion; }
    public int getStockActual() { return stockActual; }
    public int getStockMinimo() { return stockMinimo; }
    public String getUrlFotoProducto() { return urlFotoProducto; }
    public String getResponsableFoto() { return responsableFoto; }
    public int getIdAlertas() { return idAlertas != 0 ? idAlertas : idProductos; }
    public int getIdProductos() { return idProductos; }
    public int getResponsableId() { return responsableId; }
}