package com.piccolifield.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class FcmTokenRequest {

    @SerializedName("fcm_token")
    private String fcmToken;

    @SerializedName("id_dispositivo")
    private String idDispositivo;

    public FcmTokenRequest(String fcmToken, String idDispositivo) {
        this.fcmToken = fcmToken;
        this.idDispositivo = idDispositivo;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(String idDispositivo) {
        this.idDispositivo = idDispositivo;
    }
}