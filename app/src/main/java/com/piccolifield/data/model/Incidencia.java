package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

public class Incidencia {
    @SerializedName(value = "idIncidencias", alternate = {"id", "id_incidencia", "idIncidencia"})
    private int idIncidencias;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("severidad")
    private String severidad;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("estado")
    private String estado;

    @SerializedName("fecha_registro")
    private String fechaRegistro;

    @SerializedName("comentario_atencion")
    private String comentarioAtencion;

    @SerializedName("fecha_atencion")
    private String fechaAtencion;

    @SerializedName("local_nombre")
    private String localNombre;

    @SerializedName(value = "responsable_nombre", alternate = {"impulsador_nombre"})
    private String responsableNombre;

    @SerializedName(value = "responsable_correo", alternate = {"impulsador_correo"})
    private String responsableCorreo;

    @SerializedName("tiene_evidencia")
    private int tieneEvidencia;

    @SerializedName("url_foto_evidencia")
    private String urlFotoEvidencia;

    public int getIdIncidencias() { return idIncidencias; }
    public String getTipo() { return tipo != null ? tipo : "Incidencia"; }
    public String getSeveridad() { return severidad != null ? severidad : "MEDIA"; }
    public String getDescripcion() { return descripcion != null ? descripcion : ""; }
    public String getEstado() { return estado != null ? estado : "ABIERTO"; }
    public String getFechaRegistro() { return fechaRegistro != null ? fechaRegistro : ""; }
    public String getComentarioAtencion() { return comentarioAtencion; }
    public String getFechaAtencion() { return fechaAtencion; }
    public String getLocalNombre() { return localNombre != null ? localNombre : "Punto de Venta"; }
    public String getResponsableNombre() { return responsableNombre != null ? responsableNombre : "Impulsador"; }
    public String getResponsableCorreo() { return responsableCorreo; }
    public boolean hasEvidencia() { return tieneEvidencia == 1 || (urlFotoEvidencia != null && !urlFotoEvidencia.isEmpty()); }
}