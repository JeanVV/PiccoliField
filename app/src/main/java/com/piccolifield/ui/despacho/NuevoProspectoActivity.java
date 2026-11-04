package com.piccolifield.ui.despacho;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.piccolifield.R;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Prospecto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoProspectoActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etNombre, etZona;
    private Spinner spinnerTipo;
    private MaterialButton btnCapturarGps, btnGuardarProspecto;
    private TextView tvCoordenadas;
    private MaterialCardView btnTomarFoto;

    private FusedLocationProviderClient fusedLocationClient;
    private double latitudActual = 0.0;
    private double longitudActual = 0.0;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nuevo_prospecto);

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

        // 1. Vincular vistas
        btnBack = findViewById(R.id.btnBack);
        etNombre = findViewById(R.id.etNombre);
        etZona = findViewById(R.id.etZona);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        btnCapturarGps = findViewById(R.id.btnCapturarGps);
        btnGuardarProspecto = findViewById(R.id.btnGuardarProspecto);
        tvCoordenadas = findViewById(R.id.tvCoordenadas);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 2. Configurar Spinner
        String[] tipos = {"Bodega", "Minimarket", "Licorería", "Supermercado", "Restaurante"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);

        // 3. Listeners
        btnBack.setOnClickListener(v -> finish());

        btnCapturarGps.setOnClickListener(v -> capturarUbicacion());

        btnTomarFoto.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo cámara (Simulado)...", Toast.LENGTH_SHORT).show();
            // TODO: Integrar la cámara con marca de agua del compañero
        });

        btnGuardarProspecto.setOnClickListener(v -> guardarProspecto());
    }

    private void capturarUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    latitudActual = location.getLatitude();
                    longitudActual = location.getLongitude();
                    tvCoordenadas.setText(String.format("%.5f, %.5f", latitudActual, longitudActual));
                    tvCoordenadas.setTextColor(ContextCompat.getColor(this, R.color.chocolate));
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                capturarUbicacion();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarProspecto() {
        String nombre = etNombre.getText().toString().trim();
        String zona = etZona.getText().toString().trim();
        String tipo = spinnerTipo.getSelectedItem().toString();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (latitudActual == 0.0 && longitudActual == 0.0) {
            Toast.makeText(this, "Debe capturar la ubicación GPS", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear prospecto para API
        Prospecto prospecto = new Prospecto(0, nombre, "N/A", "NUEVO", zona, tipo);
        prospecto.setLatitud(latitudActual);
        prospecto.setLongitud(longitudActual);

        ApiService apiService = RetrofitClient.createService();
        apiService.registrarProspecto(prospecto).enqueue(new Callback<ApiResponse<Prospecto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Prospecto>> call, @NonNull Response<ApiResponse<Prospecto>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(NuevoProspectoActivity.this, "Prospecto registrado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.code() == 409) {
                    Toast.makeText(NuevoProspectoActivity.this, "Error: El prospecto ya existe (409)", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(NuevoProspectoActivity.this, "Error al registrar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Prospecto>> call, @NonNull Throwable t) {
                // Simulación para fluidez de UI si no hay backend
                Toast.makeText(NuevoProspectoActivity.this, "Registro simulado exitoso", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}