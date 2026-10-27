package com.piccolifield.ui.impulsador;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.piccolifield.R;
import com.piccolifield.data.model.ImpulsadorDashboard;
import com.piccolifield.data.model.ProductoBuscado;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.request.RegistrarVentaRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.sharedpreferences.SplashStorage;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImpulsadorHomeFragment extends Fragment {

    private TextView tvLocalAsignado, tvHorario, tvMetaVentas, tvAvanceVentas;
    private ProgressBar pbVentasImpulsador;
    private MaterialButton btnEscanear, btnReportarIncidencia;

    private ActivityResultLauncher<Intent> scannerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        String barcode = result.getData().getStringExtra("SCAN_RESULT");
                        if (barcode != null) {
                            buscarProducto(barcode);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_impulsador_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLocalAsignado = view.findViewById(R.id.tvLocalAsignado);
        tvHorario = view.findViewById(R.id.tvHorario);
        tvMetaVentas = view.findViewById(R.id.tvMetaVentas);
        tvAvanceVentas = view.findViewById(R.id.tvAvanceVentas);
        pbVentasImpulsador = view.findViewById(R.id.pbVentasImpulsador);
        btnEscanear = view.findViewById(R.id.btnEscanear);
        btnReportarIncidencia = view.findViewById(R.id.btnReportarIncidencia);
        
        btnEscanear.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ScannerActivity.class);
            scannerLauncher.launch(intent);
        });

        btnReportarIncidencia.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Próximamente: Incidencias", Toast.LENGTH_SHORT).show();
            // TODO: Abrir form
        });

        cargarDashboard();
    }

    private void buscarProducto(String codigoBarras) {
        Toast.makeText(getContext(), "Buscando: " + codigoBarras, Toast.LENGTH_SHORT).show();
        ApiService apiService = RetrofitClient.createService();
        apiService.buscarProductoPorCodigo(codigoBarras).enqueue(new Callback<ApiResponse<ProductoBuscado>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductoBuscado>> call, Response<ApiResponse<ProductoBuscado>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    mostrarBottomSheetVenta(response.body().getData(), codigoBarras);
                } else {
                    Toast.makeText(getContext(), "Producto no encontrado o no asignado a tu punto de venta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductoBuscado>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void mostrarBottomSheetVenta(ProductoBuscado producto, String codigoBarras) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet_venta, null);
        bottomSheetDialog.setContentView(view);
        
        ImageView ivProductoScan = view.findViewById(R.id.ivProductoScan);
        TextView tvProductoNombreScan = view.findViewById(R.id.tvProductoNombreScan);
        TextView tvProductoCodigoScan = view.findViewById(R.id.tvProductoCodigoScan);
        TextView tvProductoPrecioScan = view.findViewById(R.id.tvProductoPrecioScan);
        TextView tvProductoStockScan = view.findViewById(R.id.tvProductoStockScan);
        
        MaterialButton btnRestar = view.findViewById(R.id.btnRestar);
        MaterialButton btnSumar = view.findViewById(R.id.btnSumar);
        TextView tvCantidadVenta = view.findViewById(R.id.tvCantidadVenta);
        MaterialButton btnConfirmarVenta = view.findViewById(R.id.btnConfirmarVenta);
        
        tvProductoNombreScan.setText(producto.getNombre());
        tvProductoCodigoScan.setText("EAN/UPC: " + codigoBarras);
        tvProductoPrecioScan.setText("S/ " + String.format("%.2f", producto.getPrecio()));
        tvProductoStockScan.setText(producto.getStockActual() + " unds");
        
        final int[] cantidad = {1};
        
        btnRestar.setOnClickListener(v -> {
            if (cantidad[0] > 1) {
                cantidad[0]--;
                tvCantidadVenta.setText(String.valueOf(cantidad[0]));
            }
        });
        
        btnSumar.setOnClickListener(v -> {
            if (cantidad[0] < producto.getStockActual()) {
                cantidad[0]++;
                tvCantidadVenta.setText(String.valueOf(cantidad[0]));
            } else {
                Toast.makeText(getContext(), "No hay stock suficiente", Toast.LENGTH_SHORT).show();
            }
        });
        
        if (producto.getId() > 0) {
            ApiService apiService = RetrofitClient.createService();
            apiService.obtenerFotoProducto(producto.getId()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        ivProductoScan.post(() -> ivProductoScan.setImageBitmap(bmp));
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {}
            });
        }
        
        btnConfirmarVenta.setOnClickListener(v -> {
            btnConfirmarVenta.setEnabled(false);
            ApiService apiService = RetrofitClient.createService();
            RegistrarVentaRequest request = new RegistrarVentaRequest(codigoBarras, cantidad[0]);
            
            apiService.registrarVenta(request).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                    if (!isAdded()) return;
                    btnConfirmarVenta.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Venta registrada exitosamente", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        cargarDashboard(); // Actualizar dashboard local
                    } else {
                        Toast.makeText(getContext(), "Error al registrar la venta", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                    if (!isAdded()) return;
                    btnConfirmarVenta.setEnabled(true);
                    Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        bottomSheetDialog.show();
    }

    private void cargarDashboard() {
        String token = SplashStorage.obtenerToken(requireContext());
        if (token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerDashboardImpulsador().enqueue(new Callback<ApiResponse<ImpulsadorDashboard>>() {
            @Override
            public void onResponse(Call<ApiResponse<ImpulsadorDashboard>> call, Response<ApiResponse<ImpulsadorDashboard>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ImpulsadorDashboard data = response.body().getData();
                    
                    if (data.getPuntoVenta() != null && data.getPuntoVenta().getNombre() != null) {
                        tvLocalAsignado.setText(data.getPuntoVenta().getNombre());
                    } else {
                        tvLocalAsignado.setText("Sin local asignado");
                    }

                    if (data.getHorario() != null && data.getHorario().getJornada() != null) {
                        tvHorario.setText("Horario: " + data.getHorario().getJornada());
                    } else {
                        tvHorario.setText("Horario: ---");
                    }

                    if (data.getProgresoVentas() != null) {
                        tvMetaVentas.setText(String.format("S/ %.2f", data.getProgresoVentas().getMetaMensual()));
                        tvAvanceVentas.setText(String.format("S/ %.2f", data.getProgresoVentas().getMontoMes()));

                        int porcentaje = (int) data.getProgresoVentas().getPorcentajeAlcanzado();
                        pbVentasImpulsador.setProgress(porcentaje);
                    } else if (data.getImpulsador() != null) {
                        tvMetaVentas.setText(String.format("S/ %.2f", data.getImpulsador().getMetaVentas()));
                        tvAvanceVentas.setText("S/ 0.00");
                        pbVentasImpulsador.setProgress(0);
                    }
                } else {
                    android.util.Log.e("ImpulsadorDashboard", "Error servidor HTTP " + response.code());
                    tvLocalAsignado.setText("Sin asignación activa");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ImpulsadorDashboard>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error al cargar dashboard: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}