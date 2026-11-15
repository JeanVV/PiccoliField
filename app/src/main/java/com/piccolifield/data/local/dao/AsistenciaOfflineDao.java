package com.piccolifield.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.piccolifield.data.local.entity.AsistenciaOfflineEntity;

import java.util.List;

@Dao
public interface AsistenciaOfflineDao {
    @Insert
    long insertar(AsistenciaOfflineEntity asistencia);

    @Query("SELECT * FROM asistencias_offline WHERE sincronizado = 0")
    List<AsistenciaOfflineEntity> obtenerPendientes();

    @Query("DELETE FROM asistencias_offline WHERE idLocal = :id")
    void eliminarPorId(int id);
}