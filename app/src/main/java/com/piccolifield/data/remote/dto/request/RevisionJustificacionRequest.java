package com.piccolifield.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class RevisionJustificacionRequest {

    @SerializedName(value = "idUsuario", alternate = {"id_usuario", "id_usuario_colaborador", "usuarioId", "idColaborador"})
    private int idUsuario;

    @SerializedName(value = "idAsistencias", alternate = {"id_asistencia", "idAsistencia", "asistenciaId"})
    private int idAsistencias;

    @SerializedName(value = "idRevisor", alternate = {"id_revisor", "revisorId"})
    private int idRevisor;

    @SerializedName(value = "estado", alternate = {"estadoNuevo", "estado_nuevo"})
    private String estado;

    @SerializedName(value = "comentarioRevision", alternate = {"comentario_revision", "comentarioAdmin", "comentario_admin", "comentario"})
    private String comentarioRevision;

    public RevisionJustificacionRequest() {}

    public RevisionJustificacionRequest(int idUsuario, int idRevisor, String estado, String comentarioRevision) {
        this.idUsuario = idUsuario;
        this.idRevisor = idRevisor;
        this.estado = estado;
        this.comentarioRevision = comentarioRevision;
    }

    public RevisionJustificacionRequest(int idUsuario, int idAsistencias, int idRevisor, String estado, String comentarioRevision) {
        this.idUsuario = idUsuario;
        this.idAsistencias = idAsistencias;
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

    public int getIdAsistencias() {
        return idAsistencias;
    }

    public void setIdAsistencias(int idAsistencias) {
        this.idAsistencias = idAsistencias;
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
