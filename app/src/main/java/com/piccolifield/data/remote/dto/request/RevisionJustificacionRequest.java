package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class RevisionJustificacionRequest {

    @SerializedName("idUsuario")
    private int idUsuario;

    @SerializedName("idRevisor")
    private int idRevisor;

    @SerializedName("estado") // "APROBADA" o "RECHAZADA"
    private String estado;

    @SerializedName("comentarioRevision")
    private String comentarioRevision;

    public RevisionJustificacionRequest() {}

    public RevisionJustificacionRequest(int idUsuario, int idRevisor, String estado, String comentarioRevision) {
        this.idUsuario = idUsuario;
        this.idRevisor = idRevisor;
        this.estado = estado;
        this.comentarioRevision = comentarioRevision;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdRevisor() {
        return idRevisor;
    }

    public void setIdRevisor(int idRevisor) {
        this.idRevisor = idRevisor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getComentarioRevision() {
        return comentarioRevision;
    }

    public void setComentarioRevision(String comentarioRevision) {
        this.comentarioRevision = comentarioRevision;
    }
}
