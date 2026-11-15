package com.piccolifield.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.piccolifield.data.local.dao.AsistenciaOfflineDao;
import com.piccolifield.data.local.dao.IncidenciaOfflineDao;
import com.piccolifield.data.local.dao.PedidoDao;
import com.piccolifield.data.local.dao.ProductoCacheDao;
import com.piccolifield.data.local.dao.ReposicionOfflineDao;
import com.piccolifield.data.local.dao.VentaOfflineDao;
import com.piccolifield.data.local.entity.AsistenciaOfflineEntity;
import com.piccolifield.data.local.entity.IncidenciaOfflineEntity;
import com.piccolifield.data.local.entity.PedidoEntity;
import com.piccolifield.data.local.entity.ProductoCacheEntity;
import com.piccolifield.data.local.entity.ReposicionOfflineEntity;
import com.piccolifield.data.local.entity.VentaOfflineEntity;

@Database(
    entities = {
        VentaOfflineEntity.class,
        IncidenciaOfflineEntity.class,
        AsistenciaOfflineEntity.class,
        ProductoCacheEntity.class,
        PedidoEntity.class,
        ReposicionOfflineEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "piccolifield_db";
    private static volatile AppDatabase INSTANCE;

    public abstract VentaOfflineDao ventaOfflineDao();
    public abstract IncidenciaOfflineDao incidenciaOfflineDao();
    public abstract AsistenciaOfflineDao asistenciaOfflineDao();
    public abstract ProductoCacheDao productoCacheDao();
    public abstract PedidoDao pedidoDao();
    public abstract ReposicionOfflineDao reposicionOfflineDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}