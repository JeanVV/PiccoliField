package com.piccolifield.ui.supervisionPdv;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.adapter.VendedorAdapter; // Asegúrate de que este import sea correcto
import com.piccolifield.data.model.ResumenDashboard;
import com.piccolifield.model.Vendedor;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupervisionFragment extends Fragment {

    // Variables del Dashboard
    private TextView tvMontoTotalVentas, tvPdvActivos, tvAlertasPendientes;

    // Pestañas
    private TextView tvTabInicio, tvTabPuntos, tvTabAlertas;

    // Listas y Contenedores
    private RecyclerView rvVendedores;
    private View layoutDashboard; // Para ocultar/mostrar las tarjetas de resumen
    private TextView tvTituloLista;

    public SupervisionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_supervisionpdv, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Vincular IDs
        tvMontoTotalVentas = view.findViewById(R.id.tvTotalVentasValue);
        tvPdvActivos = view.findViewById(R.id.tvPdvActivos);
        tvAlertasPendientes = view.findViewById(R.id.tvAlertasPendientes);
        tvTabInicio = view.findViewById(R.id.tvTabInicio);
        tvTabPuntos = view.findViewById(R.id.tvTabPuntos);
        tvTabAlertas = view.findViewById(R.id.tvTabAlertas);
        tvTituloLista = view.findViewById(R.id.tvTituloLista);

        // El RecyclerView de Vendedores
        rvVendedores = view.findViewById(R.id.rvVendedores);
        rvVendedores.setLayoutManager(new LinearLayoutManager(getContext()));

        // (Opcional pero recomendado) En tu XML deberías envolver tus 2 MaterialCardView del dashboard
        // dentro de un LinearLayout con el id "@+id/layoutDashboard" para poder ocultarlos fácilmente.
        layoutDashboard = view.findViewById(R.id.layoutDashboard);

        // 2. Eventos de las Pestañas
        tvTabInicio.setOnClickListener(v -> seleccionarPestana(0));
        tvTabPuntos.setOnClickListener(v -> seleccionarPestana(1));
        tvTabAlertas.setOnClickListener(v -> seleccionarPestana(2));

        // 3. Cargar datos iniciales (Pestaña Inicio)
        seleccionarPestana(0);
    }

    // ==========================================
    // LÓGICA DE PESTAÑAS (UI y Cambio de Datos)
    // ==========================================
    private void seleccionarPestana(int indice) {
        // 1. REINICIAR ESTILOS A CHIP APAGADO (Fondo transparente, texto gris)
        tvTabInicio.setBackgroundResource(R.drawable.bg_chip_unselected);
        tvTabInicio.setTextColor(Color.parseColor("#757575"));
        tvTabInicio.setTypeface(null, Typeface.NORMAL);

        tvTabPuntos.setBackgroundResource(R.drawable.bg_chip_unselected);
        tvTabPuntos.setTextColor(Color.parseColor("#757575"));
        tvTabPuntos.setTypeface(null, Typeface.NORMAL);

        tvTabAlertas.setBackgroundResource(R.drawable.bg_chip_unselected);
        tvTabAlertas.setTextColor(Color.parseColor("#757575"));
        tvTabAlertas.setTypeface(null, Typeface.NORMAL);

        switch (indice) {
            case 0: // INICIO
                // APLICAR ESTILO AL CHIP SELECCIONADO (Fondo chocolate, texto blanco)
                tvTabInicio.setBackgroundResource(R.drawable.bg_chip_selected);
                tvTabInicio.setTextColor(Color.WHITE); // Blanco para que contraste con el chocolate
                tvTabInicio.setTypeface(null, Typeface.BOLD);

                tvTituloLista.setText("Mejores Vendedores");
                if (layoutDashboard != null) layoutDashboard.setVisibility(View.VISIBLE);
                rvVendedores.setVisibility(View.VISIBLE);

                cargarResumenDashboard();
                cargarMejoresVendedores();
                break;

            case 1: // PUNTOS DE VENTA
                tvTabPuntos.setBackgroundResource(R.drawable.bg_chip_selected);
                tvTabPuntos.setTextColor(Color.WHITE);
                tvTabPuntos.setTypeface(null, Typeface.BOLD);

                tvTituloLista.setText("Lista de Puntos de Venta");
                if (layoutDashboard != null) layoutDashboard.setVisibility(View.GONE);
                rvVendedores.setVisibility(View.GONE);
                break;

            case 2: // ALERTAS
                tvTabAlertas.setBackgroundResource(R.drawable.bg_chip_selected);
                tvTabAlertas.setTextColor(Color.WHITE);
                tvTabAlertas.setTypeface(null, Typeface.BOLD);

                tvTituloLista.setText("Alertas de Stock");
                if (layoutDashboard != null) layoutDashboard.setVisibility(View.GONE);
                rvVendedores.setVisibility(View.GONE);
                break;
        }
    }

    // ==========================================
    // LLAMADAS A LA API
    // ==========================================
    private void cargarResumenDashboard() {
        String token = SplashStorage.obtenerToken(requireContext());
        if (token == null || token.isEmpty()) return;

        RetrofitClient.API_TOKEN = token;
        ApiService apiService = RetrofitClient.createService();

        apiService.obtenerResumenDashboard().enqueue(new Callback<ApiResponse<ResumenDashboard>>() {
            @Override
            public void onResponse(Call<ApiResponse<ResumenDashboard>> call, Response<ApiResponse<ResumenDashboard>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ResumenDashboard resumen = response.body().getData();
                    tvMontoTotalVentas.setText("S/ " + String.format("%.2f", resumen.getMonto_total_ventas()));
                    tvPdvActivos.setText(String.valueOf(resumen.getCantidad_pdv_activos()));
                    tvAlertasPendientes.setText(String.valueOf(resumen.getCantidad_alertas()));
                }
            }
            @Override public void onFailure(Call<ApiResponse<ResumenDashboard>> call, Throwable t) {}
        });
    }

    private void cargarMejoresVendedores() {
        String token = SplashStorage.obtenerToken(requireContext());
        if (token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.createService();
        apiService.listarMejoresVendedores().enqueue(new Callback<ApiResponse<List<Vendedor>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Vendedor>>> call, Response<ApiResponse<List<Vendedor>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Vendedor> listaVendedores = response.body().getData();

                    // Conectar la lista real con tu adaptador
                    VendedorAdapter adapter = new VendedorAdapter(listaVendedores, requireContext());
                    rvVendedores.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "No se pudieron cargar los vendedores", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Vendedor>>> call, Throwable t) {
                Toast.makeText(getContext(), "Falla de red cargando vendedores", Toast.LENGTH_SHORT).show();
            }
        });
    }
}