package com.piccolifield.ui.despacho;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.piccolifield.R;
import com.piccolifield.adapter.ProspectoAdapter;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Prospecto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProspectosFragment extends Fragment {

    private RecyclerView rvProspectos;
    private ProspectoAdapter adapter;
    private List<Prospecto> listaProspectos = new ArrayList<>();
    private List<Prospecto> listaFiltrada = new ArrayList<>();
    
    private EditText etBuscarProspecto;

    public ProspectosFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prospectos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvProspectos = view.findViewById(R.id.rvProspectos);
        etBuscarProspecto = view.findViewById(R.id.etBuscarProspecto);
        FloatingActionButton fabNuevoProspecto = view.findViewById(R.id.fabNuevoProspecto);

        rvProspectos.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProspectoAdapter(listaFiltrada, prospecto -> {
            Intent intent = new Intent(requireActivity(), DetalleProspectoActivity.class);
            intent.putExtra("ID_PROSPECTO", prospecto.getId());
            intent.putExtra("NOMBRE_PROSPECTO", prospecto.getNombre());
            startActivity(intent);
        });
        rvProspectos.setAdapter(adapter);

        fabNuevoProspecto.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), NuevoProspectoActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.btnFiltroProspectos).setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Abrir Filtros (Bottom Sheet)", Toast.LENGTH_SHORT).show()
        );

        etBuscarProspecto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        cargarProspectos();
    }

    private void cargarProspectos() {
        ApiService apiService = RetrofitClient.createService();
        apiService.listarProspectos(null, null, null).enqueue(new Callback<ApiResponse<List<Prospecto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Prospecto>>> call, @NonNull Response<ApiResponse<List<Prospecto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaProspectos.clear();
                    listaProspectos.addAll(response.body().getData());
                    listaFiltrada.clear();
                    listaFiltrada.addAll(listaProspectos);
                    adapter.notifyDataSetChanged();
                } else {
                    cargarMock();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Prospecto>>> call, @NonNull Throwable t) {
                cargarMock();
            }
        });
    }

    private void cargarMock() {
        listaProspectos.clear();
        listaProspectos.add(new Prospecto(1, "Bodega Don Pepe", "Av. Grau 123", "NUEVO", "Lima Centro", "Bodega"));
        listaProspectos.add(new Prospecto(2, "Minimarket El Sol", "Av. Arequipa 456", "CONTACTADO", "Miraflores", "Minimarket"));
        listaProspectos.add(new Prospecto(3, "Licorería 24 Horas", "Calle las Flores 12", "NEGOCIACION", "San Isidro", "Licorería"));
        listaFiltrada.clear();
        listaFiltrada.addAll(listaProspectos);
        adapter.notifyDataSetChanged();
    }

    private void filtrar(String query) {
        listaFiltrada.clear();
        if (query.trim().isEmpty()) {
            listaFiltrada.addAll(listaProspectos);
        } else {
            String q = query.toLowerCase();
            for (Prospecto p : listaProspectos) {
                if ((p.getNombre() != null && p.getNombre().toLowerCase().contains(q)) ||
                    (p.getZona() != null && p.getZona().toLowerCase().contains(q))) {
                    listaFiltrada.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}