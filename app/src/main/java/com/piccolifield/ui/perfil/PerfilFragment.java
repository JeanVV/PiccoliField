package com.piccolifield.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.piccolifield.LoginActivity;
import com.piccolifield.data.model.Usuario;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentPerfilBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

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

        cargarDatosPerfil();

        binding.btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void cargarDatosPerfil() {
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
                        binding.tvEmail.setText(usuario.getEmail() != null ? usuario.getEmail() : "Sin correo");
                        binding.tvRol.setText("Rol: " + (usuario.getRol() != null ? usuario.getRol() : "CLIENTE"));
                        binding.tvEstado.setText("Estado: " + (usuario.getEstado() != null ? usuario.getEstado() : "ACTIVO"));
                    } else {
                        String msg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "No se encontraron datos";
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar perfil (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Usuario>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cerrarSesion() {
        SplashStorage.limpiarSesionActiva(requireContext());
        RetrofitClient.API_TOKEN = null;

        Toast.makeText(getContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}