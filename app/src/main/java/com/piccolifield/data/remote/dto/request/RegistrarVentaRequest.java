package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class RegistrarVentaRequest {
    @SerializedName("codigo_barras")
    private String codigoBarras;

    @SerializedName("cantidad")
    private int cantidad;

    public RegistrarVentaRequest(String codigoBarras, int cantidad) {
        this.codigoBarras = codigoBarras;
        this.cantidad = cantidad;
    }
}