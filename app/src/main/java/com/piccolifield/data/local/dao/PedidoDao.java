package com.piccolifield.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.piccolifield.data.local.entity.PedidoEntity;

import java.util.List;

@Dao
public interface PedidoDao {

    @Insert
    void insertarPedido(PedidoEntity pedido);

    @Update
    void actualizarPedido(PedidoEntity pedido);

    @Delete
    void eliminarPedido(PedidoEntity pedido);

    @Query("SELECT * FROM pedidos_offline WHERE estadoSincronizacion = 'PENDIENTE'")
    List<PedidoEntity> obtenerPedidosPendientes();
    
    @Query("SELECT * FROM pedidos_offline WHERE idPuntosVenta = :idPuntoVenta")
    List<PedidoEntity> obtenerPedidosPorPuntoVenta(int idPuntoVenta);

    @Query("DELETE FROM pedidos_offline WHERE estadoSincronizacion = 'SINCRONIZADO'")
    void limpiarSincronizados();
}