package com.piccolifield.ui.despacho;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.piccolifield.R;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.EstadoRequest;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Pedido;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstadoPedidoBottomSheet extends BottomSheetDialogFragment {

    private Pedido pedidoActual;
    private Runnable onSuccess;
    private String estadoSeleccionado = "REGISTRADO"; // Default

    public EstadoPedidoBottomSheet(Pedido pedido, Runnable onSuccess) {
        this.pedidoActual = pedido;
        this.onSuccess = onSuccess;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bottom_sheet_estado_pedido, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButtonToggleGroup toggleEstados = view.findViewById(R.id.toggleEstados);
        
        toggleEstados.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnRegistrado) {
                    estadoSeleccionado = "REGISTRADO";
                } else if (checkedId == R.id.btnPreparacion) {
                    estadoSeleccionado = "EN_PREPARACION";
                } else if (checkedId == R.id.btnEntregado) {
                    estadoSeleccionado = "ENTREGADO";
                }
            }
        });

        view.findViewById(R.id.btnConfirmarEstado).setOnClickListener(v -> actualizarEstado());
    }

    private void actualizarEstado() {
        ApiService apiService = RetrofitClient.createService();
        EstadoRequest request = new EstadoRequest();
        request.setEstado(estadoSeleccionado);
        
        apiService.actualizarEstadoPedido(pedidoActual.getIdPedido(), request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Estado actualizado", Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) onSuccess.run();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                // Si la red falla pero queremos probar la UX:
                Toast.makeText(getContext(), "Estado simulado: " + estadoSeleccionado, Toast.LENGTH_SHORT).show();
                if (onSuccess != null) onSuccess.run();
                dismiss();
            }
        });
    }
}