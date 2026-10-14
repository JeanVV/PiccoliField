package com.piccolifield.ui.asistencia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.piccolifield.R;
import com.piccolifield.databinding.FragmentAsistenciaHomeBinding;

public class AsistenciaHomeFragment extends Fragment {

    private FragmentAsistenciaHomeBinding binding;

    public AsistenciaHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAsistenciaHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar RecyclerView
        if (binding.rvRecentPersonnel != null) {
            binding.rvRecentPersonnel.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        // Listeners de búsqueda y notificación
        if (binding.btnFilter != null) {
            binding.btnFilter.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Filtrar resultados", Toast.LENGTH_SHORT).show()
            );
        }

        if (binding.btnNotification != null) {
            binding.btnNotification.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Notificaciones de asistencias", Toast.LENGTH_SHORT).show()
            );
        }

        // Navegación interna entre Fragments
        if (binding.cardGoToMap != null) {
            binding.cardGoToMap.setOnClickListener(v ->
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new MapaFragment())
                            .addToBackStack(null)
                            .commit()
            );
        }

        if (binding.cardGoToCollaborators != null) {
            binding.cardGoToCollaborators.setOnClickListener(v ->
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new ColaboradoresFragment())
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
