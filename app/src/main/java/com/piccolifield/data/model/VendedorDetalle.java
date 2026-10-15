package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;
import com.piccolifield.model.ProductoVendido;

import java.util.List;

public class VendedorDetalle {
    private String nombre;
    private String local_pertenece;
    private double monto_vendido;
    private double porcentaje_logro;
    private List<ProductoVendido> productos_vendidos;

    @SerializedName("cantidadVendida")
    private double cantidadVendida;

    @SerializedName("meta_asignada")
    private int meta;

    public String getNombre() { return nombre; }
    public String getLocal_pertenece() { return local_pertenece; }
    public double getMonto_vendido() { return monto_vendido; }
    public double getPorcentaje_logro() { return porcentaje_logro; }
    public List<ProductoVendido> getProductos_vendidos() { return productos_vendidos; }
    public double getCantidadVendida() { return cantidadVendida; }
    public int getMeta() { return meta; }
}