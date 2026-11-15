package com.piccolifield.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TiposIncidenciaResponse {
    @SerializedName("tipos")
    private List<TipoIncidenciaDto> tipos;

    @SerializedName("severidades")
    private List<String> severidades;

    public List<TipoIncidenciaDto> getTipos() { return tipos; }
    public List<String> getSeveridades() { return severidades; }
}