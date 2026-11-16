package com.piccolifield.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    private T data;
    private String message;
    private boolean status;

    @SerializedName("success")
    private boolean success;

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status || success;
    }

    public boolean isSuccess() {
        return success || status;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
