package com.piccolifield.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.piccolifield.data.local.entity.ProductoCacheEntity;

import java.util.List;

@Dao
public interface ProductoCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardarProductos(List<ProductoCacheEntity> productos);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardarProducto(ProductoCacheEntity producto);

    @Query("SELECT * FROM productos_cache WHERE codigoEan = :ean LIMIT 1")
    ProductoCacheEntity buscarPorEan(String ean);

    @Query("SELECT * FROM productos_cache")
    List<ProductoCacheEntity> obtenerTodos();
}