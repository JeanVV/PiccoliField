package com.piccolifield;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.piccolifield.data.model.AlertaDetalle;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.sharedpreferences.SplashStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleAlertaActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvEstadoAlerta, tvNombreLocal, tvDireccionLocal;
    private TextView tvNivelAlerta, tvProductoNombre, tvStockActual, tvStockMinimo;
    private TextView tvResponsableNombre, tvResponsableRol;

    private int idAlertaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_alerta);

        // 1. Vincular los IDs (Revisa que estos sean los mismos de tu diseño XML)
        btnBack = findViewById(R.id.btnBack);
        tvEstadoAlerta = findViewById(R.id.tvEstadoAlerta);
        tvNombreLocal = findViewById(R.id.tvNombreLocal);
        tvDireccionLocal = findViewById(R.id.tvDireccionLocal);
        tvNivelAlerta = findViewById(R.id.tvNivelAlerta);
        tvProductoNombre = findViewById(R.id.tvProductoNombre);
        tvStockActual = findViewById(R.id.tvStockActual);
        tvStockMinimo = findViewById(R.id.tvStockMinimo);
        tvResponsableNombre = findViewById(R.id.tvResponsableNombre);
        tvResponsableRol = findViewById(R.id.tvResponsableRol);

        // Botón de retroceso
        btnBack.setOnClickListener(v -> finish());

        // 2. Recibir el ID de la alerta (temporalmente pondremos 1 por defecto para que puedas probarlo directo)
        idAlertaSeleccionada = getIntent().getIntExtra("ID_ALERTA", 1);

        if (idAlertaSeleccionada == -1) {
            Toast.makeText(this, getString(R.string.error_cargar_alerta), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Llamamos al backend
        cargarDatosAlerta();
    }

    private void cargarDatosAlerta() {
        String token = SplashStorage.obtenerToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.API_TOKEN = token;
        ApiService apiService = RetrofitClient.createService();

        // 3. Llamar a la ruta /api/supervision/alertas/1
        apiService.obtenerDetalleAlerta(idAlertaSeleccionada).enqueue(new Callback<ApiResponse<AlertaDetalle>>() {
            @Override
            public void onResponse(Call<ApiResponse<AlertaDetalle>> call, Response<ApiResponse<AlertaDetalle>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    AlertaDetalle alerta = response.body().getData();

                    if (alerta != null) {
                        // Pintamos todos los datos que vienen del Postman
                        if (tvEstadoAlerta != null) tvEstadoAlerta.setText(getString(R.string.label_estado_alerta, alerta.getEstadoResolucion()));
                        if (tvNivelAlerta != null) tvNivelAlerta.setText(getString(R.string.label_nivel_alerta, alerta.getNivelAlerta()));

                        if (tvNombreLocal != null) tvNombreLocal.setText(alerta.getLocalNombre());
                        if (tvDireccionLocal != null) tvDireccionLocal.setText(alerta.getLocalDireccion());

                        if (tvProductoNombre != null) tvProductoNombre.setText(getString(R.string.label_producto_alerta, alerta.getNombreProducto()));

                        if (tvStockActual != null) {
                            tvStockActual.setText(String.valueOf(alerta.getStockActual()));
                        }
                        
                        if (tvStockMinimo != null) {
                            tvStockMinimo.setText(String.valueOf(alerta.getStockMinimo()));
                        }

                        if (tvResponsableNombre != null) tvResponsableNombre.setText(getString(R.string.label_responsable_alerta, alerta.getResponsableNombre()));
                        if (tvResponsableRol != null) tvResponsableRol.setText(getString(R.string.label_rol_alerta, alerta.getResponsableRol()));
                    }
                } else {
                    Toast.makeText(DetalleAlertaActivity.this, getString(R.string.error_cargar_alerta), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AlertaDetalle>> call, Throwable t) {
                Toast.makeText(DetalleAlertaActivity.this, getString(R.string.error_falla_red, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}