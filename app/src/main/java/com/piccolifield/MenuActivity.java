package com.piccolifield;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.piccolifield.data.model.Usuario;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.ActivityMenuBinding;
import com.piccolifield.sharedpreferences.SplashStorage;
import com.piccolifield.ui.asistencia.AsistenciaHomeFragment;
import com.piccolifield.ui.asistencias.AsistenciasFragment;
import com.piccolifield.ui.despacho.PuntosDespachoFragment;
import com.piccolifield.ui.perfil.PerfilFragment;
import com.piccolifield.ui.supervisionPdv.SupervisionFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Asegurar la precarga del perfil si aún no hay rol en SharedPreferences
        if (SplashStorage.obtenerRol(this).isEmpty()) {
            precargarPerfil();
        }

        // Cargar por defecto la primera pantalla (Inicio)
        if (savedInstanceState == null) {
            cargarFragmento(new PerfilFragment());
        }

        binding.curvedBottomNav.setOnTabSelectedListener(position -> {
            switch (position) {
                case 0:
                    // Inicio / Home
                    cargarFragmento(new PerfilFragment());
                    break;
                case 1:
                    // Asistencias (Validación según Rol)
                    cargarFragmentoAsistenciaSegunRol();
                    break;
                case 2:
                    // Supervisión
                    cargarFragmento(new SupervisionFragment());
                    break;
                case 3:
                    // Puntos de Despacho
                    cargarFragmento(new PuntosDespachoFragment());
                    break;
            }
        });
    }

    private void precargarPerfil() {
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(this);
        }

        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerPerfil().enqueue(new Callback<ApiResponse<Usuario>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Usuario>> call, @NonNull Response<ApiResponse<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Usuario usuario = response.body().getData();
                    SplashStorage.guardarRol(MenuActivity.this, usuario.getRol());
                    SplashStorage.guardarUsuarioId(MenuActivity.this, usuario.getIdUsuario());
                    SplashStorage.guardarNombreUsuario(MenuActivity.this, usuario.getNombreCompleto());
                    SplashStorage.guardarCorreoUsuario(MenuActivity.this, usuario.getEmail());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Usuario>> call, @NonNull Throwable t) {
                // Se intentará nuevamente cuando el usuario navegue
            }
        });
    }

    private void cargarFragmentoAsistenciaSegunRol() {
        String rol = SplashStorage.obtenerRol(this);

        if (rol.isEmpty()) {
            if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
                RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(this);
            }

            ApiService apiService = RetrofitClient.createService();
            apiService.obtenerPerfil().enqueue(new Callback<ApiResponse<Usuario>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Usuario>> call, @NonNull Response<ApiResponse<Usuario>> response) {
                    if (isFinishing() || binding == null) return;

                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        Usuario usuario = response.body().getData();
                        String r = usuario.getRol();
                        SplashStorage.guardarRol(MenuActivity.this, r);
                        SplashStorage.guardarUsuarioId(MenuActivity.this, usuario.getIdUsuario());
                        SplashStorage.guardarNombreUsuario(MenuActivity.this, usuario.getNombreCompleto());
                        SplashStorage.guardarCorreoUsuario(MenuActivity.this, usuario.getEmail());

                        if ("ADMINISTRADOR".equalsIgnoreCase(r)) {
                            cargarFragmento(new AsistenciaHomeFragment());
                        } else {
                            cargarFragmento(new AsistenciasFragment());
                        }
                    } else {
                        cargarFragmento(new AsistenciasFragment());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Usuario>> call, @NonNull Throwable t) {
                    if (isFinishing() || binding == null) return;
                    cargarFragmento(new AsistenciasFragment());
                }
            });
        } else if ("ADMINISTRADOR".equalsIgnoreCase(rol)) {
            cargarFragmento(new AsistenciaHomeFragment());
        } else {
            cargarFragmento(new AsistenciasFragment());
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