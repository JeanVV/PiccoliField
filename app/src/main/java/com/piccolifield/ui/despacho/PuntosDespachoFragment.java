package com.piccolifield.ui.despacho;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.piccolifield.DetallePuntoDistribucionActivity;
import com.piccolifield.R;
import com.piccolifield.adapter.PuntoDistribucionAdapter;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentPuntosDespachoBinding;
import com.piccolifield.model.PuntoDistribucion;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PuntosDespachoFragment extends Fragment {

    private FragmentPuntosDespachoBinding binding;
    private PuntoDistribucionAdapter adapter;
    private final List<PuntoDistribucion> listaPuntosOriginal = new ArrayList<>();
    private final List<PuntoDistribucion> listaPuntosFiltrada = new ArrayList<>();

    public PuntosDespachoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPuntosDespachoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        cargarDatosDesdeApi();
        setupToggleViews();
        setupListeners();
    }

    private void setupRecyclerView() {
        binding.rvListaPuntos.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PuntoDistribucionAdapter(listaPuntosFiltrada, requireActivity(), pdv -> {
            if (pdv == null) return;
            Intent intent = new Intent(requireActivity(), DetallePuntoDistribucionActivity.class);
            intent.putExtra("ID_PDV", pdv.getId());
            intent.putExtra("NOMBRE_PDV", pdv.getNombre());
            intent.putExtra("DIRECCION", pdv.getDireccion());
            intent.putExtra("DISTANCIA", pdv.getDistancia());
            intent.putExtra("NUMERO_PEDIDO", pdv.getNumeroPedido());
            intent.putExtra("ESTADO_DESPACHO", pdv.getEstadoDespacho());
            intent.putExtra("BADGE_NUMERO", pdv.getBadgeNumero());
            startActivity(intent);
        });
        binding.rvListaPuntos.setAdapter(adapter);
    }

    private void cargarDatosDesdeApi() {
        ApiService apiService = RetrofitClient.createService();
        apiService.listarPuntosVenta(null, null, null, null).enqueue(new Callback<ApiResponse<List<PuntoDistribucion>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PuntoDistribucion>>> call, @NonNull Response<ApiResponse<List<PuntoDistribucion>>> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    listaPuntosOriginal.clear();
                    listaPuntosOriginal.addAll(response.body().getData());
                    actualizarUI();
                } else {
                    cargarDatosSimulados();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PuntoDistribucion>>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                cargarDatosSimulados();
            }
        });
    }

    private void cargarDatosSimulados() {
        listaPuntosOriginal.clear();
        listaPuntosOriginal.add(new PuntoDistribucion(1, "Licorería El Buen Sabor", "Av. Principal 123, Lima", "0245", "En preparación", "12"));
        listaPuntosOriginal.add(new PuntoDistribucion(2, "Supermercado Plaza Vea", "Av. Los Olivos 456, Lima", "0244", "En camino", "8"));
        listaPuntosOriginal.add(new PuntoDistribucion(3, "Bar La Cava", "Calle Las Flores 789, Lima", "0243", "Pendiente", "5"));
        listaPuntosOriginal.add(new PuntoDistribucion(4, "Tienda Don José", "Av. Universitaria 321, Lima", "0242", "En preparación", "14"));
        listaPuntosOriginal.add(new PuntoDistribucion(5, "Wong", "Av. Javier Prado 654, Lima", "0241", "En camino", "7"));

        actualizarUI();
    }

    private void actualizarUI() {
        listaPuntosFiltrada.clear();
        listaPuntosFiltrada.addAll(listaPuntosOriginal);

        binding.tvTotalPuntos.setText(String.valueOf(listaPuntosOriginal.size()));
        binding.tvActivosCount.setText(String.valueOf(contarActivos(listaPuntosOriginal)));
        binding.tvCiudadesCount.setText("12");

        adapter.notifyDataSetChanged();
    }

    private int contarActivos(List<PuntoDistribucion> lista) {
        int count = 0;
        for (PuntoDistribucion p : lista) {
            if ("ACTIVO".equalsIgnoreCase(p.getEstado())) {
                count++;
            }
        }
        return count;
    }

    private void setupToggleViews() {
        int colorCafe = ContextCompat.getColor(requireContext(), R.color.cafe_profundo);
        int colorBlanco = ContextCompat.getColor(requireContext(), R.color.white);
        int colorTransparente = ContextCompat.getColor(requireContext(), android.R.color.transparent);

        binding.btnToggleMapa.setOnClickListener(v -> {
            binding.layoutMapView.setVisibility(View.VISIBLE);
            binding.rvListaPuntos.setVisibility(View.GONE);
            binding.cardHero.setVisibility(View.VISIBLE);
            binding.layoutStats.setVisibility(View.VISIBLE);

            binding.btnToggleMapa.setBackgroundTintList(ColorStateList.valueOf(colorCafe));
            binding.btnToggleMapa.setTextColor(colorBlanco);

            binding.btnToggleLista.setBackgroundTintList(ColorStateList.valueOf(colorTransparente));
            binding.btnToggleLista.setTextColor(colorCafe);
        });

        binding.btnToggleLista.setOnClickListener(v -> {
            binding.layoutMapView.setVisibility(View.GONE);
            binding.rvListaPuntos.setVisibility(View.VISIBLE);
            binding.cardHero.setVisibility(View.GONE);
            binding.layoutStats.setVisibility(View.GONE);

            binding.btnToggleLista.setBackgroundTintList(ColorStateList.valueOf(colorCafe));
            binding.btnToggleLista.setTextColor(colorBlanco);

            binding.btnToggleMapa.setBackgroundTintList(ColorStateList.valueOf(colorTransparente));
            binding.btnToggleMapa.setTextColor(colorCafe);
        });
    }

    private void setupListeners() {
        binding.cardTooltipStore.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), DetallePuntoDistribucionActivity.class);
            intent.putExtra("ID_PDV", 1);
            intent.putExtra("NOMBRE_PDV", "Licorería El Buen Sabor");
            startActivity(intent);
        });

        binding.btnMenu.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.fabMyLocation.setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Centrando en ubicación actual", Toast.LENGTH_SHORT).show()
        );

        binding.btnFiltrosContainer.setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Filtros de búsqueda", Toast.LENGTH_SHORT).show()
        );

        binding.etBuscarPuntos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filtrar(String query) {
        listaPuntosFiltrada.clear();
        if (query.trim().isEmpty()) {
            listaPuntosFiltrada.addAll(listaPuntosOriginal);
        } else {
            String q = query.toLowerCase();
            for (PuntoDistribucion p : listaPuntosOriginal) {
                if ((p.getNombre() != null && p.getNombre().toLowerCase().contains(q)) ||
                    (p.getDireccion() != null && p.getDireccion().toLowerCase().contains(q)) ||
                    (p.getNumeroPedido() != null && p.getNumeroPedido().toLowerCase().contains(q)) ||
                    (p.getEstadoDespacho() != null && p.getEstadoDespacho().toLowerCase().contains(q))) {
                    listaPuntosFiltrada.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}