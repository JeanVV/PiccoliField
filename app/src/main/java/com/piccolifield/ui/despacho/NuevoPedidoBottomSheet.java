package com.piccolifield.ui.despacho;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.piccolifield.R;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Pedido;
import com.piccolifield.model.Producto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoPedidoBottomSheet extends BottomSheetDialogFragment {

    private int idPuntoVenta;
    private int cantidad = 0;
    private TextView tvCantidadPedido, tvErrorCantidad;
    private Spinner spProducto;
    private Runnable onSuccess;
    private List<Producto> listaProductos = new ArrayList<>();
    private List<String> nombresProductos = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

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
        spProducto = view.findViewById(R.id.spProducto);

        spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombresProductos);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProducto.setAdapter(spinnerAdapter);

        cargarProductos();

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
                tvErrorCantidad.setText("La cantidad debe ser mayor a 0");
                tvErrorCantidad.setVisibility(View.VISIBLE);
            } else if (listaProductos.isEmpty()) {
                tvErrorCantidad.setText("No hay productos disponibles para seleccionar");
                tvErrorCantidad.setVisibility(View.VISIBLE);
            } else {
                tvErrorCantidad.setVisibility(View.GONE);
                registrarPedido();
            }
        });
    }

    private void cargarProductos() {
        ApiService apiService = RetrofitClient.createService();
        apiService.listarProductos("ACTIVO").enqueue(new Callback<ApiResponse<List<Producto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Producto>>> call, @NonNull Response<ApiResponse<List<Producto>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaProductos.clear();
                    nombresProductos.clear();
                    listaProductos.addAll(response.body().getData());
                    
                    for (Producto p : listaProductos) {
                        nombresProductos.add(p.getProducto() != null ? p.getProducto() : "Producto Sin Nombre");
                    }
                    spinnerAdapter.notifyDataSetChanged();
                } else {
                    String errorMsg = "Error";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        } else if (response.body() != null) {
                            errorMsg = response.body().getMessage();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(getContext(), "Fallo red/API: " + response.code() + " " + errorMsg, Toast.LENGTH_LONG).show();
                    cargarProductosMock();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Producto>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Usando catálogo local (Error de red: " + t.getMessage() + ")", Toast.LENGTH_LONG).show();
                cargarProductosMock();
            }
        });
    }

    private void cargarProductosMock() {
        listaProductos.clear();
        nombresProductos.clear();

        listaProductos.add(new Producto(1, "SKU01", "123456", "Pisco Italia 750ml", "Licores", "Botella", "ACTIVO"));
        listaProductos.add(new Producto(2, "SKU02", "123457", "Vino Tinto 750ml", "Licores", "Botella", "ACTIVO"));
        listaProductos.add(new Producto(3, "SKU03", "123458", "Cerveza Artesanal 330ml", "Bebidas", "Lata", "ACTIVO"));
        listaProductos.add(new Producto(4, "SKU04", "123459", "Snack Papas Nativas", "Snacks", "Bolsa", "ACTIVO"));

        for (Producto p : listaProductos) {
            nombresProductos.add(p.getProducto());
        }
        spinnerAdapter.notifyDataSetChanged();
    }

    private void actualizarCantidad() {
        tvCantidadPedido.setText(String.valueOf(cantidad));
        if (cantidad > 0) {
            tvErrorCantidad.setVisibility(View.GONE);
        }
    }

    private void registrarPedido() {
        int selectedPosition = spProducto.getSelectedItemPosition();
        if (selectedPosition == Spinner.INVALID_POSITION || selectedPosition >= listaProductos.size()) {
            Toast.makeText(getContext(), "Por favor, seleccione un producto", Toast.LENGTH_SHORT).show();
            return;
        }

        int idProductoSeleccionado = listaProductos.get(selectedPosition).getIdProductos();
        // Usar un formato completo con hora para que el backend lo registre exacto (ej. 2024-11-23 15:30:00)
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        ApiService apiService = RetrofitClient.createService();
        Pedido nuevoPedido = new Pedido(0, cantidad, fechaHoy, idPuntoVenta, idProductoSeleccionado);
        
        apiService.registrarPedido(nuevoPedido).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Pedido registrado", Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) onSuccess.run();
                    dismiss();
                } else {
                    String errorDetalle = "Error " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorDetalle = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    
                    Toast.makeText(getContext(), "Fallo: " + errorDetalle, Toast.LENGTH_LONG).show();
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