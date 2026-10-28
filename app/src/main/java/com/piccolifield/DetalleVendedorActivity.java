package com.piccolifield; // Asegúrate de que el paquete sea el correcto

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import okhttp3.ResponseBody;

import androidx.appcompat.app.AppCompatActivity;

import com.piccolifield.data.model.VendedorDetalle;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.dto.RetrofitClient; // Cambia esto si tu RetrofitClient está en otra carpeta
import com.piccolifield.sharedpreferences.SplashStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleVendedorActivity extends AppCompatActivity {

    private ImageView btnBack, ivPerfilVendedor;
    private TextView tvNombreVendedor, tvPuntoVenta, tvMontoTotal, tvProgresoAsignacion, tvCantidades;
    private ProgressBar progressBarAsignacion, pbLoading;
    private androidx.core.widget.NestedScrollView scrollView;

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

        btnBack.setOnClickListener(v -> finish());
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
}