package com.piccolifield;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class DetalleVendedorActivity extends AppCompatActivity {

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_vendedor);

        // 1. Vincular el botón
        btnBack = findViewById(R.id.btnBack);

        // 2. Darle la acción de cerrar la pantalla
        btnBack.setOnClickListener(v -> finish());
    }
}