package com.piccolifield;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.adapter.AlertaAdapter;
import com.piccolifield.adapter.PuntoVentaAdapter;
import com.piccolifield.adapter.VendedorAdapter;
import com.piccolifield.model.Alerta;
import com.piccolifield.model.PuntoVenta;
import com.piccolifield.model.Vendedor;

import java.util.ArrayList;
import java.util.List;

public class SupervisionpdvActivity extends AppCompatActivity {

    private TextView tvTabInicio, tvTabPuntos, tvTabAlertas, tvTituloLista;
    private RecyclerView rvVendedores;
    private String pestanaActual = "INICIO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisionpdv);

        // 1. Enlazar las vistas
        tvTabInicio = findViewById(R.id.tvTabInicio);
        tvTabPuntos = findViewById(R.id.tvTabPuntos);
        tvTabAlertas = findViewById(R.id.tvTabAlertas);
        tvTituloLista = findViewById(R.id.tvTituloLista); // Vinculamos el título

        // 2. Configurar RecyclerView
        rvVendedores = findViewById(R.id.rvVendedores);
        rvVendedores.setLayoutManager(new LinearLayoutManager(this));

        // 3. Configurar el estilo inicial (Inicio seleccionado por defecto)
        seleccionarPestana(tvTabInicio);

        // 4. Eventos de clic
        tvTabInicio.setOnClickListener(v -> seleccionarPestana(tvTabInicio));
        tvTabPuntos.setOnClickListener(v -> seleccionarPestana(tvTabPuntos));
        tvTabAlertas.setOnClickListener(v -> seleccionarPestana(tvTabAlertas));
    }

    private void seleccionarPestana(TextView pestanaSeleccionada) {
        // Resetear visualmente todas
        resetearPestana(tvTabInicio);
        resetearPestana(tvTabPuntos);
        resetearPestana(tvTabAlertas);

        // Activar la seleccionada
        pestanaSeleccionada.setBackgroundResource(R.drawable.bg_chip_selected);
        pestanaSeleccionada.setTextColor(ContextCompat.getColor(this, R.color.white));
        pestanaSeleccionada.setTypeface(null, Typeface.BOLD);

        // Cambiar contenido dinámicamente
        if (pestanaSeleccionada == tvTabInicio) {
            pestanaActual = "INICIO";
            tvTituloLista.setText("Mejores Vendedores");
            cargarDatosInicio();

        } else if (pestanaSeleccionada == tvTabPuntos) {
            pestanaActual = "PUNTOS";
            tvTituloLista.setText("Puntos de Venta");
            cargarDatosPuntos();

        } else if (pestanaSeleccionada == tvTabAlertas) {
            pestanaActual = "ALERTAS";
            tvTituloLista.setText("Alertas Recientes");
            cargarDatosAlertas();
        }
    }

    private void resetearPestana(TextView pestana) {
        pestana.setBackgroundResource(R.drawable.bg_chip_unselected);
        pestana.setTextColor(ContextCompat.getColor(this, R.color.black));
        pestana.setTypeface(null, Typeface.NORMAL);
    }

    // --- MÉTODOS PARA CARGAR DATOS (Aquí usarás Retrofit luego) ---

    private void cargarDatosInicio() {
        List<Vendedor> vendedores = new ArrayList<>();
        vendedores.add(new Vendedor(1, "Luis Rojas", "Licorería El Buen Sabor", 25));
        vendedores.add(new Vendedor(2, "Ana Torres", "Bodega Central", 18));

        VendedorAdapter adapter = new VendedorAdapter(vendedores, this);
        rvVendedores.setAdapter(adapter);
    }

    private void cargarDatosPuntos() {
        List<PuntoVenta> puntos = new ArrayList<>();
        puntos.add(new PuntoVenta(1, "Licorería El Buen Sabor", "Av. Santa Victoria 456", "ACTIVO"));
        puntos.add(new PuntoVenta(2, "Plaza Vea Chiclayo", "Mall Aventura", "ACTIVO"));

        PuntoVentaAdapter adapter = new PuntoVentaAdapter(puntos, this);
        rvVendedores.setAdapter(adapter);
    }

    private void cargarDatosAlertas() {
        List<Alerta> alertas = new ArrayList<>();
        alertas.add(new Alerta(1, "Stock crítico de producto", "Licorería El Buen Sabor", "Whisky · 2 unidades disponibles", "Hace 5 min", "Crítica"));
        alertas.add(new Alerta(2, "Asistencia pendiente de validar", "Licorería Ruta", "Un registro requiere revisión", "Hace 32 min", "Pendiente"));

        AlertaAdapter adapter = new AlertaAdapter(alertas, this);
        rvVendedores.setAdapter(adapter);
    }
}