package com.piccolifield;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import okhttp3.ResponseBody;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.piccolifield.data.model.AlertaDetalle;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.request.ResolverAlertaRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.sharedpreferences.SplashStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleAlertaActivity extends AppCompatActivity {

    private ImageView btnBack, ivProducto, ivResponsable;
    private TextView tvEstadoAlerta, tvNombreLocal, tvDireccionLocal;
    private TextView tvNivelAlerta, tvProductoNombre, tvStockActual, tvStockMinimo;
    private TextView tvResponsableNombre, tvResponsableRol, tvMensajeResolucion, tvComentarioResolucion;
    private MaterialButton btnResolverAlerta, btnIgnorarAlerta;
    private View layoutAccionesResolucion;
    private ProgressBar pbLoading;
    private androidx.core.widget.NestedScrollView scrollView;

    private int idAlertaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_alerta);

        // 1. Vincular los IDs
        btnBack = findViewById(R.id.btnBack);
        ivProducto = findViewById(R.id.ivProductoAlerta);
        ivResponsable = findViewById(R.id.ivResponsable);
        tvEstadoAlerta = findViewById(R.id.tvEstadoAlerta);
        tvNombreLocal = findViewById(R.id.tvNombreLocal);
        tvDireccionLocal = findViewById(R.id.tvDireccionLocal);
        tvNivelAlerta = findViewById(R.id.tvNivelAlerta);
        tvProductoNombre = findViewById(R.id.tvProductoNombre);
        tvStockActual = findViewById(R.id.tvStockActual);
        tvStockMinimo = findViewById(R.id.tvStockMinimo);
        tvResponsableNombre = findViewById(R.id.tvResponsableNombre);
        tvResponsableRol = findViewById(R.id.tvResponsableRol);
        tvMensajeResolucion = findViewById(R.id.tvMensajeResolucion);
        tvComentarioResolucion = findViewById(R.id.tvComentarioResolucion);
        btnResolverAlerta = findViewById(R.id.btnResolverAlerta);
        btnIgnorarAlerta = findViewById(R.id.btnIgnorarAlerta);
        layoutAccionesResolucion = findViewById(R.id.layoutAccionesResolucion);
        pbLoading = findViewById(R.id.pbLoading);
        scrollView = findViewById(R.id.scrollView);

        // Botón de retroceso
        btnBack.setOnClickListener(v -> finish());

        if (btnResolverAlerta != null) {
            btnResolverAlerta.setOnClickListener(v -> mostrarDialogoComentario("ATENDER"));
        }
        if (btnIgnorarAlerta != null) {
            btnIgnorarAlerta.setOnClickListener(v -> mostrarDialogoComentario("IGNORAR"));
        }

        // 2. Recibir el ID de la alerta
        idAlertaSeleccionada = getIntent().getIntExtra("ID_ALERTA", -1);

        if (idAlertaSeleccionada == -1) {
            Toast.makeText(this, getString(R.string.error_cargar_alerta), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Llamamos al backend
        cargarDatosAlerta();
    }

    private void cargarDatosAlerta() {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (scrollView != null) scrollView.setVisibility(View.GONE);
        String token = SplashStorage.obtenerToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.API_TOKEN = token;
        ApiService apiService = RetrofitClient.createService();

        apiService.obtenerDetalleAlerta(idAlertaSeleccionada).enqueue(new Callback<ApiResponse<AlertaDetalle>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AlertaDetalle>> call, @NonNull Response<ApiResponse<AlertaDetalle>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                if (scrollView != null) scrollView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    AlertaDetalle alerta = response.body().getData();

                    String estado = alerta.getEstadoResolucion() != null ? alerta.getEstadoResolucion() : "PENDIENTE";
                    String nivel = alerta.getNivelAlerta() != null ? alerta.getNivelAlerta() : "ALTO";
                    String local = alerta.getLocalNombre() != null ? alerta.getLocalNombre() : "Sin local";
                    String direccion = alerta.getLocalDireccion() != null ? alerta.getLocalDireccion() : "";
                    String producto = alerta.getNombreProducto() != null ? alerta.getNombreProducto() : "Sin producto";
                    String responsable = alerta.getResponsableNombre() != null ? alerta.getResponsableNombre() : "Sin responsable";
                    String rol = alerta.getResponsableRol() != null ? alerta.getResponsableRol() : "IMPULSADOR";

                    if (tvEstadoAlerta != null) tvEstadoAlerta.setText(getString(R.string.label_estado_alerta, estado));
                    if (tvNivelAlerta != null) tvNivelAlerta.setText(getString(R.string.label_nivel_alerta, nivel));

                    // Mostrar Comentario / Resolución si existe
                    if (alerta.getComentario() != null && !alerta.getComentario().isEmpty()) {
                        if (tvComentarioResolucion != null) {
                            String fechaAtencionStr = alerta.getFechaAtencion() != null ? " (" + alerta.getFechaAtencion() + " " + (alerta.getHoraAtencion() != null ? alerta.getHoraAtencion() : "") + ")" : "";
                            tvComentarioResolucion.setText("Comentario / Resolución: " + alerta.getComentario() + fechaAtencionStr);
                            tvComentarioResolucion.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (tvComentarioResolucion != null) tvComentarioResolucion.setVisibility(View.GONE);
                    }

                    if ("ATENDIDA".equalsIgnoreCase(estado) || "RESUELTA".equalsIgnoreCase(estado) || "IGNORADA".equalsIgnoreCase(estado)) {
                        if (layoutAccionesResolucion != null) layoutAccionesResolucion.setVisibility(View.GONE);
                        if (tvMensajeResolucion != null) tvMensajeResolucion.setText("Esta alerta ya ha sido procesada.");
                        if (tvEstadoAlerta != null) {
                            tvEstadoAlerta.setText(estado);
                            tvEstadoAlerta.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#388E3C")));
                        }
                    } else {
                        if (layoutAccionesResolucion != null) layoutAccionesResolucion.setVisibility(View.VISIBLE);
                        if (tvMensajeResolucion != null) tvMensajeResolucion.setText(getString(R.string.msg_resolucion_pendiente));
                        if (tvEstadoAlerta != null) {
                            tvEstadoAlerta.setText("PENDIENTE");
                            tvEstadoAlerta.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D32F2F")));
                        }
                    }

                    if (tvNombreLocal != null) tvNombreLocal.setText(local);
                    if (tvDireccionLocal != null) tvDireccionLocal.setText(direccion);

                    if (tvProductoNombre != null) tvProductoNombre.setText(getString(R.string.label_producto_alerta, producto));

                    if (tvStockActual != null) {
                        tvStockActual.setText(String.valueOf(alerta.getStockActual()));
                    }
                    
                    if (tvStockMinimo != null) {
                        tvStockMinimo.setText(String.valueOf(alerta.getStockMinimo()));
                    }

                    if (tvResponsableNombre != null) tvResponsableNombre.setText(getString(R.string.label_responsable_alerta, responsable));
                    if (tvResponsableRol != null) tvResponsableRol.setText(getString(R.string.label_rol_alerta, rol));

                    if (alerta.getIdProductos() > 0 && ivProducto != null) {
                        apiService.obtenerFotoProducto(alerta.getIdProductos()).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                if (isFinishing() || isDestroyed()) return;
                                if (response.isSuccessful() && response.body() != null) {
                                    try {
                                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                                        if (bmp != null) {
                                            ivProducto.post(() -> ivProducto.setImageBitmap(bmp));
                                        } else {
                                            ivProducto.setImageResource(R.drawable.ic_avatar_placeholder);
                                        }
                                    } catch (Exception e) {
                                        ivProducto.setImageResource(R.drawable.ic_avatar_placeholder);
                                    }
                                } else {
                                    ivProducto.setImageResource(R.drawable.ic_avatar_placeholder);
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                if (isFinishing() || isDestroyed()) return;
                                ivProducto.setImageResource(R.drawable.ic_avatar_placeholder);
                            }
                        });
                    }

                    if (alerta.getResponsableId() > 0 && ivResponsable != null) {
                        apiService.obtenerFotoImpulsador(alerta.getResponsableId()).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                if (isFinishing() || isDestroyed()) return;
                                if (response.isSuccessful() && response.body() != null) {
                                    try {
                                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                                        if (bmp != null) {
                                            ivResponsable.post(() -> ivResponsable.setImageBitmap(bmp));
                                        } else {
                                            ivResponsable.setImageResource(R.drawable.ic_avatar_placeholder);
                                        }
                                    } catch (Exception e) {
                                        ivResponsable.setImageResource(R.drawable.ic_avatar_placeholder);
                                    }
                                } else {
                                    ivResponsable.setImageResource(R.drawable.ic_avatar_placeholder);
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                if (isFinishing() || isDestroyed()) return;
                                ivResponsable.setImageResource(R.drawable.ic_avatar_placeholder);
                            }
                        });
                    }
                } else {
                    Toast.makeText(DetalleAlertaActivity.this, getString(R.string.error_cargar_alerta), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AlertaDetalle>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                Toast.makeText(DetalleAlertaActivity.this, getString(R.string.error_falla_red, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoComentario(String accion) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_comentario_alerta, null);
        bottomSheetDialog.setContentView(dialogView);

        TextView tvTitulo = dialogView.findViewById(R.id.tvTituloDialogoAlerta);
        EditText etComentario = dialogView.findViewById(R.id.etComentarioAlerta);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarAlertaDialog);
        MaterialButton btnConfirmar = dialogView.findViewById(R.id.btnConfirmarAlertaDialog);

        if (tvTitulo != null) {
            tvTitulo.setText(accion.equals("ATENDER") ? "Atender Alerta" : "Ignorar Alerta");
        }

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> {
                String textoComentario = etComentario != null && etComentario.getText() != null ? etComentario.getText().toString().trim() : "";
                if (textoComentario.isEmpty()) {
                    textoComentario = accion.equals("ATENDER") ? "Atendido por supervisor" : "Ignorado por supervisor";
                }
                bottomSheetDialog.dismiss();
                ejecutarAccionAlerta(accion, textoComentario);
            });
        }

        bottomSheetDialog.show();
    }

    private void ejecutarAccionAlerta(String accion, String comentario) {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        String token = SplashStorage.obtenerToken(this);
        if (token == null || token.isEmpty()) return;

        RetrofitClient.API_TOKEN = token;
        ApiService apiService = RetrofitClient.createService();
        ResolverAlertaRequest request = new ResolverAlertaRequest(comentario);

        Call<ApiResponse<Object>> call;
        if (accion.equals("ATENDER")) {
            call = apiService.atenderAlerta(idAlertaSeleccionada, request);
        } else {
            call = apiService.ignorarAlerta(idAlertaSeleccionada, request);
        }

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(DetalleAlertaActivity.this, "Alerta procesada exitosamente", Toast.LENGTH_SHORT).show();
                    cargarDatosAlerta();
                } else {
                    Toast.makeText(DetalleAlertaActivity.this, "Error al procesar alerta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                Toast.makeText(DetalleAlertaActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}