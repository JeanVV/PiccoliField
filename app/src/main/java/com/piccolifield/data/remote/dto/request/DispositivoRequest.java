package com.piccolifield.data.remote.dto.request;

public class DispositivoRequest {

    private String fcm_token;
    private String plataforma;
    private String nombre_dispositivo;

    public DispositivoRequest(String fcm_token, String plataforma, String nombre_dispositivo) {
        this.fcm_token = fcm_token;
        this.plataforma = plataforma;
        this.nombre_dispositivo = nombre_dispositivo;
    }
}