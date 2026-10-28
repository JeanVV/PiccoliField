package com.piccolifield.ui.asistencia;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.piccolifield.R;
import com.piccolifield.adapter.ColaboradorAdapter;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentColaboradoresBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ColaboradoresFragment extends Fragment {

    private FragmentColaboradoresBinding binding;
    private ApiService apiService;
    private ColaboradorAdapter adapter;
    private String estadoFiltroActual = "TODOS";

    public ColaboradoresFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentColaboradoresBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Asegurar token de autenticación
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }

        apiService = RetrofitClient.createService();

        setupListeners();
        cargarColaboradores();
    }

    private void setupListeners() {
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

    private void cargarColaboradores() {
        if (apiService == null) return;

        apiService.obtenerColaboradoresConAsistencia().enqueue(new Callback<ApiResponse<List<RegistroAsistencia>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<RegistroAsistencia>>> call, @NonNull Response<ApiResponse<List<RegistroAsistencia>>> response) {
                if (!isAdded() || binding == null) return;

                List<RegistroAsistencia> listaRemote = null;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaRemote = response.body().getData();
                }

                List<RegistroAsistencia> listaCombinada = combinarConLocales(listaRemote);

                if (listaCombinada.isEmpty()) {
                    binding.layoutEmptyCollaboratorsState.setVisibility(View.VISIBLE);
                    binding.rvCollaborators.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyCollaboratorsState.setVisibility(View.GONE);
                    binding.rvCollaborators.setVisibility(View.VISIBLE);
                    binding.rvCollaborators.setLayoutManager(new LinearLayoutManager(getContext()));

                    adapter = new ColaboradorAdapter(listaCombinada);
                    adapter.setOnItemClickListener(item -> {
                        DetalleAsistenciaFragment fragment = DetalleAsistenciaFragment.newInstance(item);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                    binding.rvCollaborators.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<RegistroAsistencia>>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                Log.e("ColaboradoresFragment", "Error red: " + t.getMessage());

                List<RegistroAsistencia> listaLocales = combinarConLocales(null);
                if (!listaLocales.isEmpty()) {
                    binding.layoutEmptyCollaboratorsState.setVisibility(View.GONE);
                    binding.rvCollaborators.setVisibility(View.VISIBLE);
                    binding.rvCollaborators.setLayoutManager(new LinearLayoutManager(getContext()));

                    adapter = new ColaboradorAdapter(listaLocales);
                    adapter.setOnItemClickListener(item -> {
                        DetalleAsistenciaFragment fragment = DetalleAsistenciaFragment.newInstance(item);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                    binding.rvCollaborators.setAdapter(adapter);
                } else {
                    binding.layoutEmptyCollaboratorsState.setVisibility(View.VISIBLE);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
