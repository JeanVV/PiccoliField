package com.piccolifield; // Asegúrate de que el paquete sea el correcto

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.piccolifield.adapter.RendimientoMensualAdapter;
import com.piccolifield.adapter.VendedorAsignacionAdapter;
import com.piccolifield.data.model.PuntoVentaHistorial;
import com.piccolifield.data.model.RendimientoMensualItem;
import com.piccolifield.data.model.VendedorDetalle;
import com.piccolifield.data.model.VendedorRendimientoHistoricoResponse;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.dto.request.ReasignarVendedorRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.SupervisionPuntoVenta;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleVendedorActivity extends AppCompatActivity {

    private ImageView btnBack, ivPerfilVendedor;
    private TextView tvNombreVendedor, tvPuntoVenta, tvMontoTotal, tvProgresoAsignacion, tvCantidades;
    private ProgressBar progressBarAsignacion, pbLoading;
    private androidx.core.widget.NestedScrollView scrollView;
    private MaterialButton btnHistorialRendimiento, btnReasignar;

    private int idUsuarioSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_vendedor);

        // 1. Vincular los IDs EXACTOS de tu XML
        btnBack = findViewById(R.id.btnBack);
        ivPerfilVendedor = findViewById(R.id.ivPerfilVendedor);
        tvNombreVendedor = findViewById(R.id.tvNombreVendedor);
        tvPuntoVenta = findViewById(R.id.tvPuntoVenta);
        tvMontoTotal = findViewById(R.id.tvMontoTotal);

        // Aquí corregimos los IDs por los que usaste en el XML
        tvProgresoAsignacion = findViewById(R.id.tvPorcentaje);
        progressBarAsignacion = findViewById(R.id.pbVentas);
        tvCantidades = findViewById(R.id.tvCantidades);
        pbLoading = findViewById(R.id.pbLoading);
        scrollView = findViewById(R.id.scrollView);
        btnHistorialRendimiento = findViewById(R.id.btnVerEvidencias);
        btnReasignar = findViewById(R.id.btnReasignar);

        btnBack.setOnClickListener(v -> finish());

        if (btnHistorialRendimiento != null) {
            btnHistorialRendimiento.setOnClickListener(v -> mostrarDialogoHistorialRendimiento());
        }

        if (btnReasignar != null) {
            btnReasignar.setOnClickListener(v -> mostrarDialogoReasignarVendedor());
        }
        idUsuarioSeleccionado = getIntent().getIntExtra("ID_VENDEDOR", -1);

        if (idUsuarioSeleccionado == -1) {
            Toast.makeText(this, getString(R.string.error_cargar_datos), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        cargarDatosVendedor();
    }

    private void cargarDatosVendedor() {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (scrollView != null) scrollView.setVisibility(View.GONE);
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = SplashStorage.obtenerToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.API_TOKEN = token;
        ApiService apiService = RetrofitClient.createService();

        // 4. Llamar a la API SIN el Bearer (porque ya lo hace tu Interceptor)
        apiService.obtenerDetalleVendedor(idUsuarioSeleccionado).enqueue(new Callback<ApiResponse<VendedorDetalle>>() {
            @Override
            public void onResponse(Call<ApiResponse<VendedorDetalle>> call, Response<ApiResponse<VendedorDetalle>> response) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                if (scrollView != null) scrollView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {

                    VendedorDetalle vendedor = response.body().getData();

                    if (vendedor != null) {
                        tvNombreVendedor.setText(vendedor.getNombre());
                        tvPuntoVenta.setText(getString(R.string.label_pin_local, vendedor.getLocal_pertenece()));
                        tvMontoTotal.setText(getString(R.string.label_moneda_soles, String.format("%.2f", vendedor.getMonto_vendido())));

                        // Si la API no trae cantidadVendida o meta, usamos las que pasamos por el Intent
                        int cantidadVendida = (int) vendedor.getCantidadVendida();
                        if (cantidadVendida == 0) {
                            cantidadVendida = (int) getIntent().getDoubleExtra("CANTIDAD_VENDIDA", 0);
                        }
                        
                        int meta = vendedor.getMeta();
                        if (meta == 0) {
                            meta = getIntent().getIntExtra("META_ASIGNADA", 1);
                        }

                        if (meta == 0) {
                            meta = 1; // Para evitar división por cero si aún sigue en 0
                        }

                        // Calculamos manualmente el porcentaje para que coincida exactamente con los datos de cantidad / meta
                        double porcentajeDouble = ((double) cantidadVendida / meta) * 100;
                        int porcentajeInt = (int) Math.round(porcentajeDouble);

                        // Modificamos el texto para que diga el % en el formato de tu diseño
                        tvProgresoAsignacion.setText(porcentajeInt + "%");
                        progressBarAsignacion.setProgress(porcentajeInt);
                        tvCantidades.setText(getString(R.string.label_vendidos, cantidadVendida, meta));
                        
                        apiService.obtenerFotoImpulsador(idUsuarioSeleccionado).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                                    ivPerfilVendedor.post(() -> ivPerfilVendedor.setImageBitmap(bmp));
                                } else {
                                    ivPerfilVendedor.setImageResource(R.drawable.ic_avatar_placeholder);
                                }
                            }
                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ivPerfilVendedor.setImageResource(R.drawable.ic_avatar_placeholder);
                            }
                        });
                    }

                } else {
                    Toast.makeText(DetalleVendedorActivity.this, getString(R.string.error_cargar_datos), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<VendedorDetalle>> call, Throwable t) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                Toast.makeText(DetalleVendedorActivity.this, getString(R.string.error_falla_red, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoHistorialRendimiento() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet_historial_vendedor, null);
        bottomSheetDialog.setContentView(dialogView);

        ProgressBar pbLoading = dialogView.findViewById(R.id.pbLoadingHistorialVendedor);
        TextView tvEmptyRendimiento = dialogView.findViewById(R.id.tvEmptyRendimiento);
        TextView tvEmptyAsignaciones = dialogView.findViewById(R.id.tvEmptyAsignaciones);
        RecyclerView rvRendimientoMensual = dialogView.findViewById(R.id.rvRendimientoMensual);
        RecyclerView rvHistorialAsignaciones = dialogView.findViewById(R.id.rvHistorialAsignacionesVendedor);

        rvRendimientoMensual.setLayoutManager(new LinearLayoutManager(this));
        rvHistorialAsignaciones.setLayoutManager(new LinearLayoutManager(this));

        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerRendimientoHistoricoVendedor(idUsuarioSeleccionado, null).enqueue(new Callback<ApiResponse<VendedorRendimientoHistoricoResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<VendedorRendimientoHistoricoResponse>> call, @NonNull Response<ApiResponse<VendedorRendimientoHistoricoResponse>> response) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    VendedorRendimientoHistoricoResponse data = response.body().getData();

                    // 1. Cargar Desempeño Mensual
                    List<RendimientoMensualItem> mensualItems = data.getDesempenoMensual();
                    if (mensualItems == null || mensualItems.isEmpty()) {
                        if (tvEmptyRendimiento != null) tvEmptyRendimiento.setVisibility(View.VISIBLE);
                        rvRendimientoMensual.setVisibility(View.GONE);
                    } else {
                        if (tvEmptyRendimiento != null) tvEmptyRendimiento.setVisibility(View.GONE);
                        rvRendimientoMensual.setVisibility(View.VISIBLE);
                        RendimientoMensualAdapter adapter = new RendimientoMensualAdapter(mensualItems, DetalleVendedorActivity.this);
                        rvRendimientoMensual.setAdapter(adapter);
                    }

                    // 2. Cargar Asignaciones Previas
                    List<PuntoVentaHistorial> asignacionesItems = data.getHistorialAsignaciones();
                    if (asignacionesItems == null || asignacionesItems.isEmpty()) {
                        if (tvEmptyAsignaciones != null) tvEmptyAsignaciones.setVisibility(View.VISIBLE);
                        rvHistorialAsignaciones.setVisibility(View.GONE);
                    } else {
                        if (tvEmptyAsignaciones != null) tvEmptyAsignaciones.setVisibility(View.GONE);
                        rvHistorialAsignaciones.setVisibility(View.VISIBLE);
                        VendedorAsignacionAdapter adapter = new VendedorAsignacionAdapter(asignacionesItems, DetalleVendedorActivity.this);
                        rvHistorialAsignaciones.setAdapter(adapter);
                    }

                } else {
                    if (tvEmptyRendimiento != null) tvEmptyRendimiento.setVisibility(View.VISIBLE);
                    if (tvEmptyAsignaciones != null) tvEmptyAsignaciones.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<VendedorRendimientoHistoricoResponse>> call, @NonNull Throwable t) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                if (tvEmptyRendimiento != null) tvEmptyRendimiento.setVisibility(View.VISIBLE);
                if (tvEmptyAsignaciones != null) tvEmptyAsignaciones.setVisibility(View.VISIBLE);
            }
        });

        bottomSheetDialog.show();
    }

    private void mostrarDialogoReasignarVendedor() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet_reasignar_vendedor, null);
        bottomSheetDialog.setContentView(dialogView);

        Spinner spPuntosVenta = dialogView.findViewById(R.id.spPuntosVentaDestino);
        ProgressBar pbLoading = dialogView.findViewById(R.id.pbLoadingPuntosVenta);
        EditText etMotivo = dialogView.findViewById(R.id.etMotivoReasignacionVendedor);
        MaterialButton btnConfirmar = dialogView.findViewById(R.id.btnConfirmarReasignarVendedor);

        List<SupervisionPuntoVenta> listaPdvs = new ArrayList<>();
        List<String> nombresPdvs = new ArrayList<>();

        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (spPuntosVenta != null) spPuntosVenta.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.createService();
        apiService.listarPuntosVentaSupervision("ACTIVO").enqueue(new Callback<ApiResponse<List<SupervisionPuntoVenta>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<SupervisionPuntoVenta>>> call, @NonNull Response<ApiResponse<List<SupervisionPuntoVenta>>> response) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                if (spPuntosVenta != null) spPuntosVenta.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaPdvs.addAll(response.body().getData());
                    for (SupervisionPuntoVenta pdv : listaPdvs) {
                        nombresPdvs.add(pdv.getNombre() + " (" + (pdv.getDireccion() != null ? pdv.getDireccion() : "") + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(DetalleVendedorActivity.this, android.R.layout.simple_spinner_item, nombresPdvs);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spPuntosVenta.setAdapter(adapter);
                } else {
                    Toast.makeText(DetalleVendedorActivity.this, "No se pudieron obtener los puntos de venta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SupervisionPuntoVenta>>> call, @NonNull Throwable t) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                Toast.makeText(DetalleVendedorActivity.this, "Error de red al cargar puntos de venta", Toast.LENGTH_SHORT).show();
            }
        });

        btnConfirmar.setOnClickListener(v -> {
            int selectedIndex = spPuntosVenta.getSelectedItemPosition();
            if (selectedIndex < 0 || selectedIndex >= listaPdvs.size()) {
                Toast.makeText(DetalleVendedorActivity.this, "Seleccione un punto de venta válido", Toast.LENGTH_SHORT).show();
                return;
            }

            String motivo = etMotivo.getText() != null ? etMotivo.getText().toString().trim() : "";
            if (motivo.isEmpty()) {
                etMotivo.setError("Ingrese un motivo");
                return;
            }

            SupervisionPuntoVenta pdvSeleccionado = listaPdvs.get(selectedIndex);

            ReasignarVendedorRequest request = new ReasignarVendedorRequest(pdvSeleccionado.getId(), motivo);

            btnConfirmar.setEnabled(false);
            apiService.reasignarVendedor(idUsuarioSeleccionado, request).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                    btnConfirmar.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(DetalleVendedorActivity.this, "Vendedor reasignado exitosamente", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        cargarDatosVendedor(); // Recargar el detalle
                    } else {
                        Toast.makeText(DetalleVendedorActivity.this, "Error al reasignar vendedor", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                    btnConfirmar.setEnabled(true);
                    Toast.makeText(DetalleVendedorActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        bottomSheetDialog.show();
    }
}