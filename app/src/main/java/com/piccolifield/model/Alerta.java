package com.piccolifield.model;

public class Alerta {
    private int id;
    private String titulo;
    private String local;
    private String detalle;
    private String tiempo;
    private String severidad;

    public Alerta(int id, String titulo, String local, String detalle, String tiempo, String severidad) {
        this.id = id;
        this.titulo = titulo;
        this.local = local;
        this.detalle = detalle;
        this.tiempo = tiempo;
        this.severidad = severidad;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getLocal() { return local; }
    public String getDetalle() { return detalle; }
    public String getTiempo() { return tiempo; }
    public String getSeveridad() { return severidad; }
}