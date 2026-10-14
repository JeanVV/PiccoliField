package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class PuntoVenta {

    @SerializedName(value = "idPuntosVenta", alternate = {"id", "idPdv", "id_puntos_venta"})
    private int idPuntosVenta;

    @SerializedName("codigo")
    private String codigo;

    @SerializedName(value = "puntosVenta", alternate = {"nombre", "nombrePdv", "tienda"})
    private String puntosVenta;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("estado") // ENUM('ACTIVO', 'INACTIVO', 'CLAUSURADO')
    private String estado;

    @SerializedName("latitud")
    private double latitud;

    @SerializedName("longitud")
    private double longitud;

    @SerializedName("idDistrito")
    private int idDistrito;

    @SerializedName("idUsuario")
    private int idUsuario;

    // Campos auxiliares para la UI de Despacho y Puntos
    private String tipo;
    private String distancia;
    private int imagenResId;
    private String numeroPedido;
    private String estadoDespacho;
    private String badgeNumero;

    public PuntoVenta() {}

    public PuntoVenta(int idPuntosVenta, String puntosVenta, String direccion, String estado) {
        this(idPuntosVenta, "PDV-" + idPuntosVenta, puntosVenta, "Tienda", direccion, "0.8 km", estado, -12.046374, -77.042793, 1, 1, 0, "0245", "En preparación", String.valueOf(idPuntosVenta));
    }

    public PuntoVenta(int idPuntosVenta, String puntosVenta, String direccion, String numeroPedido, String estadoDespacho, String badgeNumero) {
        this(idPuntosVenta, "PDV-" + idPuntosVenta, puntosVenta, "Tienda", direccion, "0.8 km", "ACTIVO", -12.046374, -77.042793, 1, 1, 0, numeroPedido, estadoDespacho, badgeNumero);
    }

    public PuntoVenta(int idPuntosVenta, String puntosVenta, String tipo, String direccion, String distancia, String estado, int imagenResId) {
        this(idPuntosVenta, "PDV-" + idPuntosVenta, puntosVenta, tipo, direccion, distancia, estado, -12.046374, -77.042793, 1, 1, imagenResId, "0245", "En preparación", String.valueOf(idPuntosVenta));
    }

    public PuntoVenta(int idPuntosVenta, String codigo, String puntosVenta, String tipo, String direccion, String distancia, String estado, double latitud, double longitud, int idDistrito, int idUsuario, int imagenResId, String numeroPedido, String estadoDespacho, String badgeNumero) {
        this.idPuntosVenta = idPuntosVenta;
        this.codigo = codigo;
        this.puntosVenta = puntosVenta;
        this.tipo = tipo;
        this.direccion = direccion;
        this.distancia = distancia;
        this.estado = estado;
        this.latitud = latitud;
        this.longitud = longitud;
        this.idDistrito = idDistrito;
        this.idUsuario = idUsuario;
        this.imagenResId = imagenResId;
        this.numeroPedido = numeroPedido;
        this.estadoDespacho = estadoDespacho;
        this.badgeNumero = badgeNumero;
    }

    public int getIdPuntosVenta() { return idPuntosVenta; }
    public int getId() { return idPuntosVenta; }
    public String getCodigo() { return codigo != null ? codigo : "PDV-" + idPuntosVenta; }
    public String getPuntosVenta() { return puntosVenta; }
    public String getNombre() { return puntosVenta; }
    public String getTipo() { return tipo != null ? tipo : "Tienda"; }
    public String getDireccion() { return direccion; }
    public String getDistancia() { return distancia != null ? distancia : "0.8 km"; }
    public String getEstado() { return estado != null ? estado : "ACTIVO"; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public int getIdDistrito() { return idDistrito; }
    public int getIdUsuario() { return idUsuario; }
    public int getImagenResId() { return imagenResId; }
    public String getNumeroPedido() { return numeroPedido != null ? numeroPedido : "0245"; }
    public String getEstadoDespacho() { return estadoDespacho != null ? estadoDespacho : "En preparación"; }
    public String getBadgeNumero() { return badgeNumero != null ? badgeNumero : String.valueOf(idPuntosVenta); }

    public void setIdPuntosVenta(int idPuntosVenta) { this.idPuntosVenta = idPuntosVenta; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setPuntosVenta(String puntosVenta) { this.puntosVenta = puntosVenta; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public void setIdDistrito(int idDistrito) { this.idDistrito = idDistrito; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setDistancia(String distancia) { this.distancia = distancia; }
    public void setImagenResId(int imagenResId) { this.imagenResId = imagenResId; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }
    public void setEstadoDespacho(String estadoDespacho) { this.estadoDespacho = estadoDespacho; }
    public void setBadgeNumero(String badgeNumero) { this.badgeNumero = badgeNumero; }
}