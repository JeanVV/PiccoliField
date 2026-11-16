package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class ActualizarFotoUsuarioRequest {

    @SerializedName("url_foto")
    private String urlFoto;

    public ActualizarFotoUsuarioRequest(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }
}
