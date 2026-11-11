package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class ResolverAlertaRequest {
    @SerializedName("comentario")
    private String comentario;

    public ResolverAlertaRequest(String comentario) {
        this.comentario = comentario;
    }

    public String getComentario() { return comentario; }
}