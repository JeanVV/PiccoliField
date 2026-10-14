package com.piccolifield.data.model;

import com.piccolifield.model.ProductoVendido;

import java.util.List;

public class VendedorDetalle {
    private String nombre;
    private String local_pertenece;
    private double monto_vendido;
    private double meta_asignada;
    private double porcentaje_logro;
    private List<ProductoVendido> productos_vendidos;

    public String getNombre() { return nombre; }
    public String getLocal_pertenece() { return local_pertenece; }
    public double getMonto_vendido() { return monto_vendido; }
    public double getMeta_asignada() { return meta_asignada; }
    public double getPorcentaje_logro() { return porcentaje_logro; }
    public List<ProductoVendido> getProductos_vendidos() { return productos_vendidos; }
}