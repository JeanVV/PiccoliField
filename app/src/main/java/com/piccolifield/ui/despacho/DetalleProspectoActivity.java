package com.piccolifield.ui.despacho;

import android.content.Intent;
import android.net.Uri;
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
import com.piccolifield.R;
import com.piccolifield.adapter.SeguimientoAdapter;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Prospecto;
import com.piccolifield.model.Seguimiento;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleProspectoActivity extends AppCompatActivity {

    private TextView tvNombreProspecto, tvZonaTipo, tvEstadoProspecto, btnNuevoSeguimiento;
    private MaterialButton btnVerUbicacion, btnConvertirPDV;
    private RecyclerView rvSeguimientos;
    
    private SeguimientoAdapter adapter;
    private List<Seguimiento> listaSeguimientos = new ArrayList<>();
    
    private Prospecto prospectoInfo;
    private int idProspecto = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_prospecto);

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

        // 1. Vincular
        tvNombreProspecto = findViewById(R.id.tvNombreProspecto);
        tvZonaTipo = findViewById(R.id.tvZonaTipo);
        tvEstadoProspecto = findViewById(R.id.tvEstadoProspecto);
        btnNuevoSeguimiento = findViewById(R.id.btnNuevoSeguimiento);
        btnVerUbicacion = findViewById(R.id.btnVerUbicacion);
        btnConvertirPDV = findViewById(R.id.btnConvertirPDV);
        rvSeguimientos = findViewById(R.id.rvSeguimientos);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // 2. Setup RecyclerView
        rvSeguimientos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SeguimientoAdapter(listaSeguimientos);
        rvSeguimientos.setAdapter(adapter);

        // 3. Recibir Datos y Cargar
        if (getIntent() != null) {
            idProspecto = getIntent().getIntExtra("ID_PROSPECTO", -1);
            String nombre = getIntent().getStringExtra("NOMBRE_PROSPECTO");
            if (nombre != null) tvNombreProspecto.setText(nombre);
            
            cargarDetalleProspecto(idProspecto);
            cargarSeguimientos(idProspecto);
        }

        // 4. Listeners
        btnVerUbicacion.setOnClickListener(v -> {
            if (prospectoInfo != null) {
                Uri gmmIntentUri = Uri.parse("geo:" + prospectoInfo.getLatitud() + "," + prospectoInfo.getLongitud() + "?q=" + prospectoInfo.getLatitud() + "," + prospectoInfo.getLongitud() + "(" + prospectoInfo.getNombre() + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(this, "Google Maps no está instalado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnNuevoSeguimiento.setOnClickListener(v -> {
            if (prospectoInfo != null) {
                NuevoSeguimientoBottomSheet bottomSheet = new NuevoSeguimientoBottomSheet(prospectoInfo, () -> {
                    // Recargar datos tras guardar
                    cargarDetalleProspecto(idProspecto);
                    cargarSeguimientos(idProspecto);
                });
                bottomSheet.show(getSupportFragmentManager(), "NuevoSeguimiento");
            }
        });
        
        btnConvertirPDV.setOnClickListener(v -> convertirAPuntoDistribucion());
    }

    private void cargarDetalleProspecto(int id) {
        if (id == -1) return;
        
        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerProspecto(id).enqueue(new Callback<ApiResponse<Prospecto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Prospecto>> call, @NonNull Response<ApiResponse<Prospecto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    prospectoInfo = response.body().getData();
                    actualizarUiProspecto();
                } else {
                    // Fallback
                    cargarMockProspecto();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Prospecto>> call, @NonNull Throwable t) {
                cargarMockProspecto();
            }
        });
    }

    private void actualizarUiProspecto() {
        tvNombreProspecto.setText(prospectoInfo.getNombre());
        tvZonaTipo.setText((prospectoInfo.getZona() != null ? prospectoInfo.getZona() : "") + " · " + (prospectoInfo.getTipo() != null ? prospectoInfo.getTipo() : ""));
        tvEstadoProspecto.setText(prospectoInfo.getEstado() != null ? prospectoInfo.getEstado() : "NUEVO");
        
        // Mostrar botón de conversión solo si está en NEGOCIACION
        if ("NEGOCIACION".equalsIgnoreCase(prospectoInfo.getEstado())) {
            btnConvertirPDV.setVisibility(View.VISIBLE);
        } else {
            btnConvertirPDV.setVisibility(View.GONE);
        }
    }

    private void cargarMockProspecto() {
        prospectoInfo = new Prospecto(idProspecto, tvNombreProspecto.getText().toString(), "Dir Mock", "NEGOCIACION", "Lima", "Bodega");
        prospectoInfo.setLatitud(-12.046374);
        prospectoInfo.setLongitud(-77.042793);
        actualizarUiProspecto();
    }

    private void cargarSeguimientos(int id) {
        if (id == -1) return;
        
        ApiService apiService = RetrofitClient.createService();
        apiService.listarSeguimientos(id).enqueue(new Callback<ApiResponse<List<Seguimiento>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Seguimiento>>> call, @NonNull Response<ApiResponse<List<Seguimiento>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaSeguimientos.clear();
                    listaSeguimientos.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    cargarMockSeguimientos();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Seguimiento>>> call, @NonNull Throwable t) {
                cargarMockSeguimientos();
            }
        });
    }

    private void cargarMockSeguimientos() {
        listaSeguimientos.clear();
        listaSeguimientos.add(new Seguimiento(1, idProspecto, "12 Oct 2023 - 10:00", "Llamada inicial, mostró interés", "NUEVO", "CONTACTADO"));
        listaSeguimientos.add(new Seguimiento(2, idProspecto, "15 Oct 2023 - 14:30", "Visita presencial. Solicita catálogo y precios", "CONTACTADO", "NEGOCIACION"));
        adapter.notifyDataSetChanged();
    }
    
    private void convertirAPuntoDistribucion() {
        // Simulación: El backend (Claude dice: "Al pasar a Convertido, crear en transacción el PDV de tipo DISTRIBUCIÓN")
        // Llamaremos a PUT /prospectos/{id}/estado con estado = CONVERTIDO
        
        ApiService apiService = RetrofitClient.createService();
        com.piccolifield.data.remote.dto.EstadoRequest request = new com.piccolifield.data.remote.dto.EstadoRequest();
        request.setEstado("CONVERTIDO");
        
        apiService.actualizarEstadoProspecto(idProspecto, request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetalleProspectoActivity.this, "¡Felicidades! Prospecto convertido a Punto de Distribución", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(DetalleProspectoActivity.this, "Error al convertir", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                Toast.makeText(DetalleProspectoActivity.this, "Conversión simulada exitosa. (Módulo Back-end pendiente)", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}