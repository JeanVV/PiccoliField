package com.piccolifield.data.model;

public class AlertaDetalle {
    private String nombre_producto;
    private int stock_actual;
    private int stock_minimo;
    private String local_nombre;
    private String local_direccion;
    private String responsable_nombre;
    private String responsable_rol;
    private String estado_resolucion;
    private String nivel_alerta;

    public String getNombre_producto() { return nombre_producto; }
    public int getStock_actual() { return stock_actual; }
    public int getStock_minimo() { return stock_minimo; }
    public String getLocal_nombre() { return local_nombre; }
    public String getLocal_direccion() { return local_direccion; }
    public String getResponsable_nombre() { return responsable_nombre; }
    public String getResponsable_rol() { return responsable_rol; }
    public String getEstado_resolucion() { return estado_resolucion; }
    public String getNivel_alerta() { return nivel_alerta; }
}