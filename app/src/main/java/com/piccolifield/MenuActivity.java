package com.piccolifield;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.piccolifield.databinding.ActivityMenuBinding;
import com.piccolifield.sharedpreferences.SplashStorage;
import com.piccolifield.ui.asistencia.AsistenciaHomeFragment;
import com.piccolifield.ui.despacho.PuntosDespachoFragment;
import com.piccolifield.ui.impulsador.ImpulsadorHomeFragment;
import com.piccolifield.ui.perfil.PerfilFragment;
import com.piccolifield.ui.supervisionPdv.SupervisionFragment;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.fragmentContainer.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            // Elevar la barra de navegación curva por encima de los botones/gestos del celular
            if (binding.curvedBottomNav.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.curvedBottomNav.getLayoutParams();
                params.bottomMargin = systemBars.bottom + 12;
                binding.curvedBottomNav.setLayoutParams(params);
            }

            return insets;
        });

        // Cargar por defecto la primera pantalla (Inicio)
        if (savedInstanceState == null) {
            cargarFragmentoPorRol(0);
        }

        binding.curvedBottomNav.setOnTabSelectedListener(this::cargarFragmentoPorRol);
    }

    private void cargarFragmentoPorRol(int position) {
        String rol = SplashStorage.obtenerRol(this);

        if ("IMPULSADOR".equalsIgnoreCase(rol)) {
            // Flujo del Impulsador
            switch (position) {
                case 0:
                    // Inicio / Home (Ahora es Perfil para Impulsador)
                    cargarFragmento(new PerfilFragment());
                    break;
                case 1:
                    // Asistencias
                    cargarFragmento(new AsistenciaHomeFragment());
                    break;
                case 2:
                    // Supervision (Ahora es el ImpulsadorHomeFragment)
                    cargarFragmento(new ImpulsadorHomeFragment());
                    break;
                case 3:
                    // Despacho (Lo dejamos vacío o puedes poner otra cosa para Impulsador)
                    break;
            }
        } else {
            // Flujo del Supervisor / Despacho (original)
            switch (position) {
                case 0:
                    // Inicio / Home
                    cargarFragmento(new PerfilFragment());
                    break;
                case 1:
                    // Asistencias
                    cargarFragmento(new AsistenciaHomeFragment());
                    break;
                case 2:
                    // Supervision
                    cargarFragmento(new SupervisionFragment());
                    break;
                case 3:
                    // Despacho
                    cargarFragmento(new PuntosDespachoFragment());
                    break;
            }
        }
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