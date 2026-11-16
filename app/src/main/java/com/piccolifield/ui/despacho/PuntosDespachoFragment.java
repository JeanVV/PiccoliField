package com.piccolifield.ui.despacho;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.piccolifield.DetallePuntoDistribucionActivity;
import com.piccolifield.R;
import com.piccolifield.adapter.PuntoDistribucionAdapter;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentPuntosDespachoBinding;
import com.piccolifield.model.PuntoDistribucion;
import com.piccolifield.sharedpreferences.PuntosDistribucionStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PuntosDespachoFragment extends Fragment implements OnMapReadyCallback {

    private FragmentPuntosDespachoBinding binding;
    private PuntoDistribucionAdapter adapter;
    private final List<PuntoDistribucion> listaPuntosOriginal = new ArrayList<>();
    private final List<PuntoDistribucion> listaPuntosFiltrada = new ArrayList<>();

    private GoogleMap googleMap;
    private String filtroCriterioActual = "TODOS";
    private String filtroEstadoActual = "TODOS";

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

        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = com.piccolifield.sharedpreferences.SplashStorage.obtenerToken(requireContext());
        }

        setupRecyclerView();
        setupSpinnersFiltros();
        cargarDatosDesdeApi();
        setupToggleViews();
        setupListeners();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && !listaPuntosOriginal.isEmpty()) {
            sincronizarPuntosLocales();
            aplicarFiltros();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        actualizarPinesMapa();
    }

    private void actualizarPinesMapa() {
        if (googleMap == null) return;
        googleMap.clear();

        if (listaPuntosOriginal.isEmpty()) return;

        for (PuntoDistribucion p : listaPuntosOriginal) {
            LatLng posicion = new LatLng(p.getLatitud(), p.getLongitud());
            googleMap.addMarker(new MarkerOptions()
                    .position(posicion)
                    .title(p.getNombre() + " (" + p.getCodigo() + ")")
                    .snippet(p.getDireccion()));
        }

        PuntoDistribucion primero = listaPuntosOriginal.get(0);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(primero.getLatitud(), primero.getLongitud()), 12f));
    }

    private void setupRecyclerView() {
        binding.rvListaPuntos.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PuntoDistribucionAdapter(listaPuntosFiltrada, requireActivity(), pdv -> {
            if (pdv == null) return;
            Intent intent = new Intent(requireActivity(), DetallePuntoDistribucionActivity.class);
            intent.putExtra("ID_PDV", pdv.getId());
            intent.putExtra("NOMBRE_PDV", pdv.getNombre());
            intent.putExtra("CODIGO_PDV", pdv.getCodigo());
            intent.putExtra("TIPO_NEGOCIO", pdv.getCriterioNegocio());
            intent.putExtra("DIRECCION", pdv.getDireccion());
            intent.putExtra("DISTANCIA", pdv.getDistancia());
            intent.putExtra("ESTADO_PDV", pdv.getEstado());
            intent.putExtra("BADGE_NUMERO", pdv.getBadgeNumero());
            startActivity(intent);
        });
        binding.rvListaPuntos.setAdapter(adapter);
    }

    private void setupSpinnersFiltros() {
        String[] opcionesCriterio = {"Criterio: Todos", "Minorista", "Mayorista"};
        ArrayAdapter<String> adapterCriterio = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, opcionesCriterio);
        adapterCriterio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spFiltroCriterio.setAdapter(adapterCriterio);

        binding.spFiltroCriterio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) filtroCriterioActual = "Minorista";
                else if (position == 2) filtroCriterioActual = "Mayorista";
                else filtroCriterioActual = "TODOS";
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        String[] opcionesEstado = {"Estado: Todos", "ACTIVO", "INACTIVO"};
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, opcionesEstado);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spFiltroEstado.setAdapter(adapterEstado);

        binding.spFiltroEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) filtroEstadoActual = "ACTIVO";
                else if (position == 2) filtroEstadoActual = "INACTIVO";
                else filtroEstadoActual = "TODOS";
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void cargarDatosDesdeApi() {
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = com.piccolifield.sharedpreferences.SplashStorage.obtenerToken(requireContext());
        }
        ApiService apiService = RetrofitClient.createService();
        // Solo obtener puntos del tipo DISTRIBUCION para que no salgan los VENTA
        apiService.listarPuntosVenta(null, "DISTRIBUCION", null, null).enqueue(new Callback<ApiResponse<List<PuntoDistribucion>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PuntoDistribucion>>> call, @NonNull Response<ApiResponse<List<PuntoDistribucion>>> response) {
                if (!isAdded() || binding == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    listaPuntosOriginal.clear();
                    listaPuntosOriginal.addAll(response.body().getData());
                    sincronizarPuntosLocales();
                    actualizarUI();
                } else {
                    cargarPuntosRespaldo();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PuntoDistribucion>>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                cargarPuntosRespaldo();
                sincronizarPuntosLocales();
                actualizarUI();
            }
        });
    }

    private void cargarPuntosRespaldo() {
        listaPuntosOriginal.add(new PuntoDistribucion(1, "PDV-001", "Licorería El Buen Sabor", "Minorista", "Av. Principal 123, Miraflores", "0.8 km", "ACTIVO", -12.1191, -77.0349, 1, 1, 0, "PDV-001", "", "1"));
        listaPuntosOriginal.add(new PuntoDistribucion(2, "PDV-002", "Distribuidora San Martín", "Mayorista", "Av. Arequipa 1450, San Isidro", "1.4 km", "ACTIVO", -12.0931, -77.0365, 2, 1, 0, "PDV-002", "", "2"));
        listaPuntosOriginal.add(new PuntoDistribucion(3, "PDV-003", "Minimarket Las Flores", "Minorista", "Calle Las Flores 789, Surco", "2.1 km", "ACTIVO", -12.1350, -76.9950, 3, 1, 0, "PDV-003", "", "3"));
        listaPuntosOriginal.add(new PuntoDistribucion(4, "PDV-004", "Comercial Mayorista Central", "Mayorista", "Av. Universitaria 321, Lima", "3.0 km", "ACTIVO", -12.0550, -77.0750, 4, 1, 0, "PDV-004", "", "4"));
    }

    private void sincronizarPuntosLocales() {
        if (!isAdded()) return;
        List<PuntoDistribucion> locales = PuntosDistribucionStorage.obtenerPuntosLocales(requireContext());
        if (locales.isEmpty()) return;

        Set<Integer> idsExistentes = new HashSet<>();
        for (PuntoDistribucion p : listaPuntosOriginal) {
            idsExistentes.add(p.getId());
        }

        for (int i = locales.size() - 1; i >= 0; i--) {
            PuntoDistribucion local = locales.get(i);
            if (!idsExistentes.contains(local.getId())) {
                listaPuntosOriginal.add(0, local);
                idsExistentes.add(local.getId());
            } else {
                for (PuntoDistribucion existente : listaPuntosOriginal) {
                    if (existente.getId() == local.getId()) {
                        existente.setPuntosVenta(local.getNombre());
                        existente.setCodigo(local.getCodigo());
                        existente.setTipo(local.getCriterioNegocio());
                        existente.setDireccion(local.getDireccion());
                        break;
                    }
                }
            }
        }
    }

    private void actualizarUI() {
        if (binding == null) return;
        aplicarFiltros();

        binding.tvTotalPuntos.setText(String.valueOf(listaPuntosOriginal.size()));
        binding.tvActivosCount.setText(String.valueOf(contarActivos(listaPuntosOriginal)));
        binding.tvCiudadesCount.setText("1");

        actualizarPinesMapa();
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
        binding.btnMenu.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.fabMyLocation.setOnClickListener(v -> {
            if (googleMap != null && !listaPuntosOriginal.isEmpty()) {
                PuntoDistribucion primero = listaPuntosOriginal.get(0);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(primero.getLatitud(), primero.getLongitud()), 12f));
            } else {
                Toast.makeText(requireContext(), "Centrando en ubicación actual", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnFiltrosContainer.setOnClickListener(v -> {
            if (binding.layoutFiltrosCombos.getVisibility() == View.VISIBLE) {
                binding.layoutFiltrosCombos.setVisibility(View.GONE);
            } else {
                binding.layoutFiltrosCombos.setVisibility(View.VISIBLE);
            }
        });

        binding.fabAgregarPunto.setOnClickListener(v -> abrirFormularioNuevoPunto());

        binding.etBuscarPuntos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void abrirFormularioNuevoPunto() {
        int siguienteNum = listaPuntosOriginal.size() + 1;
        NuevoPuntoDistribucionFragment fragment = new NuevoPuntoDistribucionFragment(siguienteNum, () -> {
            if (isAdded()) {
                cargarDatosDesdeApi();
            }
        });
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack("NuevoPuntoDistribucion")
                .commit();
    }

    private void aplicarFiltros() {
        if (binding == null) return;
        String query = binding.etBuscarPuntos.getText() != null
                ? binding.etBuscarPuntos.getText().toString().trim().toLowerCase()
                : "";

        listaPuntosFiltrada.clear();
        for (PuntoDistribucion p : listaPuntosOriginal) {
            boolean coincideTexto = query.isEmpty()
                    || (p.getNombre() != null && p.getNombre().toLowerCase().contains(query))
                    || (p.getDireccion() != null && p.getDireccion().toLowerCase().contains(query))
                    || (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(query))
                    || (p.getCriterioNegocio() != null && p.getCriterioNegocio().toLowerCase().contains(query));

            boolean coincideCriterio = "TODOS".equalsIgnoreCase(filtroCriterioActual)
                    || filtroCriterioActual.equalsIgnoreCase(p.getCriterioNegocio());

            boolean coincideEstado = "TODOS".equalsIgnoreCase(filtroEstadoActual)
                    || filtroEstadoActual.equalsIgnoreCase(p.getEstado());

            if (coincideTexto && coincideCriterio && coincideEstado) {
                listaPuntosFiltrada.add(p);
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
