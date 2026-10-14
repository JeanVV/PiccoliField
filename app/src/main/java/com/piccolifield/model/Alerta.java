package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class Alerta {

    @SerializedName(value = "idAlertas", alternate = {"id", "id_alerta"})
    private int idAlertas;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("estado") // ENUM('PENDIENTE', 'ATENDIDA', 'IGNORADA')
    private String estado;

    @SerializedName("comentario")
    private String comentario;

    @SerializedName("fecha_atencion")
    private String fechaAtencion;

    @SerializedName("idProductos")
    private int idProductos;

    @SerializedName("idPuntosVenta")
    private int idPuntosVenta;

    // Campos auxiliares para la UI
    private String titulo;
    private String local;
    private String detalle;
    private String tiempo;
    private String severidad;

    public Alerta() {}

    public Alerta(int idAlertas, String titulo, String local, String detalle, String tiempo, String severidad) {
        this.idAlertas = idAlertas;
        this.tipo = titulo;
        this.titulo = titulo;
        this.local = local;
        this.detalle = detalle;
        this.tiempo = tiempo;
        this.severidad = severidad;
        this.estado = "PENDIENTE";
    }

    public int getIdAlertas() { return idAlertas; }
    public int getId() { return idAlertas; }
    public String getTipo() { return tipo != null ? tipo : titulo; }
    public String getTitulo() { return titulo != null ? titulo : tipo; }
    public String getLocal() { return local != null ? local : "Punto de Venta #" + idPuntosVenta; }
    public String getDetalle() { return detalle != null ? detalle : comentario; }
    public String getTiempo() { return tiempo != null ? tiempo : "Hace un momento"; }
    public String getSeveridad() { return severidad != null ? severidad : "Crítica"; }
    public String getEstado() { return estado; }
    public String getComentario() { return comentario; }
    public int getIdProductos() { return idProductos; }
    public int getIdPuntosVenta() { return idPuntosVenta; }

    public void setIdAlertas(int idAlertas) { this.idAlertas = idAlertas; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public void setFechaAtencion(String fechaAtencion) { this.fechaAtencion = fechaAtencion; }
    public void setIdProductos(int idProductos) { this.idProductos = idProductos; }
    public void setIdPuntosVenta(int idPuntosVenta) { this.idPuntosVenta = idPuntosVenta; }
}