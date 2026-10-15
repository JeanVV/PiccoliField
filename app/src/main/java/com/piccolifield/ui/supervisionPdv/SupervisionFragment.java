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
import com.piccolifield.adapter.VendedorAdapter;
import com.piccolifield.adapter.AlertaAdapter;
import com.piccolifield.adapter.SupervisionPuntoVentaAdapter;
import com.piccolifield.data.model.ResumenDashboard;
import com.piccolifield.model.Alerta;
import com.piccolifield.model.Vendedor;
import com.piccolifield.model.SupervisionPuntoVenta;
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
    private RecyclerView rvAlertas;
    private RecyclerView rvPuntosVenta;
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

        // El RecyclerView de Alertas
        rvAlertas = view.findViewById(R.id.rvAlertas);
        rvAlertas.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // El RecyclerView de Puntos de Venta
        rvPuntosVenta = view.findViewById(R.id.rvPuntosVenta);
        rvPuntosVenta.setLayoutManager(new LinearLayoutManager(getContext()));

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

                tvTituloLista.setText(getString(R.string.label_mejores_vendedores));
                if (layoutDashboard != null) layoutDashboard.setVisibility(View.VISIBLE);
                rvVendedores.setVisibility(View.VISIBLE);
                if (rvAlertas != null) rvAlertas.setVisibility(View.GONE);
                if (rvPuntosVenta != null) rvPuntosVenta.setVisibility(View.GONE);

                cargarResumenDashboard();
                cargarMejoresVendedores();
                break;

            case 1: // PUNTOS DE VENTA
                tvTabPuntos.setBackgroundResource(R.drawable.bg_chip_selected);
                tvTabPuntos.setTextColor(Color.WHITE);
                tvTabPuntos.setTypeface(null, Typeface.BOLD);

                tvTituloLista.setText(getString(R.string.label_lista_pdv));
                if (layoutDashboard != null) layoutDashboard.setVisibility(View.GONE);
                rvVendedores.setVisibility(View.GONE);
                if (rvAlertas != null) rvAlertas.setVisibility(View.GONE);
                if (rvPuntosVenta != null) rvPuntosVenta.setVisibility(View.VISIBLE);
                
                cargarPuntosVenta();
                break;

            case 2: // ALERTAS
                tvTabAlertas.setBackgroundResource(R.drawable.bg_chip_selected);
                tvTabAlertas.setTextColor(Color.WHITE);
                tvTabAlertas.setTypeface(null, Typeface.BOLD);

                tvTituloLista.setText(getString(R.string.label_alertas));
                if (layoutDashboard != null) layoutDashboard.setVisibility(View.GONE);
                rvVendedores.setVisibility(View.GONE);
                if (rvPuntosVenta != null) rvPuntosVenta.setVisibility(View.GONE);
                if (rvAlertas != null) rvAlertas.setVisibility(View.VISIBLE);
                
                cargarAlertas();
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
                    Toast.makeText(getContext(), getString(R.string.error_cargar_vendedores), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Vendedor>>> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_red_vendedores), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarAlertas() {
        String token = SplashStorage.obtenerToken(requireContext());
        if (token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.createService();
        apiService.listarAlertasSupervision().enqueue(new Callback<ApiResponse<List<Alerta>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Alerta>>> call, Response<ApiResponse<List<Alerta>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Alerta> listaAlertas = response.body().getData();
                    AlertaAdapter adapter = new AlertaAdapter(listaAlertas, requireContext());
                    rvAlertas.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_cargar_alertas), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Alerta>>> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_red_alertas), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPuntosVenta() {
        String token = SplashStorage.obtenerToken(requireContext());
        if (token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.createService();
        apiService.listarPuntosVentaSupervision().enqueue(new Callback<ApiResponse<List<SupervisionPuntoVenta>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SupervisionPuntoVenta>>> call, Response<ApiResponse<List<SupervisionPuntoVenta>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SupervisionPuntoVenta> lista = response.body().getData();
                    SupervisionPuntoVentaAdapter adapter = new SupervisionPuntoVentaAdapter(lista, requireContext());
                    rvPuntosVenta.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_cargar_pdvs), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SupervisionPuntoVenta>>> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_red_pdvs), Toast.LENGTH_SHORT).show();
            }
        });
    }
}