package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class NotificacionItem {
    @SerializedName("idNotificacion")
    private int idNotificacion;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("tipo_original")
    private String tipoOriginal;

    @SerializedName("id")
    private String id;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("mensaje")
    private String mensaje;

    @SerializedName("referencia")
    private String referencia;

    @SerializedName("fecha")
    private String fecha;

    public int getIdNotificacion() { return idNotificacion; }
    public String getTipo() { return tipo != null ? tipo : ""; }
    public String getTipoOriginal() { return tipoOriginal; }
    public String getId() { return id != null ? id : "0"; }
    public String getTitulo() { return titulo != null ? titulo : "Notificación"; }
    public String getMensaje() { return mensaje != null ? mensaje : ""; }
    public String getReferencia() { return referencia; }
    public String getFecha() { return fecha != null ? fecha : ""; }
}