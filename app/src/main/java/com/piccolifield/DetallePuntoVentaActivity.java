package com.piccolifield;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    private String numeroPedido = "0245";
    private String estadoDespacho = "En preparación";

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
            String direccion = getIntent().getStringExtra("DIRECCION");
            String distancia = getIntent().getStringExtra("DISTANCIA");
            String numPed = getIntent().getStringExtra("NUMERO_PEDIDO");
            String estadoDisp = getIntent().getStringExtra("ESTADO_DESPACHO");
            String badgeNum = getIntent().getStringExtra("BADGE_NUMERO");

            if (nombrePuntoVenta != null && !nombrePuntoVenta.isEmpty()) {
                tvNombreTienda.setText(nombrePuntoVenta);
            }
            if (badgeNum != null && !badgeNum.isEmpty() && tvBadgeNumero != null) {
                tvBadgeNumero.setText(badgeNum);
            }
            if (direccion != null && !direccion.isEmpty() && tvDireccionVal != null) {
                tvDireccionVal.setText(direccion);
            }
            if (distancia != null && !distancia.isEmpty() && tvDistanciaVal != null) {
                tvDistanciaVal.setText(distancia);
            }
            if (numPed != null && !numPed.isEmpty()) {
                numeroPedido = numPed;
                if (tvPedidoNumVal != null) {
                    tvPedidoNumVal.setText(numPed);
                }
            }
            if (estadoDisp != null && !estadoDisp.isEmpty()) {
                estadoDespacho = estadoDisp;
                if (tvEstadoBadge != null) {
                    tvEstadoBadge.setText(estadoDisp);
                }
            }
        }

        // 3. Listener del botón atrás
        btnBack.setOnClickListener(v -> finish());

        // 4. Listener Ver Todos / Productos -> Abre Detalle del Pedido
        View.OnClickListener abrirDetallePedido = v -> {
            try {
                Intent intent = new Intent(DetallePuntoVentaActivity.this, DetallePedidoActivity.class);
                intent.putExtra("ID_PEDIDO", idPuntoVenta);
                intent.putExtra("NOMBRE_TIENDA", tvNombreTienda != null ? tvNombreTienda.getText().toString() : "Licorería El Buen Sabor");
                intent.putExtra("NUMERO_PEDIDO", numeroPedido);
                intent.putExtra("ESTADO_PEDIDO", estadoDespacho);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        if (btnVerTodosProductos != null) {
            btnVerTodosProductos.setOnClickListener(abrirDetallePedido);
        }

        View layoutProductosHeader = findViewById(R.id.layoutProductosHeader);
        if (layoutProductosHeader != null) {
            layoutProductosHeader.setOnClickListener(abrirDetallePedido);
        }

        // 5. Listener Ver Ruta de Entrega
        if (btnVerRuta != null) {
            btnVerRuta.setOnClickListener(v -> 
                Toast.makeText(this, "Navegando a ruta de entrega para: " + tvNombreTienda.getText(), Toast.LENGTH_SHORT).show()
            );
        }
    }
}