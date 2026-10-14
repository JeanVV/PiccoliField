package com.piccolifield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;

public class DetallePuntoVentaActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvBadgeNumero, tvNombreTienda, tvEstadoBadge, tvDireccionVal, tvDistanciaVal, tvPedidoNumVal, tvRepartidorVal, btnVerTodosProductos;
    private MaterialButton btnVerRuta;

    private int idPuntoVenta;
    private String nombrePuntoVenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_punto_venta);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.color_fondo));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.color_fondo));

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Vincular Vistas
        btnBack = findViewById(R.id.btnBack);
        tvBadgeNumero = findViewById(R.id.tvBadgeNumero);
        tvNombreTienda = findViewById(R.id.tvNombreTienda);
        tvEstadoBadge = findViewById(R.id.tvEstadoBadge);
        tvDireccionVal = findViewById(R.id.tvDireccionVal);
        tvDistanciaVal = findViewById(R.id.tvDistanciaVal);
        tvPedidoNumVal = findViewById(R.id.tvPedidoNumVal);
        tvRepartidorVal = findViewById(R.id.tvRepartidorVal);
        btnVerTodosProductos = findViewById(R.id.btnVerTodosProductos);
        btnVerRuta = findViewById(R.id.btnVerRuta);

        // 2. Recibir los datos enviados por PuntoVentaAdapter
        if (getIntent() != null) {
            idPuntoVenta = getIntent().getIntExtra("ID_PDV", -1);
            nombrePuntoVenta = getIntent().getStringExtra("NOMBRE_PDV");

            if (nombrePuntoVenta != null && !nombrePuntoVenta.isEmpty()) {
                tvNombreTienda.setText(nombrePuntoVenta);
            }
        }

        // 3. Listener del botón atrás
        btnBack.setOnClickListener(v -> finish());

        // 4. Listener Ver Todos -> Abre Detalle del Pedido
        if (btnVerTodosProductos != null) {
            btnVerTodosProductos.setOnClickListener(v -> {
                Intent intent = new Intent(this, DetallePedidoActivity.class);
                intent.putExtra("ID_PEDIDO", 1);
                intent.putExtra("NOMBRE_TIENDA", tvNombreTienda.getText().toString());
                intent.putExtra("NUMERO_PEDIDO", "0245");
                intent.putExtra("ESTADO_PEDIDO", "En preparación");
                startActivity(intent);
            });
        }

        // 5. Listener Ver Ruta de Entrega
        if (btnVerRuta != null) {
            btnVerRuta.setOnClickListener(v -> 
                Toast.makeText(this, "Navegando a ruta de entrega para: " + tvNombreTienda.getText(), Toast.LENGTH_SHORT).show()
            );
        }
    }
}