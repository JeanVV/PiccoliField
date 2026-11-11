package com.piccolifield.model;

import com.google.gson.annotations.SerializedName;

public class Alerta {

    @SerializedName(value = "idAlertas", alternate = {"idAlerta", "id", "id_alerta", "id_alertas"})
    private int idAlerta;

    @SerializedName("nivel_alerta")
    private String nivelAlerta;

    @SerializedName("estado_resolucion")
    private String estadoResolucion;

    @SerializedName("local_nombre")
    private String localNombre;

    @SerializedName("nombre_producto")
    private String nombreProducto;

    public Alerta() {}

    public int getId() { return idAlerta; }
    public String getTitulo() { return nivelAlerta != null ? nivelAlerta : "Alerta de Stock"; }
    public String getLocal() { return localNombre != null ? localNombre : "Sin local"; }
    public String getDetalle() { return "Producto: " + (nombreProducto != null ? nombreProducto : "Desconocido"); }
    public String getTiempo() { return "Reciente"; }
    public String getSeveridad() { return nivelAlerta != null ? nivelAlerta : "Media"; }
    public String getNivelFormateado() {
        if (nivelAlerta == null) return "STOCK BAJO";
        switch (nivelAlerta.toUpperCase()) {
            case "PRODUCTO_AGOTADO":
                return "PRODUCTO AGOTADO";
            case "STOCK_BAJO":
                return "STOCK BAJO";
            default:
                return nivelAlerta.replace("_", " ").toUpperCase();
        }
    }
    public String getEstado() { return estadoResolucion != null ? estadoResolucion : "PENDIENTE"; }
    public String getNombreProducto() { return nombreProducto; }

    public void setIdAlerta(int idAlerta) { this.idAlerta = idAlerta; }
    public void setNivelAlerta(String nivelAlerta) { this.nivelAlerta = nivelAlerta; }
    public void setEstadoResolucion(String estadoResolucion) { this.estadoResolucion = estadoResolucion; }
    public void setLocalNombre(String localNombre) { this.localNombre = localNombre; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
}