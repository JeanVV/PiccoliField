package com.piccolifield.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.piccolifield.data.local.entity.VentaOfflineEntity;

import java.util.List;

@Dao
public interface VentaOfflineDao {
    @Insert
    long insertar(VentaOfflineEntity venta);

    @Query("SELECT * FROM ventas_offline WHERE sincronizado = 0")
    List<VentaOfflineEntity> obtenerPendientes();

    @Update
    void actualizar(VentaOfflineEntity venta);

    @Query("DELETE FROM ventas_offline WHERE idLocal = :id")
    void eliminarPorId(int id);
}