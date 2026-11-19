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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.piccolifield.DetallePuntoDistribucionActivity;
import com.piccolifield.R;
import com.piccolifield.adapter.ProspectoAdapter;
import com.piccolifield.adapter.PuntoDistribucionAdapter;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.request.EstadoRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentPuntosDespachoBinding;
import com.piccolifield.model.Prospecto;
import com.piccolifield.model.PuntoDistribucion;
import com.piccolifield.sharedpreferences.PuntosDistribucionStorage;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PuntosDespachoFragment extends Fragment implements OnMapReadyCallback {

    private FragmentPuntosDespachoBinding binding;
    private PuntoDistribucionAdapter adapter;
    private ProspectoAdapter prospectoAdapter;
    private final List<PuntoDistribucion> listaPuntosOriginal = new ArrayList<>();
    private final List<PuntoDistribucion> listaPuntosFiltrada = new ArrayList<>();
    private final List<Prospecto> listaProspectosOriginal = new ArrayList<>();
    private final List<Prospecto> listaProspectosFiltrada = new ArrayList<>();

    private GoogleMap googleMap;
    private String filtroCriterioActual = "TODOS";
    private String filtroEstadoActual = "TODOS";
    private static final String ARG_INITIAL_TAB = "arg_initial_tab";
    private int tabActual = 1; // 0 = Mapa, 1 = Lista, 2 = Prospectos
    private boolean cargandoPuntos = false;
    private boolean cargandoProspectos = false;

    public PuntosDespachoFragment() {
        // Required empty public constructor
    }

    public static PuntosDespachoFragment newInstance(int initialTab) {
        PuntosDespachoFragment fragment = new PuntosDespachoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INITIAL_TAB, initialTab);
        fragment.setArguments(args);
        return fragment;
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
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }

        setupRecyclerView();
        setupSpinnersFiltros();
        if (getArguments() != null) {
            tabActual = getArguments().getInt(ARG_INITIAL_TAB, 1);
        }
        cargarDatosDesdeApi();
        cargarProspectosDesdeApi();
        setupToggleViews();
        seleccionarTab(tabActual);
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

        if (listaPuntosFiltrada.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        LatLng ultimaPosicion = null;
        int validCount = 0;

        for (PuntoDistribucion p : listaPuntosFiltrada) {
            double lat = p.getLatitud();
            double lng = p.getLongitud();
            if (Double.isNaN(lat) || Double.isNaN(lng) || (lat == 0.0 && lng == 0.0)) {
                continue;
            }
            LatLng posicion = new LatLng(lat, lng);
            googleMap.addMarker(new MarkerOptions()
                    .position(posicion)
                    .title(p.getNombre() + " (" + p.getCodigo() + ")")
                    .snippet(p.getDireccion() + " · " + p.getCriterioNegocio()));
            boundsBuilder.include(posicion);
            ultimaPosicion = posicion;
            validCount++;
        }

        if (validCount == 1 && ultimaPosicion != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ultimaPosicion, 15f));
        } else if (validCount > 1 && ultimaPosicion != null) {
            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
            } catch (Exception e) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ultimaPosicion, 12f));
            }
        }
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

        binding.rvProspectosPuntos.setLayoutManager(new LinearLayoutManager(requireContext()));
        prospectoAdapter = new ProspectoAdapter(
                listaProspectosFiltrada,
                this::mostrarDetalleProspecto,
                this::mostrarDialogoCambiarEstadoProspecto
        );
        binding.rvProspectosPuntos.setAdapter(prospectoAdapter);
    }

    private void mostrarDetalleProspecto(Prospecto prospecto) {
        if (!isAdded() || prospecto == null) return;
        DetalleProspectoBottomSheet bottomSheet = DetalleProspectoBottomSheet.newInstance(prospecto);
        bottomSheet.setOnEstadoActualizadoListener((prospectoActualizado, nuevoEstado) -> {
            if (!isAdded()) return;
            aplicarCambioEstadoExitoso(prospectoActualizado, nuevoEstado);
        });
        bottomSheet.show(getChildFragmentManager(), "DetalleProspectoBottomSheet");
    }

    private void mostrarDialogoCambiarEstadoProspecto(Prospecto prospecto) {
        if (!isAdded() || prospecto == null) return;

        String estadoActual = prospecto.getEstado() != null ? prospecto.getEstado().toUpperCase(Locale.ROOT) : "NUEVO";
        if ("CONVERTIDO".equals(estadoActual)) {
            Toast.makeText(requireContext(), "Este prospecto ya fue convertido a Punto de Distribución", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] estadosDisponibles = {"NUEVO", "EN_NEGOCIACION", "CONVERTIDO", "DESCARTADO"};
        final String[] etiquetas = {
                "NUEVO",
                "EN NEGOCIACIÓN",
                "CONVERTIDO (Pasa a Punto de Distribución)",
                "DESCARTADO"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Estado de " + (prospecto.getNombre() != null ? prospecto.getNombre() : "Prospecto"))
                .setItems(etiquetas, (dialog, which) -> {
                    String nuevoEstado = estadosDisponibles[which];
                    actualizarEstadoProspectoApi(prospecto, nuevoEstado);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarEstadoProspectoApi(Prospecto prospecto, String nuevoEstado) {
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }

        ApiService apiService = RetrofitClient.createService();
        apiService.actualizarEstadoProspecto(prospecto.getId(), new EstadoRequest(nuevoEstado))
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            aplicarCambioEstadoExitoso(prospecto, nuevoEstado);
                        } else if (response.code() == 400) {
                            intentarActualizarViaPutGeneral(apiService, prospecto, nuevoEstado, extraerMensajeError(response));
                        } else {
                            Toast.makeText(requireContext(),
                                    extraerMensajeError(response),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                "Error de red al cambiar estado: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void intentarActualizarViaPutGeneral(ApiService apiService, Prospecto prospecto, String nuevoEstado, String errorOriginal) {
        apiService.actualizarProspecto(prospecto.getId(), new EstadoRequest(nuevoEstado))
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            aplicarCambioEstadoExitoso(prospecto, nuevoEstado);
                        } else {
                            String detalle = errorOriginal != null && !errorOriginal.isEmpty()
                                    ? errorOriginal
                                    : extraerMensajeError(response);
                            Toast.makeText(requireContext(), detalle, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                errorOriginal != null && !errorOriginal.isEmpty() ? errorOriginal : "Error al actualizar estado",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void aplicarCambioEstadoExitoso(Prospecto prospecto, String nuevoEstado) {
        prospecto.setEstado(nuevoEstado);
        prospectoAdapter.notifyDataSetChanged();

        if ("CONVERTIDO".equalsIgnoreCase(nuevoEstado)) {
            Toast.makeText(requireContext(),
                    "Prospecto convertido exitosamente a Punto de Venta y Distribución",
                    Toast.LENGTH_LONG).show();
            cargarDatosDesdeApi();
            cargarProspectosDesdeApi();
        } else {
            Toast.makeText(requireContext(),
                    "Estado actualizado a " + nuevoEstado,
                    Toast.LENGTH_SHORT).show();
            cargarProspectosDesdeApi();
        }
    }

    private String extraerMensajeError(Response<?> response) {
        String fallback = "No se pudo actualizar el estado (HTTP " + response.code() + ")";
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                android.util.Log.e("PuntosDespacho", "Error HTTP " + response.code() + ": " + raw);
                org.json.JSONObject json = new org.json.JSONObject(raw);
                String msg = json.optString("message", json.optString("error", json.optString("msg", "")));
                if (!msg.isEmpty()) {
                    return msg + " (HTTP " + response.code() + ")";
                }
            }
        } catch (Exception ignored) {}
        return fallback;
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
        cargandoPuntos = true;
        actualizarIndicadorCarga();
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }
        ApiService apiService = RetrofitClient.createService();
        apiService.listarPuntosVenta(null, null, null, null).enqueue(new Callback<ApiResponse<List<PuntoDistribucion>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PuntoDistribucion>>> call, @NonNull Response<ApiResponse<List<PuntoDistribucion>>> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    cargandoPuntos = false;
                    listaPuntosOriginal.clear();
                    listaPuntosOriginal.addAll(response.body().getData());
                    sincronizarPuntosLocales();
                    actualizarUI();
                    actualizarIndicadorCarga();
                } else {
                    cargarPuntosConFiltroDistribucion(apiService);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PuntoDistribucion>>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                cargarPuntosConFiltroDistribucion(apiService);
            }
        });
    }

    private void cargarPuntosConFiltroDistribucion(ApiService apiService) {
        apiService.listarPuntosVenta(null, "DISTRIBUCION", null, null).enqueue(new Callback<ApiResponse<List<PuntoDistribucion>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PuntoDistribucion>>> call, @NonNull Response<ApiResponse<List<PuntoDistribucion>>> response) {
                if (!isAdded() || binding == null) return;
                cargandoPuntos = false;
                listaPuntosOriginal.clear();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    listaPuntosOriginal.addAll(response.body().getData());
                } else {
                    cargarPuntosRespaldo();
                }
                sincronizarPuntosLocales();
                actualizarUI();
                actualizarIndicadorCarga();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PuntoDistribucion>>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                cargandoPuntos = false;
                listaPuntosOriginal.clear();
                cargarPuntosRespaldo();
                sincronizarPuntosLocales();
                actualizarUI();
                actualizarIndicadorCarga();
            }
        });
    }

    private void cargarProspectosDesdeApi() {
        cargandoProspectos = true;
        actualizarIndicadorCarga();
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }
        ApiService apiService = RetrofitClient.createService();
        apiService.listarProspectos(null, null, null).enqueue(new Callback<ApiResponse<List<Prospecto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Prospecto>>> call, @NonNull Response<ApiResponse<List<Prospecto>>> response) {
                if (!isAdded() || binding == null) return;
                cargandoProspectos = false;
                if (response.isSuccessful() && response.body() != null) {
                    listaProspectosOriginal.clear();
                    if (response.body().getData() != null) {
                        listaProspectosOriginal.addAll(response.body().getData());
                    }
                } else {
                    String error = "Error " + response.code();
                    try { if (response.errorBody() != null) error = response.errorBody().string(); } catch (Exception ignored) {}
                    Toast.makeText(requireContext(),
                            "No se pudieron cargar los prospectos (HTTP " + error + ")",
                            Toast.LENGTH_LONG).show();
                }
                aplicarFiltrosProspectos();
                actualizarIndicadorCarga();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Prospecto>>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                cargandoProspectos = false;
                Toast.makeText(requireContext(),
                        "Error al cargar prospectos: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                aplicarFiltrosProspectos();
                actualizarIndicadorCarga();
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
        binding.btnToggleMapa.setOnClickListener(v -> seleccionarTab(0));
        binding.btnToggleLista.setOnClickListener(v -> seleccionarTab(1));
        binding.btnToggleProspectos.setOnClickListener(v -> {
            seleccionarTab(2);
            cargarProspectosDesdeApi();
        });
    }

    private void seleccionarTab(int tab) {
        if (binding == null) return;
        tabActual = tab;

        int colorCafe = ContextCompat.getColor(requireContext(), R.color.cafe_profundo);
        int colorBlanco = ContextCompat.getColor(requireContext(), R.color.white);

        // Reset all 3 toggle buttons to inactive white pill
        binding.btnToggleMapa.setBackgroundTintList(ColorStateList.valueOf(colorBlanco));
        binding.btnToggleMapa.setTextColor(colorCafe);
        binding.btnToggleLista.setBackgroundTintList(ColorStateList.valueOf(colorBlanco));
        binding.btnToggleLista.setTextColor(colorCafe);
        binding.btnToggleProspectos.setBackgroundTintList(ColorStateList.valueOf(colorBlanco));
        binding.btnToggleProspectos.setTextColor(colorCafe);

        binding.layoutMapView.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        binding.cardHero.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        binding.layoutStats.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        binding.rvListaPuntos.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);
        binding.rvProspectosPuntos.setVisibility(tab == 2 ? View.VISIBLE : View.GONE);

        if (tab == 0) {
            binding.btnToggleMapa.setBackgroundTintList(ColorStateList.valueOf(colorCafe));
            binding.btnToggleMapa.setTextColor(colorBlanco);
            binding.layoutMapView.post(this::actualizarPinesMapa);
        } else if (tab == 1) {
            binding.btnToggleLista.setBackgroundTintList(ColorStateList.valueOf(colorCafe));
            binding.btnToggleLista.setTextColor(colorBlanco);
        } else {
            binding.btnToggleProspectos.setBackgroundTintList(ColorStateList.valueOf(colorCafe));
            binding.btnToggleProspectos.setTextColor(colorBlanco);
        }
        actualizarIndicadorCarga();
    }

    private void actualizarIndicadorCarga() {
        if (binding == null) return;
        boolean cargandoActual = (tabActual == 2) ? cargandoProspectos : cargandoPuntos;
        binding.pbPuntosDespacho.setVisibility(cargandoActual ? View.VISIBLE : View.GONE);

        if (cargandoActual) {
            binding.tvEmptyPuntosDespacho.setVisibility(View.GONE);
        } else if (tabActual == 1) {
            binding.tvEmptyPuntosDespacho.setText("No hay puntos de distribución para mostrar");
            binding.tvEmptyPuntosDespacho.setVisibility(listaPuntosFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
        } else if (tabActual == 2) {
            binding.tvEmptyPuntosDespacho.setText("No hay prospectos registrados");
            binding.tvEmptyPuntosDespacho.setVisibility(listaProspectosFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            binding.tvEmptyPuntosDespacho.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        binding.btnMenu.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.fabMyLocation.setOnClickListener(v -> {
            if (googleMap != null && !listaPuntosFiltrada.isEmpty()) {
                actualizarPinesMapa();
            } else {
                Toast.makeText(requireContext(), "No hay puntos filtrados para centrar", Toast.LENGTH_SHORT).show();
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
                aplicarFiltrosProspectos();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void abrirFormularioNuevoPunto() {
        int siguienteNum = listaPuntosOriginal.size() + 1;
        NuevoPuntoDistribucionFragment fragment = new NuevoPuntoDistribucionFragment(siguienteNum, () -> {
            if (isAdded()) {
                seleccionarTab(2);
                cargarProspectosDesdeApi();
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
        actualizarPinesMapa();
        actualizarIndicadorCarga();
    }

    private void aplicarFiltrosProspectos() {
        if (binding == null || prospectoAdapter == null) return;
        String query = binding.etBuscarPuntos.getText() != null
                ? binding.etBuscarPuntos.getText().toString().trim().toLowerCase()
                : "";

        listaProspectosFiltrada.clear();
        for (Prospecto pr : listaProspectosOriginal) {
            boolean coincideTexto = query.isEmpty()
                    || (pr.getNombre() != null && pr.getNombre().toLowerCase().contains(query))
                    || (pr.getDireccion() != null && pr.getDireccion().toLowerCase().contains(query))
                    || (pr.getZona() != null && pr.getZona().toLowerCase().contains(query))
                    || (pr.getEstado() != null && pr.getEstado().toLowerCase().contains(query));

            if (coincideTexto) {
                listaProspectosFiltrada.add(pr);
            }
        }
        prospectoAdapter.notifyDataSetChanged();
        actualizarIndicadorCarga();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
