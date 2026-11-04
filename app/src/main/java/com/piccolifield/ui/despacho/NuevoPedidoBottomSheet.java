package com.piccolifield.ui.despacho;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.piccolifield.R;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Pedido;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoPedidoBottomSheet extends BottomSheetDialogFragment {

    private int idPuntoVenta;
    private int cantidad = 0;
    private TextView tvCantidadPedido, tvErrorCantidad;
    private Runnable onSuccess;

    public NuevoPedidoBottomSheet(int idPuntoVenta, Runnable onSuccess) {
        this.idPuntoVenta = idPuntoVenta;
        this.onSuccess = onSuccess;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bottom_sheet_nuevo_pedido, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnRestar = view.findViewById(R.id.btnRestar);
        MaterialButton btnSumar = view.findViewById(R.id.btnSumar);
        MaterialButton btnConfirmar = view.findViewById(R.id.btnConfirmarPedido);
        tvCantidadPedido = view.findViewById(R.id.tvCantidadPedido);
        tvErrorCantidad = view.findViewById(R.id.tvErrorCantidad);

        btnRestar.setOnClickListener(v -> {
            if (cantidad > 0) {
                cantidad--;
                actualizarCantidad();
            }
        });

        btnSumar.setOnClickListener(v -> {
            cantidad++;
            actualizarCantidad();
        });

        btnConfirmar.setOnClickListener(v -> {
            if (cantidad <= 0) {
                tvErrorCantidad.setVisibility(View.VISIBLE);
            } else {
                tvErrorCantidad.setVisibility(View.GONE);
                registrarPedido();
            }
        });
    }

    private void actualizarCantidad() {
        tvCantidadPedido.setText(String.valueOf(cantidad));
        if (cantidad > 0) {
            tvErrorCantidad.setVisibility(View.GONE);
        }
    }

    private void registrarPedido() {
        ApiService apiService = RetrofitClient.createService();
        Pedido nuevoPedido = new Pedido(0, cantidad, "2024-01-01", idPuntoVenta, 1);
        
        apiService.registrarPedido(nuevoPedido).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Pedido registrado", Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) onSuccess.run();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Error al registrar (422)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Simulando registro exitoso", Toast.LENGTH_SHORT).show();
                if (onSuccess != null) onSuccess.run();
                dismiss();
            }
        });
    }
}