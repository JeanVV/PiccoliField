package com.piccolifield.ui.asistencia;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.piccolifield.adapter.ColaboradorAdapter;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentColaboradoresBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ColaboradoresFragment extends Fragment {

    private FragmentColaboradoresBinding binding;
    private ApiService apiService;

    public ColaboradoresFragment() {
        // Required empty public constructor
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

        cargarColaboradores();
    }

    private void cargarColaboradores() {
        if (apiService == null) return;

        apiService.obtenerColaboradoresConAsistencia().enqueue(new Callback<ApiResponse<List<RegistroAsistencia>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<RegistroAsistencia>>> call, @NonNull Response<ApiResponse<List<RegistroAsistencia>>> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<RegistroAsistencia> lista = response.body().getData();
                    if (lista.isEmpty()) {
                        binding.layoutEmptyCollaboratorsState.setVisibility(View.VISIBLE);
                        binding.rvCollaborators.setVisibility(View.GONE);
                    } else {
                        binding.layoutEmptyCollaboratorsState.setVisibility(View.GONE);
                        binding.rvCollaborators.setVisibility(View.VISIBLE);
                        binding.rvCollaborators.setLayoutManager(new LinearLayoutManager(getContext()));
                        binding.rvCollaborators.setAdapter(new ColaboradorAdapter(lista));
                    }
                } else {
                    Log.e("ColaboradoresFragment", "Error respuesta: " + response.code());
                    binding.layoutEmptyCollaboratorsState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<RegistroAsistencia>>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                Log.e("ColaboradoresFragment", "Error red: " + t.getMessage());
                binding.layoutEmptyCollaboratorsState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
