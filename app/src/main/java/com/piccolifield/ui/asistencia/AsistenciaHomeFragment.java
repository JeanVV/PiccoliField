package com.piccolifield.ui.asistencia;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.piccolifield.R;
import com.piccolifield.adapter.RecentPersonnelAdapter;
import com.piccolifield.data.model.AsistenciaResumen;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentAsistenciaHomeBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsistenciaHomeFragment extends Fragment {

    private FragmentAsistenciaHomeBinding binding;
    private ApiService apiService;

    public AsistenciaHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAsistenciaHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Asegurar que el token de autenticación esté cargado en RetrofitClient
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }

        apiService = RetrofitClient.createService();

        // 2. Configurar eventos de navegación interna
        setupListeners();

        // 3. Cargar datos del resumen y personal reciente
        cargarDatos();
    }

    private void setupListeners() {
        // Botón/Card Ver Mapa
        if (binding.cardGoToMap != null) {
            binding.cardGoToMap.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new MapaFragment())
                    .addToBackStack(null)
                    .commit()
            );
        }

        // Botón/Card Colaboradores
        if (binding.cardGoToCollaborators != null) {
            binding.cardGoToCollaborators.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ColaboradoresFragment())
                    .addToBackStack(null)
                    .commit()
            );
        }

        // Botón Notificación
        if (binding.btnNotification != null) {
            binding.btnNotification.setOnClickListener(v ->
                Toast.makeText(getContext(), "Notificaciones de asistencias", Toast.LENGTH_SHORT).show()
            );
        }

        // Botón Filtro
        if (binding.btnFilter != null) {
            binding.btnFilter.setOnClickListener(v ->
                Toast.makeText(getContext(), "Filtrar resultados", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void cargarDatos() {
        if (apiService == null) return;

        // 1. Cargar Resumen del Día
        apiService.obtenerResumenAsistencias().enqueue(new Callback<ApiResponse<AsistenciaResumen>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AsistenciaResumen>> call, @NonNull Response<ApiResponse<AsistenciaResumen>> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    AsistenciaResumen resumen = response.body().getData();
                    binding.pbAttendancePercentage.setProgress(resumen.getPorcentaje());
                    binding.tvAttendancePercentage.setText(resumen.getPorcentaje() + "%");
                    binding.tvPresentesFraction.setText(resumen.getPresentes() + "/" + resumen.getTotal());
                    binding.tvOnTimeCount.setText("(" + resumen.getATiempo() + ")");
                    binding.tvLateCount.setText("(" + resumen.getTardanzas() + ")");
                } else {
                    Log.e("AsistenciaHome", "Error en respuesta resumen: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AsistenciaResumen>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                Log.e("AsistenciaHome", "Fallo red resumen: " + t.getMessage());
            }
        });

        // 2. Cargar Personal Reciente
        apiService.obtenerPersonalReciente().enqueue(new Callback<ApiResponse<List<RegistroAsistencia>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<RegistroAsistencia>>> call, @NonNull Response<ApiResponse<List<RegistroAsistencia>>> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<RegistroAsistencia> lista = response.body().getData();
                    if (lista.isEmpty()) {
                        binding.layoutEmptyRecentState.setVisibility(View.VISIBLE);
                        binding.rvRecentPersonnel.setVisibility(View.GONE);
                    } else {
                        binding.layoutEmptyRecentState.setVisibility(View.GONE);
                        binding.rvRecentPersonnel.setVisibility(View.VISIBLE);
                        binding.rvRecentPersonnel.setLayoutManager(new LinearLayoutManager(getContext()));
                        binding.rvRecentPersonnel.setAdapter(new RecentPersonnelAdapter(lista));
                    }
                } else {
                    Log.e("AsistenciaHome", "Error en respuesta personal: " + response.code());
                    binding.layoutEmptyRecentState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<RegistroAsistencia>>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                Log.e("AsistenciaHome", "Fallo red personal: " + t.getMessage());
                binding.layoutEmptyRecentState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
