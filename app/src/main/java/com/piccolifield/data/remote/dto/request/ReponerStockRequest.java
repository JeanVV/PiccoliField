package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class ReponerStockRequest {
    @SerializedName("codigo_barras")
    private String codigoBarras;

    @SerializedName("idProductos")
    private Integer idProductos;

    @SerializedName("cantidad")
    private int cantidad;

    @SerializedName("observacion")
    private String observacion;

    @SerializedName("cant_exhibida")
    private Integer cantExhibida;

    @SerializedName("cant_almacen")
    private Integer cantAlmacen;

    @SerializedName("foto_base64")
    private String fotoBase64;

    public ReponerStockRequest(String codigoBarras, Integer idProductos, int cantidad, String observacion) {
        this.codigoBarras = codigoBarras;
        this.idProductos = idProductos;
        this.cantidad = cantidad;
        this.observacion = observacion;
    }

    public void setCantExhibida(Integer cantExhibida) { this.cantExhibida = cantExhibida; }
    public void setCantAlmacen(Integer cantAlmacen) { this.cantAlmacen = cantAlmacen; }
    public void setFotoBase64(String fotoBase64) { this.fotoBase64 = fotoBase64; }
}