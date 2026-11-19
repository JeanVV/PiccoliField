package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SedeDto implements Serializable {

    @SerializedName(value = "idSede", alternate = {"id_sede", "id"})
    private int idSede;

    @SerializedName(value = "sede", alternate = {"nombre", "nombreSede", "nombre_sede"})
    private String sede;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName(value = "latitud", alternate = {"lat"})
    private double latitud;

    @SerializedName(value = "longitud", alternate = {"lng", "lon"})
    private double longitud;

    @SerializedName(value = "radio_geocerca", alternate = {"radioGeocerca", "radio", "radio_m"})
    private float radioGeocerca;

    public SedeDto() {}

    public SedeDto(int idSede, String sede, double latitud, double longitud, float radioGeocerca) {
        this.idSede = idSede;
        this.sede = sede;
        this.latitud = latitud;
        this.longitud = longitud;
        this.radioGeocerca = radioGeocerca;
    }

    public int getIdSede() { return idSede; }
    public void setIdSede(int idSede) { this.idSede = idSede; }

    public String getSede() { return sede != null ? sede : "Sede Principal"; }
    public void setSede(String sede) { this.sede = sede; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public float getRadioGeocerca() { return radioGeocerca > 0 ? radioGeocerca : 100f; }
    public void setRadioGeocerca(float radioGeocerca) { this.radioGeocerca = radioGeocerca; }
}
