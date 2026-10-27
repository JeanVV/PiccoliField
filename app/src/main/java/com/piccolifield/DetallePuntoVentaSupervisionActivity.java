package com.piccolifield;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import okhttp3.ResponseBody;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.piccolifield.adapter.ProductoSupervisionAdapter;
import com.piccolifield.adapter.PuntoVentaHistorialAdapter;
import com.piccolifield.data.model.Impulsador;
import com.piccolifield.data.model.PuntoVentaDetalle;
import com.piccolifield.data.model.PuntoVentaHistorial;
import com.piccolifield.data.model.PuntoVentaProducto;
import com.piccolifield.data.model.ReasignarImpulsadorResponse;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.request.ReasignarImpulsadorRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallePuntoVentaSupervisionActivity extends AppCompatActivity {

    private ImageView btnBack, ivImpulsadorFoto;
    private TextView tvNombrePuntoVenta, tvDireccionPuntoVenta, tvDistritoProvincia;
    private TextView tvImpulsadorNombre, tvImpulsadorCorreo;
    private MaterialButton btnReasignar, btnHistorial;
    private RecyclerView rvProductosSupervision;
    private ProgressBar pbLoading;
    private androidx.core.widget.NestedScrollView scrollView;

    private int idPuntoVenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_punto_venta_supervision);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.color_fondo));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.color_fondo));

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btnBack);
        ivImpulsadorFoto = findViewById(R.id.ivImpulsadorFoto);
        tvNombrePuntoVenta = findViewById(R.id.tvNombrePuntoVenta);
        tvDireccionPuntoVenta = findViewById(R.id.tvDireccionPuntoVenta);
        tvDistritoProvincia = findViewById(R.id.tvDistritoProvincia);
        tvImpulsadorNombre = findViewById(R.id.tvImpulsadorNombre);
        tvImpulsadorCorreo = findViewById(R.id.tvImpulsadorCorreo);
        btnReasignar = findViewById(R.id.btnReasignar);
        btnHistorial = findViewById(R.id.btnHistorial);
        pbLoading = findViewById(R.id.pbLoading);
        scrollView = findViewById(R.id.scrollView);
        rvProductosSupervision = findViewById(R.id.rvProductosSupervision);
        rvProductosSupervision.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        idPuntoVenta = getIntent().getIntExtra("ID_PDV", -1);
        
        if (idPuntoVenta == -1) {
            Toast.makeText(this, "Punto de Venta no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnReasignar.setOnClickListener(v -> mostrarDialogoReasignar());
        
        btnHistorial.setOnClickListener(v -> mostrarDialogoHistorial());

        cargarDetallePuntoVenta();
    }

    private void cargarDetallePuntoVenta() {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (scrollView != null) scrollView.setVisibility(View.GONE);
        String token = SplashStorage.obtenerToken(this);
        if (token == null || token.isEmpty()) return;

        RetrofitClient.API_TOKEN = token;
        ApiService apiService = RetrofitClient.createService();

        apiService.obtenerDetallePuntoVentaSupervision(idPuntoVenta).enqueue(new Callback<ApiResponse<PuntoVentaDetalle>>() {
            @Override
            public void onResponse(Call<ApiResponse<PuntoVentaDetalle>> call, Response<ApiResponse<PuntoVentaDetalle>> response) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                if (scrollView != null) scrollView.setVisibility(View.VISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    PuntoVentaDetalle detalle = response.body().getData();
                    if (detalle != null) {
                        tvNombrePuntoVenta.setText(detalle.getPuntoVenta());
                        tvDireccionPuntoVenta.setText(detalle.getDireccion());
                        tvDistritoProvincia.setText(detalle.getDistrito() + " - " + detalle.getProvincia());
                        
                        if (detalle.getImpulsadorNombre() != null && !detalle.getImpulsadorNombre().isEmpty()) {
                            tvImpulsadorNombre.setText(detalle.getImpulsadorNombre());
                            tvImpulsadorCorreo.setText(detalle.getImpulsadorCorreo());
                            
                            if (detalle.getIdImpulsador() != null && detalle.getIdImpulsador() > 0) {
                                apiService.obtenerFotoImpulsador(detalle.getIdImpulsador()).enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                                            ivImpulsadorFoto.post(() -> ivImpulsadorFoto.setImageBitmap(bmp));
                                        } else {
                                            ivImpulsadorFoto.setImageResource(R.drawable.ic_avatar_placeholder);
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        ivImpulsadorFoto.setImageResource(R.drawable.ic_avatar_placeholder);
                                    }
                                });
                            }
                        } else {
                            tvImpulsadorNombre.setText("Sin Asignar");
                            tvImpulsadorCorreo.setText("---");
                            ivImpulsadorFoto.setImageResource(R.drawable.ic_avatar_placeholder);
                        }

                        List<PuntoVentaProducto> productos = detalle.getProductos();
                        if (productos != null && !productos.isEmpty()) {
                            ProductoSupervisionAdapter adapter = new ProductoSupervisionAdapter(productos, DetallePuntoVentaSupervisionActivity.this);
                            rvProductosSupervision.setAdapter(adapter);
                        }
                    }
                } else {
                    Toast.makeText(DetallePuntoVentaSupervisionActivity.this, "Error al cargar detalle", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PuntoVentaDetalle>> call, Throwable t) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                Toast.makeText(DetallePuntoVentaSupervisionActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoHistorial() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet_historial, null);
        bottomSheetDialog.setContentView(dialogView);

        ProgressBar pbLoading = dialogView.findViewById(R.id.pbLoadingHistorial);
        TextView tvVacio = dialogView.findViewById(R.id.tvHistorialVacio);
        RecyclerView rvHistorial = dialogView.findViewById(R.id.rvHistorial);
        rvHistorial.setLayoutManager(new LinearLayoutManager(this));

        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerHistorialPuntoVenta(idPuntoVenta).enqueue(new Callback<ApiResponse<List<PuntoVentaHistorial>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PuntoVentaHistorial>>> call, Response<ApiResponse<List<PuntoVentaHistorial>>> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<PuntoVentaHistorial> lista = response.body().getData();
                    if (lista.isEmpty()) {
                        tvVacio.setVisibility(View.VISIBLE);
                    } else {
                        rvHistorial.setVisibility(View.VISIBLE);
                        PuntoVentaHistorialAdapter adapter = new PuntoVentaHistorialAdapter(lista, DetallePuntoVentaSupervisionActivity.this);
                        rvHistorial.setAdapter(adapter);
                    }
                } else {
                    tvVacio.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PuntoVentaHistorial>>> call, Throwable t) {
                pbLoading.setVisibility(View.GONE);
                tvVacio.setVisibility(View.VISIBLE);
            }
        });

        bottomSheetDialog.show();
    }

    private void mostrarDialogoReasignar() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet_reasignar, null);
        bottomSheetDialog.setContentView(dialogView);

        Spinner spImpulsadores = dialogView.findViewById(R.id.spImpulsadores);
        ProgressBar pbLoading = dialogView.findViewById(R.id.pbLoadingImpulsadores);
        EditText etMotivo = dialogView.findViewById(R.id.etMotivo);
        MaterialButton btnConfirmar = dialogView.findViewById(R.id.btnConfirmarReasignar);

        List<Impulsador> listaImpulsadores = new ArrayList<>();
        List<String> nombresImpulsadores = new ArrayList<>();

        pbLoading.setVisibility(View.VISIBLE);
        spImpulsadores.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.createService();
        apiService.listarImpulsadores().enqueue(new Callback<ApiResponse<List<Impulsador>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Impulsador>>> call, Response<ApiResponse<List<Impulsador>>> response) {
                pbLoading.setVisibility(View.GONE);
                spImpulsadores.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaImpulsadores.addAll(response.body().getData());
                    for (Impulsador imp : listaImpulsadores) {
                        nombresImpulsadores.add(imp.getNombreCompleto() + " (" + (imp.getLocalActual() != null ? imp.getLocalActual() : "Sin Local") + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(DetallePuntoVentaSupervisionActivity.this, android.R.layout.simple_spinner_item, nombresImpulsadores);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spImpulsadores.setAdapter(adapter);
                } else {
                    Toast.makeText(DetallePuntoVentaSupervisionActivity.this, "No se pudieron obtener impulsadores", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Impulsador>>> call, Throwable t) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(DetallePuntoVentaSupervisionActivity.this, "Error de red al cargar impulsadores", Toast.LENGTH_SHORT).show();
            }
        });

        btnConfirmar.setOnClickListener(v -> {
            int selectedIndex = spImpulsadores.getSelectedItemPosition();
            if (selectedIndex < 0 || selectedIndex >= listaImpulsadores.size()) {
                Toast.makeText(DetallePuntoVentaSupervisionActivity.this, "Seleccione un impulsador válido", Toast.LENGTH_SHORT).show();
                return;
            }

            String motivo = etMotivo.getText().toString().trim();
            if (motivo.isEmpty()) {
                etMotivo.setError("Ingrese un motivo");
                return;
            }

            Impulsador impulsadorSeleccionado = listaImpulsadores.get(selectedIndex);

            ReasignarImpulsadorRequest request = new ReasignarImpulsadorRequest(impulsadorSeleccionado.getIdUsuario(), idPuntoVenta, motivo);

            btnConfirmar.setEnabled(false);
            apiService.reasignarImpulsador(request).enqueue(new Callback<ApiResponse<ReasignarImpulsadorResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<ReasignarImpulsadorResponse>> call, Response<ApiResponse<ReasignarImpulsadorResponse>> response) {
                    btnConfirmar.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        Toast.makeText(DetallePuntoVentaSupervisionActivity.this, "Impulsador reasignado exitosamente", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        cargarDetallePuntoVenta(); // Recargar el detalle
                    } else {
                        Toast.makeText(DetallePuntoVentaSupervisionActivity.this, "Error al reasignar impulsador", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ReasignarImpulsadorResponse>> call, Throwable t) {
                    btnConfirmar.setEnabled(true);
                    Toast.makeText(DetallePuntoVentaSupervisionActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        bottomSheetDialog.show();
    }
}