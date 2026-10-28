package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PuntoVentaDetalle {
    @SerializedName("idPuntosVenta")
    private int idPuntosVenta;

    @SerializedName("codigo")
    private String codigo;

    @SerializedName("puntoVenta")
    private String puntoVenta;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("estado")
    private String estado;

    @SerializedName("latitud")
    private double latitud;

    @SerializedName("longitud")
    private double longitud;

    @SerializedName("radio_geocerca")
    private double radioGeocerca;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("idDistrito")
    private int idDistrito;

    @SerializedName("distrito")
    private String distrito;

    @SerializedName("idProvincia")
    private int idProvincia;

    @SerializedName("provincia")
    private String provincia;

    @SerializedName("idDepartamento")
    private int idDepartamento;

    @SerializedName("departamento")
    private String departamento;

    @SerializedName("idImpulsador")
    private Integer idImpulsador;

    @SerializedName("impulsador_nombre")
    private String impulsadorNombre;

    @SerializedName("impulsador_correo")
    private String impulsadorCorreo;

    @SerializedName("impulsador_foto")
    private String impulsadorFoto;

    @SerializedName("impulsador_meta")
    private Integer impulsadorMeta;

    @SerializedName("productos")
    private List<PuntoVentaProducto> productos;

    public int getIdPuntosVenta() { return idPuntosVenta; }
    public void setIdPuntosVenta(int idPuntosVenta) { this.idPuntosVenta = idPuntosVenta; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getPuntoVenta() { return puntoVenta; }
    public void setPuntoVenta(String puntoVenta) { this.puntoVenta = puntoVenta; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public double getRadioGeocerca() { return radioGeocerca; }
    public void setRadioGeocerca(double radioGeocerca) { this.radioGeocerca = radioGeocerca; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getIdDistrito() { return idDistrito; }
    public void setIdDistrito(int idDistrito) { this.idDistrito = idDistrito; }

    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }

    public int getIdProvincia() { return idProvincia; }
    public void setIdProvincia(int idProvincia) { this.idProvincia = idProvincia; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public int getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(int idDepartamento) { this.idDepartamento = idDepartamento; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public Integer getIdImpulsador() { return idImpulsador; }
    public void setIdImpulsador(Integer idImpulsador) { this.idImpulsador = idImpulsador; }

    public String getImpulsadorNombre() { return impulsadorNombre; }
    public void setImpulsadorNombre(String impulsadorNombre) { this.impulsadorNombre = impulsadorNombre; }

    public String getImpulsadorCorreo() { return impulsadorCorreo; }
    public void setImpulsadorCorreo(String impulsadorCorreo) { this.impulsadorCorreo = impulsadorCorreo; }

    public String getImpulsadorFoto() { return impulsadorFoto; }
    public void setImpulsadorFoto(String impulsadorFoto) { this.impulsadorFoto = impulsadorFoto; }

    public Integer getImpulsadorMeta() { return impulsadorMeta; }
    public void setImpulsadorMeta(Integer impulsadorMeta) { this.impulsadorMeta = impulsadorMeta; }

    public List<PuntoVentaProducto> getProductos() { return productos; }
    public void setProductos(List<PuntoVentaProducto> productos) { this.productos = productos; }
}