package com.piccolifield.ui.asistencia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.piccolifield.R;
import com.piccolifield.data.remote.dto.response.AsistenciaResponse;
import com.piccolifield.databinding.FragmentMapaBinding;

import java.util.Locale;

public class MapaFragment extends Fragment {

    private FragmentMapaBinding binding;
    private AsistenciaViewModel viewModel;
    private GoogleMap googleMap;
    private AsistenciaResponse ultimaAsistencia;

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

        if (binding.mapViewLite != null) {
            binding.mapViewLite.onCreate(savedInstanceState);
        }

        viewModel = new ViewModelProvider(requireActivity()).get(AsistenciaViewModel.class);

        setupListeners();

        viewModel.getUltimaAsistencia().observe(getViewLifecycleOwner(), asistencia -> {
            ultimaAsistencia = asistencia;
            actualizarUiAsistencia(asistencia);
        });

        viewModel.cargarUltimaAsistencia();
    }

    private void actualizarUiAsistencia(AsistenciaResponse asistencia) {
        if (binding == null) return;

        if (asistencia != null) {
            String dir = asistencia.getDireccion();
            if (dir == null || dir.trim().isEmpty()) {
                dir = String.format(Locale.getDefault(), "%.6f, %.6f", asistencia.getLatitud(), asistencia.getLongitud());
            }
            binding.tvDireccionAsistencia.setText(dir);
            binding.cardDireccionContainer.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        } else {
            binding.tvDireccionAsistencia.setText("Aún no has marcado asistencia");
            binding.cardDireccionContainer.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        }

        if (binding.mapViewLite != null) {
            binding.mapViewLite.getMapAsync(gMap -> {
                googleMap = gMap;
                googleMap.clear();
                googleMap.getUiSettings().setScrollGesturesEnabled(false);
                googleMap.getUiSettings().setZoomGesturesEnabled(false);
                googleMap.getUiSettings().setZoomControlsEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);

                if (asistencia != null) {
                    LatLng punto = new LatLng(asistencia.getLatitud(), asistencia.getLongitud());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(punto, 16f));
                    googleMap.addMarker(new MarkerOptions()
                            .position(punto)
                            .title("Asistencia registrada"));
                }
            });
        }
    }

    private void setupListeners() {
        if (binding == null) return;

        // Botón Ver Detalles
        if (binding.btnViewDetails != null) {
            binding.btnViewDetails.setOnClickListener(v ->
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new DetalleAsistenciaFragment())
                            .addToBackStack(null)
                            .commit()
            );
        }

        // Botón Cómo Llegar
        if (binding.btnHowToGetThere != null) {
            binding.btnHowToGetThere.setOnClickListener(v -> abrirNavegacionGoogleMaps());
        }

        // Toggle Mapa / Lista
        if (binding.btnToggleList != null) {
            binding.btnToggleList.setOnClickListener(v ->
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new ColaboradoresFragment())
                            .commit()
            );
        }
    }

    private void abrirNavegacionGoogleMaps() {
        try {
            double lat = -6.7714;
            double lng = -79.8409;
            if (ultimaAsistencia != null) {
                lat = ultimaAsistencia.getLatitud();
                lng = ultimaAsistencia.getLongitud();
            }
            Uri gmmIntentUri = Uri.parse(String.format(Locale.US, "google.navigation:q=%f,%f&mode=d", lat, lng));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Intent webMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, "https://maps.google.com/?q=%f,%f", lat, lng)));
                startActivity(webMapIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Abriendo mapa...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (binding != null && binding.mapViewLite != null) {
            binding.mapViewLite.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && binding.mapViewLite != null) {
            binding.mapViewLite.onResume();
        }
    }

    @Override
    public void onPause() {
        if (binding != null && binding.mapViewLite != null) {
            binding.mapViewLite.onPause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (binding != null && binding.mapViewLite != null) {
            binding.mapViewLite.onStop();
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null && binding.mapViewLite != null) {
            binding.mapViewLite.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (binding != null && binding.mapViewLite != null) {
            binding.mapViewLite.onLowMemory();
        }
    }

    @Override
    public void onDestroyView() {
        if (binding != null && binding.mapViewLite != null) {
            binding.mapViewLite.onDestroy();
        }
        super.onDestroyView();
        binding = null;
    }
}
