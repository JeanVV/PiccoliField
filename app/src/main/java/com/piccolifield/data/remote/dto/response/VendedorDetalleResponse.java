package com.piccolifield.data.remote.dto.response;

import com.piccolifield.data.model.VendedorDetalle;
public class VendedorDetalleResponse {
    private VendedorDetalle data;
    private String message;
    private int http_code;

    public VendedorDetalle getData() { return data; }
    public String getMessage() { return message; }
    public int getHttp_code() { return http_code; }
}