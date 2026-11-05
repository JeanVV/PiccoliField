package com.piccolifield;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.piccolifield.adapter.PedidoAdapter;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Pedido;
import com.piccolifield.model.PuntoDistribucion;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.piccolifield.ui.despacho.EstadoPedidoBottomSheet;
import com.piccolifield.ui.despacho.NuevoPedidoBottomSheet;

public class DetallePuntoDistribucionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvBadgeNumero, tvNombreTienda, tvEstadoBadge, tvDireccionVal, tvDistanciaVal, tvPedidoNumVal, tvRepartidorVal, btnNuevoPedido, tvPedidosSub;
    private MaterialButton btnVerRuta;
    private RecyclerView rvPedidos;
    
    private PedidoAdapter pedidoAdapter;
    private List<Pedido> listaPedidos = new ArrayList<>();

    private int idPuntoVenta;
    private String nombrePuntoVenta;
    private String numeroPedido = "0245";
    private String estadoDespacho = "En preparación";
    private PuntoDistribucion puntoDistribucionInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_punto_distribucion);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.color_fondo));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.color_fondo));

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Vincular Vistas
        btnBack = findViewById(R.id.btnBack);
        tvBadgeNumero = findViewById(R.id.tvBadgeNumero);
        tvNombreTienda = findViewById(R.id.tvNombreTienda);
        tvEstadoBadge = findViewById(R.id.tvEstadoBadge);
        tvDireccionVal = findViewById(R.id.tvDireccionVal);
        tvDistanciaVal = findViewById(R.id.tvDistanciaVal);
        tvPedidoNumVal = findViewById(R.id.tvPedidoNumVal);
        tvRepartidorVal = findViewById(R.id.tvRepartidorVal);
        btnNuevoPedido = findViewById(R.id.btnNuevoPedido);
        tvPedidosSub = findViewById(R.id.tvPedidosSub);
        btnVerRuta = findViewById(R.id.btnVerRuta);
        rvPedidos = findViewById(R.id.rvPedidos);

        rvPedidos.setLayoutManager(new LinearLayoutManager(this));
        pedidoAdapter = new PedidoAdapter(listaPedidos, pedido -> {
            EstadoPedidoBottomSheet bottomSheet = new EstadoPedidoBottomSheet(pedido, () -> {
                cargarPedidos(idPuntoVenta); // Recargar la lista para mostrar el nuevo estado
            });
            bottomSheet.show(getSupportFragmentManager(), "EstadoPedido");
        });
        rvPedidos.setAdapter(pedidoAdapter);

        // 2. Recibir los datos enviados por Intent (Solo el ID en el flujo real)
        if (getIntent() != null) {
            idPuntoVenta = getIntent().getIntExtra("ID_PDV", -1);
            nombrePuntoVenta = getIntent().getStringExtra("NOMBRE_PDV");
            
            // Cargar de red (Descomentar para usar Data real)
            cargarDetallePunto(idPuntoVenta);
            cargarPedidos(idPuntoVenta);
        }

        // 3. Listener del botón atrás
        btnBack.setOnClickListener(v -> finish());

        // 4. Listener Nuevo Pedido
        if (btnNuevoPedido != null) {
            btnNuevoPedido.setOnClickListener(v -> {
                NuevoPedidoBottomSheet bottomSheet = new NuevoPedidoBottomSheet(idPuntoVenta, () -> {
                    cargarPedidos(idPuntoVenta);
                });
                bottomSheet.show(getSupportFragmentManager(), "NuevoPedido");
            });
        }

        // 5. Listener Ver Ruta de Entrega
        if (btnVerRuta != null) {
            btnVerRuta.setOnClickListener(v -> 
                Toast.makeText(this, "Navegando a ruta de entrega", Toast.LENGTH_SHORT).show()
            );
        }
    }
    
    private void cargarDetallePunto(int id) {
        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerDetallePunto(id).enqueue(new Callback<ApiResponse<PuntoDistribucion>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PuntoDistribucion>> call, @NonNull Response<ApiResponse<PuntoDistribucion>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    puntoDistribucionInfo = response.body().getData();
                    tvNombreTienda.setText(puntoDistribucionInfo.getNombre());
                    tvBadgeNumero.setText(String.valueOf(puntoDistribucionInfo.getId()));
                    tvDireccionVal.setText(puntoDistribucionInfo.getDireccion());
                    tvEstadoBadge.setText(puntoDistribucionInfo.getEstado());
                } else {
                    // Fallback
                    tvNombreTienda.setText(nombrePuntoVenta != null ? nombrePuntoVenta : "Punto Desconocido");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PuntoDistribucion>> call, @NonNull Throwable t) {
                tvNombreTienda.setText(nombrePuntoVenta != null ? nombrePuntoVenta : "Punto Desconocido");
            }
        });
    }

    private void cargarPedidos(int idPdv) {
        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerPedidosPorPdv(idPdv).enqueue(new Callback<ApiResponse<List<Pedido>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Pedido>>> call, @NonNull Response<ApiResponse<List<Pedido>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaPedidos.clear();
                    listaPedidos.addAll(response.body().getData());
                    pedidoAdapter.notifyDataSetChanged();
                    tvPedidosSub.setText(listaPedidos.size() + " pedidos registrados");
                } else {
                    // Cargar mock si el backend falla
                    cargarPedidosMock();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Pedido>>> call, @NonNull Throwable t) {
                cargarPedidosMock();
            }
        });
    }

    private void cargarPedidosMock() {
        listaPedidos.clear();
        listaPedidos.add(new Pedido(1001, 10, "12 Oct 2023", idPuntoVenta, 1));
        listaPedidos.add(new Pedido(1002, 5, "10 Oct 2023", idPuntoVenta, 2));
        pedidoAdapter.notifyDataSetChanged();
        tvPedidosSub.setText(listaPedidos.size() + " pedidos registrados (Mock)");
    }
}