package com.piccolifield.ui.asistencia;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.piccolifield.R;
import com.piccolifield.databinding.FragmentMapaBinding;

public class MapaFragment extends Fragment {

    private FragmentMapaBinding binding;
    private GoogleMap mMap;

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

        setupMap();
        setupListeners();
    }

    private void setupMap() {
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapContainer);

            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance();
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.mapContainer, mapFragment)
                        .commit();
            }

            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                // Coordenadas de Sede Principal Chiclayo
                LatLng sedeChiclayo = new LatLng(-6.7714, -79.8409);

                mMap.clear();
                mMap.addMarker(new MarkerOptions()
                        .position(sedeChiclayo)
                        .title("Sede Principal Chiclayo - Piccoli Cocktail"));

                // Dibujar Rango de Geocerca (Círculo de 100m)
                mMap.addCircle(new CircleOptions()
                        .center(sedeChiclayo)
                        .radius(100) // 100 metros
                        .strokeColor(Color.parseColor("#C99A32"))
                        .fillColor(Color.parseColor("#33C99A32"))
                        .strokeWidth(3f));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sedeChiclayo, 16f));
            });
        } catch (Exception e) {
            e.printStackTrace();
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

        // FAB Mi Ubicación
        if (binding.fabMyLocation != null) {
            binding.fabMyLocation.setOnClickListener(v -> abrirNavegacionGoogleMaps());
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
            Uri gmmIntentUri = Uri.parse("google.navigation:q=-6.7714,-79.8409&mode=d");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Intent webMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=-6.7714,-79.8409"));
                startActivity(webMapIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Abriendo mapa hacia Sede Chiclayo...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
