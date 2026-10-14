package com.piccolifield;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class DetallePedidoActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvNombreTienda, tvPedidoNum, tvEstadoBadge, tvFechaPedido, tvTipoPedido, tvTotalProductosVal, tvTotalUnidadesVal;

    private int idPedido;
    private String nombreTienda, numeroPedido, estadoPedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_pedido);

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
        tvNombreTienda = findViewById(R.id.tvNombreTienda);
        tvPedidoNum = findViewById(R.id.tvPedidoNum);
        tvEstadoBadge = findViewById(R.id.tvEstadoBadge);
        tvFechaPedido = findViewById(R.id.tvFechaPedido);
        tvTipoPedido = findViewById(R.id.tvTipoPedido);
        tvTotalProductosVal = findViewById(R.id.tvTotalProductosVal);
        tvTotalUnidadesVal = findViewById(R.id.tvTotalUnidadesVal);

        // 2. Recibir los datos enviados por Intent
        if (getIntent() != null) {
            idPedido = getIntent().getIntExtra("ID_PEDIDO", 1);
            nombreTienda = getIntent().getStringExtra("NOMBRE_TIENDA");
            numeroPedido = getIntent().getStringExtra("NUMERO_PEDIDO");
            estadoPedido = getIntent().getStringExtra("ESTADO_PEDIDO");

            if (nombreTienda != null && !nombreTienda.isEmpty()) {
                tvNombreTienda.setText(nombreTienda);
            }
            if (numeroPedido != null && !numeroPedido.isEmpty()) {
                if (!numeroPedido.startsWith("Nº Pedido")) {
                    tvPedidoNum.setText("Nº Pedido: " + numeroPedido);
                } else {
                    tvPedidoNum.setText(numeroPedido);
                }
            }
            if (estadoPedido != null && !estadoPedido.isEmpty()) {
                tvEstadoBadge.setText(estadoPedido);
            }
        }

        // 3. Listener del botón atrás
        btnBack.setOnClickListener(v -> finish());
    }
}