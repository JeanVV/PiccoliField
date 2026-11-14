package com.piccolifield.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.piccolifield.LoginActivity;
import com.piccolifield.R;
import com.piccolifield.sharedpreferences.SplashStorage;
import com.piccolifield.data.remote.RetrofitClient;

public class MiPerfilFragment extends Fragment {

    private TextView tvNombrePerfil;
    private TextView tvRolPerfil;
    private TextView tvCorreoPerfil;
    private Button btnCerrarSesion;

    public MiPerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mi_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvNombrePerfil = view.findViewById(R.id.tvNombrePerfil);
        tvRolPerfil = view.findViewById(R.id.tvRolPerfil);
        tvCorreoPerfil = view.findViewById(R.id.tvCorreoPerfil);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);

        cargarDatos();

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void cargarDatos() {
        String nombre = SplashStorage.obtenerNombreUsuario(requireContext());
        String rol = SplashStorage.obtenerRol(requireContext());
        String correo = SplashStorage.obtenerCorreoUsuario(requireContext());

        if (nombre != null && !nombre.isEmpty()) {
            tvNombrePerfil.setText(nombre);
        } else {
            tvNombrePerfil.setText("Usuario Registrado");
        }

        if (rol != null && !rol.isEmpty()) {
            tvRolPerfil.setText("Rol: " + rol);
        } else {
            tvRolPerfil.setText("Rol: No definido");
        }

        if (correo != null && !correo.isEmpty()) {
            tvCorreoPerfil.setText("Correo: " + correo);
        } else {
            tvCorreoPerfil.setText("Correo: No disponible");
        }
    }

    private void cerrarSesion() {
        // Limpiar SharedPreferences
        SplashStorage.limpiarSesionActiva(requireContext());
        RetrofitClient.API_TOKEN = null;

        Toast.makeText(getContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        // Redirigir al Login
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}