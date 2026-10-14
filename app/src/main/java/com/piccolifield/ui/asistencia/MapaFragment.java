package com.piccolifield.ui.asistencia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.SupportMapFragment;
import com.piccolifield.R;
import com.piccolifield.databinding.FragmentMapaBinding;

public class MapaFragment extends Fragment {

    private FragmentMapaBinding binding;

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

        // Inicializar SupportMapFragment como fragmento hijo de forma segura
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapContainer);

            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance();
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.mapContainer, mapFragment)
                        .commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (binding.btnViewDetails != null) {
            binding.btnViewDetails.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new DetalleAsistenciaFragment())
                    .addToBackStack(null)
                    .commit()
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
