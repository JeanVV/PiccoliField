package com.piccolifield.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.piccolifield.data.local.entity.ReposicionOfflineEntity;

import java.util.List;

@Dao
public interface ReposicionOfflineDao {
    @Insert
    long insertar(ReposicionOfflineEntity reposicion);

    @Query("SELECT * FROM reposiciones_offline WHERE sincronizado = 0")
    List<ReposicionOfflineEntity> obtenerPendientes();

    @Query("DELETE FROM reposiciones_offline WHERE idLocal = :id")
    void eliminarPorId(int id);
}