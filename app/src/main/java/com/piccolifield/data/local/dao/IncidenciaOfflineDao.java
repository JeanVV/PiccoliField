package com.piccolifield.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.piccolifield.data.local.entity.IncidenciaOfflineEntity;

import java.util.List;

@Dao
public interface IncidenciaOfflineDao {
    @Insert
    long insertar(IncidenciaOfflineEntity incidencia);

    @Query("SELECT * FROM incidencias_offline WHERE sincronizado = 0")
    List<IncidenciaOfflineEntity> obtenerPendientes();

    @Query("DELETE FROM incidencias_offline WHERE idLocal = :id")
    void eliminarPorId(int id);
}