package com.piccolifield.ui.despacho;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.piccolifield.R;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Prospecto;
import com.piccolifield.model.Seguimiento;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoSeguimientoBottomSheet extends BottomSheetDialogFragment {

    private Prospecto prospecto;
    private Runnable onSuccess;

    public NuevoSeguimientoBottomSheet(Prospecto prospecto, Runnable onSuccess) {
        this.prospecto = prospecto;
        this.onSuccess = onSuccess;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bottom_sheet_nuevo_seguimiento, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinnerEstado = view.findViewById(R.id.spinnerEstadoSeguimiento);
        EditText etNotas = view.findViewById(R.id.etNotasSeguimiento);

        String[] estados = {"NUEVO", "CONTACTADO", "NEGOCIACION", "RECHAZADO", "CONVERTIDO"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);

        // Preseleccionar el estado actual
        if (prospecto.getEstado() != null) {
            for (int i = 0; i < estados.length; i++) {
                if (estados[i].equals(prospecto.getEstado())) {
                    spinnerEstado.setSelection(i);
                    break;
                }
            }
        }

        view.findViewById(R.id.btnGuardarSeguimiento).setOnClickListener(v -> {
            String notas = etNotas.getText().toString().trim();
            if (notas.isEmpty()) {
                Toast.makeText(requireContext(), "Las notas son obligatorias", Toast.LENGTH_SHORT).show();
                return;
            }

            String nuevoEstado = spinnerEstado.getSelectedItem().toString();
            String fechaHoy = new SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault()).format(new Date());

            Seguimiento nuevoSeguimiento = new Seguimiento(
                    0, 
                    prospecto.getId(), 
                    fechaHoy, 
                    notas, 
                    prospecto.getEstado(), 
                    nuevoEstado
            );

            guardarSeguimientoApi(nuevoSeguimiento);
        });
    }

    private void guardarSeguimientoApi(Seguimiento seguimiento) {
        ApiService apiService = RetrofitClient.createService();
        apiService.registrarSeguimiento(prospecto.getId(), seguimiento).enqueue(new Callback<ApiResponse<Seguimiento>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Seguimiento>> call, @NonNull Response<ApiResponse<Seguimiento>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Seguimiento guardado", Toast.LENGTH_SHORT).show();
                    // Si el estado cambió, también podríamos actualizar el Prospecto aquí 
                    // o dejar que el backend lo haga.
                    if (onSuccess != null) onSuccess.run();
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), "Error al guardar seguimiento", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Seguimiento>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Simulando guardado exitoso", Toast.LENGTH_SHORT).show();
                if (onSuccess != null) onSuccess.run();
                dismiss();
            }
        });
    }
}