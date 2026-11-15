package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class RevisionIncidenciaRequest {
    @SerializedName("estado")
    private String estado;

    @SerializedName("comentario_atencion")
    private String comentarioAtencion;

    public RevisionIncidenciaRequest(String estado, String comentarioAtencion) {
        this.estado = estado;
        this.comentarioAtencion = comentarioAtencion;
    }
}