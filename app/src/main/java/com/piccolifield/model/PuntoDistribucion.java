package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class PuntoDistribucion {

    @SerializedName(value = "idPuntosVenta", alternate = {"id", "idPdv", "id_puntos_venta", "idPuntoVenta"})
    private int idPuntosVenta;

    @SerializedName(value = "codigo", alternate = {"codigo_pdv", "codigoPdv", "cod", "codigo_punto", "codigoPunto"})
    private String codigo;

    @SerializedName(value = "puntoVenta", alternate = {
            "puntosVenta", "nombre", "nombrePdv", "tienda",
            "punto_venta", "puntos_venta", "nombre_punto",
            "nombre_local", "nombreLocal", "razon_social", "local"
    })
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

    @SerializedName(value = "tipo", alternate = {
            "tipo_negocio", "tipoNegocio", "criterio", "modalidad",
            "tipo_cliente", "tipoCliente", "tipo_punto", "subtipo"
    })
    private String tipo;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName(value = "url_foto", alternate = {"fotoUrl", "foto_fachada", "foto"})
    private String fotoUrl;

    // Campos auxiliares para la UI de Despacho y Puntos
    private String distancia;
    private int imagenResId;
    private String numeroPedido;
    private String estadoDespacho;
    private String badgeNumero;

    public PuntoDistribucion() {}

    public PuntoDistribucion(int idPuntosVenta, String puntosVenta, String direccion, String estado) {
        this(idPuntosVenta, String.format("PDV-%03d", idPuntosVenta), puntosVenta,
                (idPuntosVenta % 2 == 0 ? "Mayorista" : "Minorista"), direccion, "0.8 km",
                estado, -12.046374, -77.042793, 1, 1, 0, "0245", "", String.valueOf(idPuntosVenta));
    }

    public PuntoDistribucion(int idPuntosVenta, String puntosVenta, String direccion, String numeroPedido, String estadoDespacho, String badgeNumero) {
        this(idPuntosVenta, String.format("PDV-%03d", idPuntosVenta), puntosVenta,
                (idPuntosVenta % 2 == 0 ? "Mayorista" : "Minorista"), direccion, "0.8 km",
                "ACTIVO", -12.046374, -77.042793, 1, 1, 0, numeroPedido, estadoDespacho, badgeNumero);
    }

    public PuntoDistribucion(int idPuntosVenta, String puntosVenta, String tipo, String direccion, String distancia, String estado, int imagenResId) {
        this(idPuntosVenta, String.format("PDV-%03d", idPuntosVenta), puntosVenta, tipo, direccion,
                distancia, estado, -12.046374, -77.042793, 1, 1, imagenResId, "0245", "", String.valueOf(idPuntosVenta));
    }

    public PuntoDistribucion(int idPuntosVenta, String codigo, String puntosVenta, String tipo, String direccion, String distancia, String estado, double latitud, double longitud, int idDistrito, int idUsuario, int imagenResId, String numeroPedido, String estadoDespacho, String badgeNumero) {
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

    public String getCodigo() {
        if (codigo != null && !codigo.trim().isEmpty()) {
            return codigo.trim();
        }
        return String.format("PDV-%03d", idPuntosVenta > 0 ? idPuntosVenta : 1);
    }

    public String getPuntosVenta() { return getNombre(); }

    public String getNombre() {
        if (puntosVenta != null && !puntosVenta.trim().isEmpty()
                && !puntosVenta.trim().equalsIgnoreCase("Punto de Distribución")
                && !puntosVenta.trim().equalsIgnoreCase("Punto de distribucion")) {
            return puntosVenta.trim();
        }
        String[] nombresFallback = {
                "Licorería El Buen Sabor",
                "Distribuidora San Martín",
                "Minimarket Los Olivos",
                "Comercial Mayorista Sur",
                "Bodega Don Pepe",
                "Autoservicio La Estación"
        };
        int idx = Math.abs(idPuntosVenta) % nombresFallback.length;
        return nombresFallback[idx];
    }

    public String getTipo() {
        return getCriterioNegocio();
    }

    public String getCriterioNegocio() {
        if (tipo != null && !tipo.trim().isEmpty()) {
            String t = tipo.trim().toUpperCase();
            if (t.contains("MAYORISTA") || t.contains("SUPERMERCADO") || t.contains("DISTRIBUIDOR") || t.contains("MAYOR")) {
                return "Mayorista";
            }
            if (t.contains("MINORISTA") || t.contains("BODEGA") || t.contains("MINIMARKET") || t.contains("LICORER") || t.contains("TIENDA") || t.contains("MENOR")) {
                return "Minorista";
            }
        }
        if (categoria != null && !categoria.trim().isEmpty()) {
            String c = categoria.trim().toUpperCase();
            if (c.contains("MAYORISTA")) return "Mayorista";
            if (c.contains("MINORISTA")) return "Minorista";
        }
        return (idPuntosVenta > 0 && idPuntosVenta % 2 == 0) ? "Mayorista" : "Minorista";
    }

    public boolean esMayorista() {
        return "Mayorista".equalsIgnoreCase(getCriterioNegocio());
    }

    public String getCategoria() { return categoria; }
    public String getDireccion() { return direccion != null && !direccion.trim().isEmpty() ? direccion : "Av. Principal 123, Lima"; }
    public String getDistancia() { return distancia != null ? distancia : "0.8 km"; }
    public String getEstado() { return estado != null ? estado : "ACTIVO"; }
    public double getLatitud() { return latitud != 0.0 ? latitud : -12.046374; }
    public double getLongitud() { return longitud != 0.0 ? longitud : -77.042793; }
    public int getIdDistrito() { return idDistrito > 0 ? idDistrito : 1; }
    public int getIdUsuario() { return idUsuario; }
    public int getImagenResId() { return imagenResId; }
    public String getNumeroPedido() { return numeroPedido != null ? numeroPedido : getCodigo(); }
    public String getEstadoDespacho() { return estadoDespacho != null ? estadoDespacho : ""; }
    public String getBadgeNumero() { return badgeNumero != null ? badgeNumero : String.valueOf(idPuntosVenta > 0 ? idPuntosVenta : 1); }
    public String getFotoUrl() { return fotoUrl; }

    public int getIdProspectoOrigen() {
        String cod = getCodigo();
        if (cod != null && cod.toUpperCase().startsWith("DIST-")) {
            try {
                return Integer.parseInt(cod.substring(5).trim());
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

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
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setDistancia(String distancia) { this.distancia = distancia; }
    public void setImagenResId(int imagenResId) { this.imagenResId = imagenResId; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }
    public void setEstadoDespacho(String estadoDespacho) { this.estadoDespacho = estadoDespacho; }
    public void setBadgeNumero(String badgeNumero) { this.badgeNumero = badgeNumero; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
