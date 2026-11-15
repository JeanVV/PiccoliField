package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotificacionResponseData {
    @SerializedName("total")
    private int total;

    @SerializedName("notificaciones")
    private List<NotificacionItem> notificaciones;

    public int getTotal() { return total; }
    public List<NotificacionItem> getNotificaciones() { return notificaciones; }
}