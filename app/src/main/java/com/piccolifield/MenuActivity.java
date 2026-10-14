package com.piccolifield;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.piccolifield.databinding.ActivityMenuBinding;
import com.piccolifield.ui.asistencia.AsistenciaHomeFragment;
import com.piccolifield.ui.perfil.PerfilFragment;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.fragmentContainer.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.curvedBottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Cargar por defecto la primera pantalla (Inicio)
        if (savedInstanceState == null) {
            cargarFragmento(new PerfilFragment());
        }

        binding.curvedBottomNav.setOnTabSelectedListener(position -> {
            switch (position) {
                case 0:
                    // Inicio
                    cargarFragmento(new PerfilFragment());
                    break;
                case 1:
                    // Módulo Asistencia
                    cargarFragmento(new AsistenciaHomeFragment());
                    break;
                case 2:
                    // Módulo en desarrollo
                    Toast.makeText(this, "Módulo en desarrollo", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    // Módulo en desarrollo
                    Toast.makeText(this, "Módulo en desarrollo", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void cargarFragmento(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
