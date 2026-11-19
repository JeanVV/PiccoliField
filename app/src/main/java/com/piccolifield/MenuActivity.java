package com.piccolifield;

import android.os.Bundle;
import android.view.ViewGroup;

import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.messaging.FirebaseMessaging;
import com.piccolifield.data.model.Usuario;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.dto.request.FcmTokenRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.ActivityMenuBinding;
import com.piccolifield.sharedpreferences.SplashStorage;
import com.piccolifield.ui.asistencia.AsistenciaHomeFragment;
import com.piccolifield.ui.asistencias.AsistenciasFragment;
import com.piccolifield.ui.despacho.PuntosDespachoFragment;
import com.piccolifield.ui.despacho.ProspectosFragment;
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
        // Desactivamos la apariencia clara en los botones de navegación porque ahora el fondo será marrón oscuro
        controller.setAppearanceLightNavigationBars(false);
        
        getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.cafe_profundo));

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.fragmentContainer.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            // Elevar la barra de navegación curva por encima de los botones/gestos del celular
            if (binding.curvedBottomNav.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.curvedBottomNav.getLayoutParams();
                // Subimos la barra para que la botonera del celular se vea abajo
                params.bottomMargin = systemBars.bottom;
                binding.curvedBottomNav.setLayoutParams(params);
            }

            return insets;
        });

        // Precargar perfil y directorio de fotos Base64 de usuarios siempre al iniciar
        precargarPerfil();
        com.piccolifield.util.UserPhotoLoader.sincronizarPerfilRaw(this, null, 0, null);
        com.piccolifield.util.UserPhotoLoader.sincronizarDirectorioUsuarios(this, null);

        // Registrar / Sincronizar Token FCM
        registrarTokenFcm();

        // Solicitar permiso de notificaciones en Android 13+ (API 33+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
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
                    if ("IMPULSADOR".equalsIgnoreCase(SplashStorage.obtenerRol(MenuActivity.this))) {
                        Toast.makeText(MenuActivity.this, "Los Puntos de Distribución no están disponibles para Impulsadores", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    cargarFragmento(new PuntosDespachoFragment());
                    break;
                case 4:
                    // Mi Perfil (Configuración/Logout)
                    cargarFragmento(new com.piccolifield.ui.perfil.MiPerfilFragment());
                    break;
                case 5:
                    // Prospectos
                    cargarFragmento(new ProspectosFragment());
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
                    if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty()) {
                        SplashStorage.guardarFotoUsuario(MenuActivity.this, usuario.getFotoPerfil());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Usuario>> call, @NonNull Throwable t) {
                // Se intentará nuevamente cuando el usuario navegue
            }
        });
    }

    private void registrarTokenFcm() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                android.util.Log.e("FCM_TOKEN_DEBUG", ">>> TOKEN FCM: " + token);

                String deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

                if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
                    RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(this);
                }

                ApiService apiService = RetrofitClient.createService();
                apiService.actualizarFcmToken(new FcmTokenRequest(token, deviceId)).enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful()) {
                            android.util.Log.d("MenuActivity", "Token FCM registrado exitosamente en el servidor");
                        } else {
                            android.util.Log.e("MenuActivity", "Fallo al registrar token FCM en servidor: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                        android.util.Log.e("MenuActivity", "Error de red al registrar token FCM", t);
                    }
                });
            } else {
                android.util.Log.e("FCM_TOKEN_DEBUG", "Error obteniendo token FCM: ", task.getException());
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

    public void navegarAPantallaPrincipal() {
        if (binding != null && binding.curvedBottomNav != null) {
            binding.curvedBottomNav.selectTab(0, true);
        } else {
            cargarFragmento(new PerfilFragment());
        }
    }

    public void abrirPuntosDistribucionEnProspectos() {
        if (binding != null && binding.curvedBottomNav != null) {
            binding.curvedBottomNav.selectTab(3, true); 
        } else {
            cargarFragmento(new PuntosDespachoFragment());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}