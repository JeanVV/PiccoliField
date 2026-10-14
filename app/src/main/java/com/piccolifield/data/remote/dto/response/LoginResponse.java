package com.piccolifield.data.remote.dto.response;

import com.piccolifield.data.model.Usuario;

public class LoginResponse extends Usuario {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}