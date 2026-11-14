package com.piccolifield.ui.perfil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.piccolifield.MenuActivity;
import com.piccolifield.R;
import com.piccolifield.data.model.Usuario;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentPerfilBinding;
import com.piccolifield.sharedpreferences.SplashStorage;
import com.piccolifield.ui.CurvedBottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;

    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cargar el token almacenado si la memoria está vacía
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }

        actualizarFechaYHora();
        cargarDatosPerfil();
        
        configurarBotonesNavegacion();
    }
    
    private void configurarBotonesNavegacion() {
        binding.cardSupervision.setOnClickListener(v -> navegarDesdeElMenu(2));
        binding.cardAsistencias.setOnClickListener(v -> navegarDesdeElMenu(1));
        binding.cardPuntosDespacho.setOnClickListener(v -> navegarDesdeElMenu(3));
        binding.cardProspectos.setOnClickListener(v -> navegarDesdeElMenu(4));
    }

    private void navegarDesdeElMenu(int index) {
        if (getActivity() instanceof MenuActivity) {
            MenuActivity menuActivity = (MenuActivity) getActivity();
            CurvedBottomNavigationView bottomNav = menuActivity.findViewById(R.id.curvedBottomNav);
            if (bottomNav != null) {
                // Al decirle a la barra que seleccione esa pestaña, automáticamente cambiará
                // de fragmento y la bolita de abajo se animará al lugar correcto
                bottomNav.selectTab(index, true);
            }
        }
    }

    private void actualizarFechaYHora() {
        Date currentDate = new Date();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES"));
        String fecha = dateFormat.format(currentDate);
        fecha = fecha.substring(0, 1).toUpperCase() + fecha.substring(1);
        binding.tvFecha.setText(fecha);
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String hora = timeFormat.format(currentDate).toLowerCase();
        binding.tvHora.setText(hora);
    }

    private void cargarDatosPerfil() {
        // 1. Mostrar inmediatamente los datos guardados en caché para que no haya demora visual
        String nombreGuardado = SplashStorage.obtenerNombreUsuario(requireContext());
        String rolGuardado = SplashStorage.obtenerRol(requireContext());

        if (nombreGuardado != null && !nombreGuardado.isEmpty()) {
            binding.tvNombre.setText(nombreGuardado);
        }
        if (rolGuardado != null && !rolGuardado.isEmpty()) {
            binding.tvRol.setText("Rol: " + rolGuardado);
        }

        // 2. Consultar en segundo plano a la API por si hubo actualizaciones
        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerPerfil().enqueue(new Callback<ApiResponse<Usuario>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Usuario>> call, @NonNull Response<ApiResponse<Usuario>> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Usuario> apiResponse = response.body();

                    if (apiResponse.getData() != null) {
                        Usuario usuario = apiResponse.getData();

                        String nombres = usuario.getNombres() != null ? usuario.getNombres() : "";
                        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos() : "";
                        String nombreCompleto = (nombres + " " + apellidos).trim();

                        // Guardar datos en SplashStorage para uso en la app
                        SplashStorage.guardarRol(requireContext(), usuario.getRol());
                        SplashStorage.guardarUsuarioId(requireContext(), usuario.getIdUsuario());
                        SplashStorage.guardarNombreUsuario(requireContext(), nombreCompleto.isEmpty() ? usuario.getNombres() : nombreCompleto);
                        SplashStorage.guardarCorreoUsuario(requireContext(), usuario.getEmail());

                        binding.tvNombre.setText(nombreCompleto.isEmpty() ? "Usuario Registrado" : nombreCompleto);
                        binding.tvRol.setText("Rol: " + (usuario.getRol() != null ? usuario.getRol() : "CLIENTE"));
                    } else {
                        // Solo mostramos error en consola en vez de un toast visible que interrumpa si no hay datos pero la API contestó 200
                        // Ya que tenemos datos cacheados
                    }
                } else if (response.code() == 401) {
                    // Si el token expiró, no hacemos nada drástico por ahora
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Usuario>> call, @NonNull Throwable t) {
                // Falla silenciosa ya que usamos la caché
                if (!isAdded() || binding == null) return;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}