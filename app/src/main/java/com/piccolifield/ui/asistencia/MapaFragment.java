package com.piccolifield.ui.asistencia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.piccolifield.R;
import com.piccolifield.adapter.RecentPersonnelAdapter;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.dto.response.AsistenciaResponse;
import com.piccolifield.databinding.FragmentMapaBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaFragment extends Fragment {

    private FragmentMapaBinding binding;
    private MonitoreoMapaViewModel viewModel;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ApiService apiService;
    private RecentPersonnelAdapter listAdapter;

    private double latSede = -6.7714;
    private double lngSede = -79.8409;
    private String nombreSede = "Sede Principal Chiclayo - Piccoli Cocktail";
    private float radioGeocerca = 100f;
    private boolean esAdmin = false;

    private LatLng userLocationLatLng;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean fine = Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarse = Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (fine || coarse) {
                    obtenerUbicacionActual();
                } else {
                    Toast.makeText(getContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                }
            });

    public MapaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMapaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (binding.mapView != null) {
            binding.mapView.onCreate(savedInstanceState);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        apiService = RetrofitClient.createService();
        viewModel = new ViewModelProvider(requireActivity()).get(MonitoreoMapaViewModel.class);

        // Consultar rol para personalizar comportamiento
        String rol = SplashStorage.obtenerRol(requireContext());
        esAdmin = "ADMINISTRADOR".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol) || "SUPERVISOR".equalsIgnoreCase(rol);
        viewModel.setEsAdministrador(esAdmin);

        setupListeners();
        observarViewModel();
        solicitarPermisosYObtenerUbicacion();
        configurarMapa();
        cargarListaColaboradores();
    }

    private void observarViewModel() {
        viewModel.getSedeAsignada().observe(getViewLifecycleOwner(), sedeInfo -> {
            if (sedeInfo != null && binding != null) {
                latSede = sedeInfo.getLatitud();
                lngSede = sedeInfo.getLongitud();
                nombreSede = sedeInfo.getNombre();
                radioGeocerca = sedeInfo.getRadioMetros();
                binding.tvSedeNombre.setText(nombreSede);
                actualizarMapa();
            }
        });

        viewModel.getMensajeEstado().observe(getViewLifecycleOwner(), mensaje -> {
            if (binding != null && mensaje != null) {
                binding.tvStatusMessage.setText(mensaje);
            }
        });

        viewModel.getEnGeocerca().observe(getViewLifecycleOwner(), dentro -> {
            if (binding == null || dentro == null) return;
            if (esAdmin) {
                int count = viewModel.getColaboradoresEnZonaCount().getValue() != null ? viewModel.getColaboradoresEnZonaCount().getValue() : 0;
                binding.tvGeofenceBadge.setText(count + " PRESENTES EN ZONA");
                binding.tvGeofenceBadge.setBackgroundResource(R.drawable.bg_badge_on_time);
                binding.tvGeofenceBadge.setTextColor(requireContext().getColor(R.color.status_on_time_text));
            } else {
                AsistenciaResponse asistencia = viewModel.getUltimaAsistencia().getValue();
                if (asistencia != null) {
                    binding.tvGeofenceBadge.setText("ASISTENCIA REGISTRADA");
                    binding.tvGeofenceBadge.setBackgroundResource(R.drawable.bg_badge_on_time);
                    binding.tvGeofenceBadge.setTextColor(requireContext().getColor(R.color.status_on_time_text));
                } else if (dentro) {
                    binding.tvGeofenceBadge.setText("EN RANGO");
                    binding.tvGeofenceBadge.setBackgroundResource(R.drawable.bg_badge_on_time);
                    binding.tvGeofenceBadge.setTextColor(requireContext().getColor(R.color.status_on_time_text));
                } else {
                    binding.tvGeofenceBadge.setText("FUERA DE RANGO");
                    binding.tvGeofenceBadge.setBackgroundResource(R.drawable.bg_badge_late);
                    binding.tvGeofenceBadge.setTextColor(requireContext().getColor(R.color.status_late_text));
                }
            }
        });

        viewModel.getUltimaAsistencia().observe(getViewLifecycleOwner(), asistencia -> {
            if (binding == null) return;
            if (asistencia != null) {
                String dir = asistencia.getDireccion();
                if (dir == null || dir.trim().isEmpty()) {
                    dir = String.format(Locale.getDefault(), "Lat: %.5f, Lng: %.5f", asistencia.getLatitud(), asistencia.getLongitud());
                }
                binding.tvDireccionAsistencia.setText(dir);
            } else {
                binding.tvDireccionAsistencia.setText("Sede asignada: Chiclayo Principal");
            }
        });

        viewModel.cargarUltimaAsistencia();
    }

    private void solicitarPermisosYObtenerUbicacion() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        viewModel.actualizarUbicacionUsuario(location.getLatitude(), location.getLongitude());
                        obtenerDireccionTexto(location.getLatitude(), location.getLongitude());
                        actualizarMapa();
                    } else {
                        Toast.makeText(getContext(), "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al obtener ubicación", Toast.LENGTH_SHORT).show());
    }

    private void obtenerDireccionTexto(double lat, double lng) {
        if (!isAdded() || binding == null) return;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            String dirText = null;
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    if (address.getMaxAddressLineIndex() >= 0) {
                        dirText = address.getAddressLine(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final String finalDir = dirText;
            mainHandler.post(() -> {
                if (isAdded() && binding != null && finalDir != null && viewModel.getUltimaAsistencia().getValue() == null) {
                    binding.tvDireccionAsistencia.setText(finalDir);
                }
            });
        });
    }

    private void configurarMapa() {
        if (binding.mapView == null) return;
        binding.mapView.getMapAsync(gMap -> {
            googleMap = gMap;
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    googleMap.setMyLocationEnabled(true);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
            actualizarMapa();
        });
    }

    private void actualizarMapa() {
        if (googleMap == null) return;
        googleMap.clear();

        LatLng sedeLatLng = new LatLng(latSede, lngSede);

        // Marcador Sede Asignada
        googleMap.addMarker(new MarkerOptions()
                .position(sedeLatLng)
                .title(nombreSede));

        // Círculo de Rango de Geocerca
        googleMap.addCircle(new CircleOptions()
                .center(sedeLatLng)
                .radius(radioGeocerca)
                .strokeColor(Color.parseColor("#C99A32"))
                .fillColor(Color.parseColor("#33C99A32"))
                .strokeWidth(4f));

        // Marcador del Usuario si existe
        if (userLocationLatLng != null) {
            googleMap.addMarker(new MarkerOptions()
                    .position(userLocationLatLng)
                    .title(esAdmin ? "Tu ubicación (Administrador)" : "Tu ubicación"));

            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(sedeLatLng)
                    .include(userLocationLatLng)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sedeLatLng, 16f));
        }
    }

    private void setupListeners() {
        if (binding == null) return;

        // Toggle Mapa / Lista
        binding.btnToggleMap.setOnClickListener(v -> mostrarModoMapa());
        binding.btnToggleList.setOnClickListener(v -> mostrarModoLista());

        // Botón "Cómo llegar" (Intent a Google Maps Navigation)
        binding.btnHowToGetThere.setOnClickListener(v -> abrirNavegacionGoogleMaps());

        // Botón "Ver detalles"
        binding.btnViewDetails.setText(esAdmin ? "Ver colaboradores" : "Ver detalles");
        binding.btnViewDetails.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new DetalleAsistenciaFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // FAB Mi Ubicación
        if (binding.fabMyLocation != null) {
            binding.fabMyLocation.setOnClickListener(v -> {
                solicitarPermisosYObtenerUbicacion();
                if (userLocationLatLng != null && googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocationLatLng, 16f));
                }
            });
        }
    }

    private void mostrarModoMapa() {
        if (binding == null) return;
        binding.mapContainer.setVisibility(View.VISIBLE);
        binding.rvCollaboratorsList.setVisibility(View.GONE);

        binding.btnToggleMap.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.cafe_profundo));
        binding.btnToggleMap.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        binding.btnToggleList.setBackgroundTintList(null);
        binding.btnToggleList.setTextColor(ContextCompat.getColor(requireContext(), R.color.cafe_profundo));
    }

    private void mostrarModoLista() {
        if (binding == null) return;
        binding.mapContainer.setVisibility(View.GONE);
        binding.rvCollaboratorsList.setVisibility(View.VISIBLE);

        binding.btnToggleList.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.cafe_profundo));
        binding.btnToggleList.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        binding.btnToggleMap.setBackgroundTintList(null);
        binding.btnToggleMap.setTextColor(ContextCompat.getColor(requireContext(), R.color.cafe_profundo));
    }

    private void cargarListaColaboradores() {
        if (apiService == null || binding == null) return;

        apiService.obtenerPersonalReciente().enqueue(new Callback<ApiResponse<List<RegistroAsistencia>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<RegistroAsistencia>>> call, @NonNull Response<ApiResponse<List<RegistroAsistencia>>> response) {
                if (!isAdded() || binding == null) return;
                List<RegistroAsistencia> lista = null;
                if (response.isSuccessful() && response.body() != null) {
                    lista = response.body().getData();
                }
                if (lista == null || lista.isEmpty()) {
                    lista = SplashStorage.obtenerRegistrosAsistenciaLocales(getContext());
                }
                if (lista != null) {
                    viewModel.setColaboradoresEnZonaCount(lista.size());
                }
                configurarAdapterLista(lista);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<RegistroAsistencia>>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                List<RegistroAsistencia> lista = SplashStorage.obtenerRegistrosAsistenciaLocales(getContext());
                if (lista != null) {
                    viewModel.setColaboradoresEnZonaCount(lista.size());
                }
                configurarAdapterLista(lista);
            }
        });
    }

    private void configurarAdapterLista(List<RegistroAsistencia> lista) {
        if (binding == null || !isAdded()) return;
        List<RegistroAsistencia> finalLista = lista != null ? lista : new ArrayList<>();
        listAdapter = new RecentPersonnelAdapter(finalLista);
        listAdapter.setOnItemClickListener(item -> {
            DetalleAsistenciaFragment fragment = DetalleAsistenciaFragment.newInstance(item);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        binding.rvCollaboratorsList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCollaboratorsList.setAdapter(listAdapter);
    }

    private void abrirNavegacionGoogleMaps() {
        try {
            Uri gmmIntentUri = Uri.parse(String.format(Locale.US, "google.navigation:q=%f,%f&mode=d", latSede, lngSede));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Intent webMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, "https://maps.google.com/?q=%f,%f", latSede, lngSede)));
                startActivity(webMapIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Abriendo mapa hacia la sede...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (binding != null && binding.mapView != null) {
            binding.mapView.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && binding.mapView != null) {
            binding.mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (binding != null && binding.mapView != null) {
            binding.mapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (binding != null && binding.mapView != null) {
            binding.mapView.onStop();
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null && binding.mapView != null) {
            binding.mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (binding != null && binding.mapView != null) {
            binding.mapView.onLowMemory();
        }
    }

    @Override
    public void onDestroyView() {
        if (binding != null && binding.mapView != null) {
            binding.mapView.onDestroy();
        }
        super.onDestroyView();
        binding = null;
    }
}
