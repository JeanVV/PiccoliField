package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class AtenderIncidenciaRequest {
    @SerializedName("estado")
    private String estado;

    @SerializedName("comentario")
    private String comentario;

    @SerializedName("comentario_atencion")
    private String comentarioAtencion;

    public AtenderIncidenciaRequest(String estado, String comentario) {
        this.estado = estado != null ? estado.trim().toUpperCase() : "RESUELTO";
        this.comentario = comentario;
        this.comentarioAtencion = comentario;
    }

    public String getEstado() { return estado; }
    public String getComentario() { return comentario; }
    public String getComentarioAtencion() { return comentarioAtencion; }
}