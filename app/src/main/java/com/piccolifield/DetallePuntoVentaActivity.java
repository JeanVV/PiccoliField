package com.piccolifield;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class DetallePuntoVentaActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvNombreTienda, tvCantExhibida, tvCantAlmacen, tvCantSolicitada, tvCantProductos;
    private MaterialButton btnOtorgarComision;

    // Variables para recibir desde el Adapter
    private int idPuntoVenta;
    private String nombrePuntoVenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_punto_venta);

        // 1. Vincular Vistas
        btnBack = findViewById(R.id.btnBack);
        tvNombreTienda = findViewById(R.id.tvNombreTienda);
        tvCantExhibida = findViewById(R.id.tvCantExhibida);
        tvCantAlmacen = findViewById(R.id.tvCantAlmacen);
        tvCantSolicitada = findViewById(R.id.tvCantSolicitada);
        tvCantProductos = findViewById(R.id.tvCantProductos);
        btnOtorgarComision = findViewById(R.id.btnOtorgarComision);

        // 2. Recibir los datos enviados por PuntoVentaAdapter
        if (getIntent() != null) {
            idPuntoVenta = getIntent().getIntExtra("ID_PDV", -1);
            nombrePuntoVenta = getIntent().getStringExtra("NOMBRE_PDV");

            // Asignar el título de la tienda recibido
            if (nombrePuntoVenta != null) {
                tvNombreTienda.setText(nombrePuntoVenta);
            }
        }

        // 3. Evento del botón de atrás
        btnBack.setOnClickListener(v -> {
            finish(); // Cierra esta pantalla y regresa a la anterior
        });

        // 4. Evento del botón Otorgar Comisión
        btnOtorgarComision.setOnClickListener(v -> {
            Toast.makeText(this, "Otorgando comisión al local: " + nombrePuntoVenta, Toast.LENGTH_SHORT).show();
            // Aquí iría tu lógica (ej. Abrir un Dialog o mandar POST por Retrofit)
        });

        // 5. Cargar detalles desde la Base de Datos
        // Aquí deberás llamar a tu RetrofitClient pasándole el 'idPuntoVenta'
        // para traer sus ventas, pedidos (tabla Pedido) y stock (STOCK_REGISTROS).
        cargarMeticasDesdeBD(idPuntoVenta);
    }

    private void cargarMeticasDesdeBD(int idPdv) {
        // Ejemplo: apiService.obtenerDetallePDV(idPdv).enqueue(...)
        // Por ahora, solo es una simulación visual
    }
}