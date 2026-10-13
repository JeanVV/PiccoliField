package com.piccolifield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.piccolifield.databinding.ActivityAttendanceHomeBinding;

public class AttendanceHomeActivity extends AppCompatActivity {

    private ActivityAttendanceHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAttendanceHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Iconos oscuros en la barra de estado y de navegación sobre el fondo claro
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.attendanceHomeRootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupUI();
    }

    private void setupUI() {
        // Configurar RecyclerView para "Personal Reciente"
        if (binding.rvRecentPersonnel != null) {
            binding.rvRecentPersonnel.setLayoutManager(new LinearLayoutManager(this));
            // TODO: Conectar con Adapter y lista de objetos desde la BD / ViewModel
        }

        // Listener de Búsqueda y Filtro
        if (binding.btnFilter != null) {
            binding.btnFilter.setOnClickListener(v -> 
                Toast.makeText(this, "Filtrar resultados", Toast.LENGTH_SHORT).show()
            );
        }

        // Listener de Notificaciones
        if (binding.btnNotification != null) {
            binding.btnNotification.setOnClickListener(v -> 
                Toast.makeText(this, "Notificaciones de asistencias", Toast.LENGTH_SHORT).show()
            );
        }

        // Configuración de la Barra de Navegación Inferior (BottomNavigationView)
        if (binding.bottomNavigation != null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
            binding.bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_map) {
                    startActivity(new Intent(this, MapMonitoringActivity.class));
                    return true;
                } else if (itemId == R.id.nav_collaborators) {
                    startActivity(new Intent(this, CollaboratorsActivity.class));
                    return true;
                } else if (itemId == R.id.nav_reports) {
                    Toast.makeText(this, "Reportes de Asistencia", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
