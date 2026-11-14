package com.piccolifield.data.repository;

import android.content.Context;

import com.piccolifield.data.local.AppDatabase;
import com.piccolifield.data.local.dao.PedidoDao;
import com.piccolifield.data.local.entity.PedidoEntity;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Pedido;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PedidoRepository {

    private final ApiService apiService;
    private final PedidoDao pedidoDao;
    private final ExecutorService executorService;

    public PedidoRepository(Context context) {
        apiService = RetrofitClient.createService();
        AppDatabase db = AppDatabase.getInstance(context);
        pedidoDao = db.pedidoDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void registrarPedido(Pedido pedido, final PedidoCallback callback) {
        // Primero intentamos enviar a la red
        apiService.registrarPedido(pedido).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String errorDetalle = "Error " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorDetalle = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    
                    callback.onError("Fallo API: " + errorDetalle);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                // Falla de red: Guardar en local (Room)
                guardarPedidoOffline(pedido, callback);
            }
        });
    }

    private void guardarPedidoOffline(Pedido pedido, final PedidoCallback callback) {
        executorService.execute(() -> {
            PedidoEntity entity = new PedidoEntity(
                    pedido.getCantSolicitada(),
                    pedido.getFecha(),
                    pedido.getIdPuntosVenta(),
                    pedido.getIdProductos(),
                    "PENDIENTE"
            );
            pedidoDao.insertarPedido(entity);
            
            // Informar a la vista que se guardó de forma local
            callback.onOfflineSaved();
        });
    }

    public interface PedidoCallback {
        void onSuccess();
        void onError(String mensaje);
        void onOfflineSaved(); // Para avisar que se guardó sin conexión
    }
}