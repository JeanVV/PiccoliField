package com.piccolifield.ui.asistencia;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
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
import com.piccolifield.databinding.FragmentAsistenciaInicioBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsistenciaHomeFragment extends Fragment {

    private FragmentAsistenciaInicioBinding binding;
    private ApiService apiService;
    private RecentPersonnelAdapter adapter;
    private String estadoFiltroActual = "TODOS";

    public AsistenciaHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAsistenciaInicioBinding.inflate(inflater, container, false);
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

        // 2. Configurar eventos de navegación interna y búsqueda
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

        // Búsqueda en tiempo real
        if (binding.etSearchCollaborator != null) {
            binding.etSearchCollaborator.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filtrar(s.toString(), estadoFiltroActual);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Botón Filtros
        View btnFiltroTarget = binding.btnFiltrosContainer != null ? binding.btnFiltrosContainer : binding.btnFilter;
        if (btnFiltroTarget != null) {
            btnFiltroTarget.setOnClickListener(this::mostrarPopupMenuFiltros);
        }
    }

    private void mostrarPopupMenuFiltros(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenu().add(0, 1, 0, "Todos los estados");
        popup.getMenu().add(0, 2, 1, "A tiempo");
        popup.getMenu().add(0, 3, 2, "Tardanza");
        popup.getMenu().add(0, 4, 3, "Falta");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    estadoFiltroActual = "TODOS";
                    break;
                case 2:
                    estadoFiltroActual = "A_TIEMPO";
                    break;
                case 3:
                    estadoFiltroActual = "TARDANZA";
                    break;
                case 4:
                    estadoFiltroActual = "FALTA";
                    break;
            }

            if (adapter != null && binding.etSearchCollaborator != null) {
                adapter.filtrar(binding.etSearchCollaborator.getText().toString(), estadoFiltroActual);
            }
            return true;
        });

        popup.show();
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

                List<RegistroAsistencia> listaRemote = null;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaRemote = response.body().getData();
                }

                List<RegistroAsistencia> listaCombinada = combinarConLocales(listaRemote);
                verificarJustificacionesPendientes(listaCombinada);

                if (listaCombinada.isEmpty()) {
                    binding.layoutEmptyRecentState.setVisibility(View.VISIBLE);
                    binding.rvRecentPersonnel.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyRecentState.setVisibility(View.GONE);
                    binding.rvRecentPersonnel.setVisibility(View.VISIBLE);
                    binding.rvRecentPersonnel.setLayoutManager(new LinearLayoutManager(getContext()));

                    adapter = new RecentPersonnelAdapter(listaCombinada);
                    adapter.setOnItemClickListener(item -> {
                        DetalleAsistenciaFragment fragment = DetalleAsistenciaFragment.newInstance(item);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                    binding.rvRecentPersonnel.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<RegistroAsistencia>>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                Log.e("AsistenciaHome", "Fallo red personal: " + t.getMessage());

                List<RegistroAsistencia> listaLocales = combinarConLocales(null);
                verificarJustificacionesPendientes(listaLocales);

                if (!listaLocales.isEmpty()) {
                    binding.layoutEmptyRecentState.setVisibility(View.GONE);
                    binding.rvRecentPersonnel.setVisibility(View.VISIBLE);
                    binding.rvRecentPersonnel.setLayoutManager(new LinearLayoutManager(getContext()));

                    adapter = new RecentPersonnelAdapter(listaLocales);
                    adapter.setOnItemClickListener(item -> {
                        DetalleAsistenciaFragment fragment = DetalleAsistenciaFragment.newInstance(item);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                    binding.rvRecentPersonnel.setAdapter(adapter);
                } else {
                    binding.layoutEmptyRecentState.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private java.util.List<RegistroAsistencia> combinarConLocales(java.util.List<RegistroAsistencia> listaRemote) {
        java.util.List<RegistroAsistencia> combinada = new java.util.ArrayList<>();
        if (listaRemote != null) {
            combinada.addAll(listaRemote);
        }

        java.util.List<RegistroAsistencia> locales = SplashStorage.obtenerRegistrosAsistenciaLocales(getContext());
        if (locales != null && !locales.isEmpty()) {
            for (RegistroAsistencia loc : locales) {
                boolean existe = false;
                for (RegistroAsistencia rem : combinada) {
                    if (rem.getIdUsuario() == loc.getIdUsuario()) {
                        if (loc.getJustificacion() != null && !loc.getJustificacion().isEmpty()) {
                            rem.setJustificacion(loc.getJustificacion());
                            rem.setMotivoJustificacion(loc.getMotivoJustificacion());
                        }
                        existe = true;
                        break;
                    }
                }
                if (!existe) {
                    combinada.add(0, loc);
                }
            }
        }
        return combinada;
    }

    private void verificarJustificacionesPendientes(List<RegistroAsistencia> lista) {
        if (binding == null) return;
        int countPendientes = 0;

        if (lista != null) {
            for (RegistroAsistencia item : lista) {
                if (item.getJustificacion() != null && !item.getJustificacion().isEmpty()) {
                    countPendientes++;
                }
            }
        }

        if (countPendientes > 0) {
            binding.cardPendingJustificationsBanner.setVisibility(View.VISIBLE);
            binding.tvPendingJustificationsCount.setText("Tienes " + countPendientes + " colaborador(es) con justificación de falta por revisar.");
            binding.btnFiltrarPendientes.setOnClickListener(v -> {
                if (adapter != null) {
                    adapter.filtrar("", "FALTA");
                }
            });
        } else {
            binding.cardPendingJustificationsBanner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
