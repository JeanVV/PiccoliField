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
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentColaboradoresBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ColaboradoresFragment extends Fragment {

    private static final String ARG_FILTRO_INICIAL = "filtro_inicial";
    private FragmentColaboradoresBinding binding;
    private ApiService apiService;
    private ColaboradorAdapter adapter;
    private String estadoFiltroActual = "TODOS";

    public ColaboradoresFragment() {
        // Required empty public constructor
    }

    public static ColaboradoresFragment newInstance(String filtroInicial) {
        ColaboradoresFragment fragment = new ColaboradoresFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTRO_INICIAL, filtroInicial);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_FILTRO_INICIAL)) {
            String filtroArg = getArguments().getString(ARG_FILTRO_INICIAL);
            if (filtroArg != null && !filtroArg.isEmpty()) {
                estadoFiltroActual = filtroArg;
            }
        }
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
        // Botón Retroceder (Flecha arriba izquierda)
        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else if (getActivity() instanceof com.piccolifield.MenuActivity) {
                    ((com.piccolifield.MenuActivity) getActivity()).navegarAPantallaPrincipal();
                }
            });
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

        // Swipe Refresh Layout
        if (binding.swipeRefreshLayout != null) {
            binding.swipeRefreshLayout.setColorSchemeResources(R.color.cafe_profundo, R.color.caramelo);
            binding.swipeRefreshLayout.setOnRefreshListener(this::cargarColaboradores);
        }
    }

    private void mostrarPopupMenuFiltros(View view) {
        androidx.appcompat.view.ContextThemeWrapper contextWrapper = new androidx.appcompat.view.ContextThemeWrapper(requireContext(), R.style.Theme_PiccoliField_PopupMenu);
        PopupMenu popup = new PopupMenu(contextWrapper, view);
        popup.getMenuInflater().inflate(R.menu.menu_filtros_asistencia, popup.getMenu());

        try {
            java.lang.reflect.Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            if (menuPopupHelper != null) {
                Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                java.lang.reflect.Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                setForceIcons.invoke(menuPopupHelper, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.filter_todos) {
                estadoFiltroActual = "TODOS";
            } else if (itemId == R.id.filter_a_tiempo) {
                estadoFiltroActual = "A_TIEMPO";
            } else if (itemId == R.id.filter_tardanza) {
                estadoFiltroActual = "TARDANZA";
            } else if (itemId == R.id.filter_falta) {
                estadoFiltroActual = "FALTA";
            }

            if (adapter != null && binding.etSearchCollaborator != null) {
                adapter.filtrar(binding.etSearchCollaborator.getText().toString(), estadoFiltroActual);
            }
            return true;
        });

        popup.show();
    }

    private void cargarColaboradores() {
        if (apiService == null || binding == null) return;

        apiService.obtenerColaboradoresConAsistencia().enqueue(new Callback<ApiResponse<List<RegistroAsistencia>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<RegistroAsistencia>>> call, @NonNull Response<ApiResponse<List<RegistroAsistencia>>> response) {
                if (!isAdded() || binding == null) return;
                if (binding.swipeRefreshLayout != null) binding.swipeRefreshLayout.setRefreshing(false);

                List<RegistroAsistencia> listaRemote = (response.isSuccessful() && response.body() != null && response.body().getData() != null)
                        ? response.body().getData()
                        : new java.util.ArrayList<>();

                if (listaRemote.isEmpty()) {
                    binding.layoutEmptyCollaboratorsState.setVisibility(View.VISIBLE);
                    binding.rvCollaborators.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyCollaboratorsState.setVisibility(View.GONE);
                    binding.rvCollaborators.setVisibility(View.VISIBLE);
                    binding.rvCollaborators.setLayoutManager(new LinearLayoutManager(getContext()));

                    adapter = new ColaboradorAdapter(listaRemote);
                    adapter.filtrar("", estadoFiltroActual);
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
                if (binding.swipeRefreshLayout != null) binding.swipeRefreshLayout.setRefreshing(false);
                Log.e("ColaboradoresFragment", "Error red: " + t.getMessage());
                binding.layoutEmptyCollaboratorsState.setVisibility(View.VISIBLE);
                binding.rvCollaborators.setVisibility(View.GONE);
            }
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
