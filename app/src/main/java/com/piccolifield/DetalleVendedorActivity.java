package com.piccolifield; // Asegúrate de que el paquete sea el correcto

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.piccolifield.data.model.VendedorDetalle;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.dto.RetrofitClient; // Cambia esto si tu RetrofitClient está en otra carpeta
import com.piccolifield.sharedpreferences.SplashStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleVendedorActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvNombreVendedor, tvPuntoVenta, tvMontoTotal, tvProgresoAsignacion;
    private ProgressBar progressBarAsignacion;

    private int idUsuarioSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_vendedor);

        // 1. Vincular los IDs EXACTOS de tu XML
        btnBack = findViewById(R.id.btnBack);
        tvNombreVendedor = findViewById(R.id.tvNombreVendedor);
        tvPuntoVenta = findViewById(R.id.tvPuntoVenta);
        tvMontoTotal = findViewById(R.id.tvMontoTotal);

        // Aquí corregimos los IDs por los que usaste en el XML
        tvProgresoAsignacion = findViewById(R.id.tvPorcentaje);
        progressBarAsignacion = findViewById(R.id.pbVentas);

        btnBack.setOnClickListener(v -> finish());
        idUsuarioSeleccionado = getIntent().getIntExtra("ID_VENDEDOR", -1);

        if (idUsuarioSeleccionado == -1) {
            Toast.makeText(this, "Error al cargar el vendedor", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        cargarDatosVendedor();
    }

    private void cargarDatosVendedor() {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = SplashStorage.obtenerToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.API_TOKEN = token;
        ApiService apiService = RetrofitClient.createService();

        // 4. Llamar a la API SIN el Bearer (porque ya lo hace tu Interceptor)
        apiService.obtenerDetalleVendedor(idUsuarioSeleccionado).enqueue(new Callback<ApiResponse<VendedorDetalle>>() {
            @Override
            public void onResponse(Call<ApiResponse<VendedorDetalle>> call, Response<ApiResponse<VendedorDetalle>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    VendedorDetalle vendedor = response.body().getData();

                    if (vendedor != null) {
                        tvNombreVendedor.setText(vendedor.getNombre());
                        tvPuntoVenta.setText("📍 " + vendedor.getLocal_pertenece());
                        tvMontoTotal.setText("S/ " + String.format("%.2f", vendedor.getMonto_vendido()));

                        int porcentajeInt = (int) Math.round(vendedor.getPorcentaje_logro());

                        // Modificamos el texto para que diga el % en el formato de tu diseño
                        tvProgresoAsignacion.setText(porcentajeInt + "%");
                        progressBarAsignacion.setProgress(porcentajeInt);
                    }

                } else {
                    Toast.makeText(DetalleVendedorActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<VendedorDetalle>> call, Throwable t) {
                Toast.makeText(DetalleVendedorActivity.this, "Falla de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}