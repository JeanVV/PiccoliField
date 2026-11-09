package com.piccolifield.ui.supervisionPdv;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.piccolifield.R;
import com.piccolifield.adapter.VendedorAdapter;
import com.piccolifield.adapter.AlertaAdapter;
import com.piccolifield.adapter.SupervisionPuntoVentaAdapter;
import com.piccolifield.data.model.ImpulsadorDashboard;
import com.piccolifield.data.model.ProductoBuscado;
import com.piccolifield.data.model.ResumenDashboard;
import com.piccolifield.data.remote.dto.request.RegistrarVentaRequest;
import com.piccolifield.data.remote.dto.request.ReponerStockRequest;
import com.piccolifield.model.Alerta;
import com.piccolifield.model.Vendedor;
import com.piccolifield.model.SupervisionPuntoVenta;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.sharedpreferences.SplashStorage;
import com.piccolifield.ui.impulsador.ScannerActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;

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
    private ProgressBar pbLoadingListas;

    // Filtros de Alertas
    private View layoutFiltrosAlertas;
    private AlertaAdapter alertaAdapter;
    private VendedorAdapter vendedorAdapter;
    private SupervisionPuntoVentaAdapter puntoVentaAdapter;
    private EditText etBuscar;
    private Spinner spFiltroEstadoAlerta, spFiltroNivelAlerta;
    private int currentTab = 0;

    // Layouts por Rol
    private View layoutSupervisor, layoutImpulsador;

    // Componentes del Impulsador
    private TextView tvLocalAsignado, tvHorario, tvMetaVentas, tvAvanceVentas;
    private ProgressBar pbVentasImpulsador;
    private MaterialButton btnEscanear, btnReportarIncidencia, btnReponerStock;

    private ActivityResultLauncher<Intent> scannerLauncher;
    private boolean isScannerForReposicion = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        String barcode = result.getData().getStringExtra("SCAN_RESULT");
                        if (barcode != null) {
                            if (isScannerForReposicion) {
                                mostrarBottomSheetReposicion(barcode);
                            } else {
                                buscarProducto(barcode);
                            }
                        }
                    }
                }
        );
    }

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
        pbLoadingListas = view.findViewById(R.id.pbLoadingListas);

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

        // Layouts por Rol
        layoutSupervisor = view.findViewById(R.id.layoutSupervisor);
        layoutImpulsador = view.findViewById(R.id.layoutImpulsador);

        // Sub-filtros y Buscador
        layoutFiltrosAlertas = view.findViewById(R.id.layoutFiltrosAlertas);
        etBuscar = view.findViewById(R.id.etBuscar);
        spFiltroEstadoAlerta = view.findViewById(R.id.spFiltroEstadoAlerta);
        spFiltroNivelAlerta = view.findViewById(R.id.spFiltroNivelAlerta);

        // Configurar Spinners de Alertas
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"Estado: Todas", "Pendientes", "Atendidas"});
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spFiltroEstadoAlerta != null) {
            spFiltroEstadoAlerta.setAdapter(adapterEstado);
            spFiltroEstadoAlerta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (alertaAdapter == null) return;
                    String estado = "TODOS";
                    if (position == 1) estado = "PENDIENTE";
                    else if (position == 2) estado = "ATENDIDA";
                    alertaAdapter.filtrar(estado);
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        ArrayAdapter<String> adapterNivel = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"Nivel: Todos", "Stock Bajo", "Producto Agotado"});
        adapterNivel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spFiltroNivelAlerta != null) {
            spFiltroNivelAlerta.setAdapter(adapterNivel);
            spFiltroNivelAlerta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (alertaAdapter == null) return;
                    String nivel = "TODOS";
                    if (position == 1) nivel = "STOCK_BAJO";
                    else if (position == 2) nivel = "PRODUCTO_AGOTADO";
                    alertaAdapter.filtrarPorNivel(nivel);
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        // Configurar Buscador Dinámico según Pestaña
        if (etBuscar != null) {
            etBuscar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s != null ? s.toString() : "";
                    if (currentTab == 0 && vendedorAdapter != null) {
                        vendedorAdapter.filtrar(query);
                    } else if (currentTab == 1 && puntoVentaAdapter != null) {
                        puntoVentaAdapter.filtrar(query);
                    } else if (currentTab == 2 && alertaAdapter != null) {
                        alertaAdapter.filtrarPorTexto(query);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Bind Impulsador views
        tvLocalAsignado = view.findViewById(R.id.tvLocalAsignado);
        tvHorario = view.findViewById(R.id.tvHorario);
        tvMetaVentas = view.findViewById(R.id.tvMetaVentas);
        tvAvanceVentas = view.findViewById(R.id.tvAvanceVentas);
        pbVentasImpulsador = view.findViewById(R.id.pbVentasImpulsador);
        btnEscanear = view.findViewById(R.id.btnEscanear);
        btnReportarIncidencia = view.findViewById(R.id.btnReportarIncidencia);
        btnReponerStock = view.findViewById(R.id.btnReponerStock);

        if (btnEscanear != null) {
            btnEscanear.setOnClickListener(v -> {
                isScannerForReposicion = false;
                Intent intent = new Intent(requireActivity(), ScannerActivity.class);
                scannerLauncher.launch(intent);
            });
        }

        if (btnReportarIncidencia != null) {
            btnReportarIncidencia.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Próximamente: Incidencias", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnReponerStock != null) {
            btnReponerStock.setOnClickListener(v -> {
                isScannerForReposicion = true;
                Intent intent = new Intent(requireActivity(), ScannerActivity.class);
                scannerLauncher.launch(intent);
            });
        }

        // 2. Eventos de las Pestañas
        tvTabInicio.setOnClickListener(v -> seleccionarPestana(0));
        tvTabPuntos.setOnClickListener(v -> seleccionarPestana(1));
        tvTabAlertas.setOnClickListener(v -> seleccionarPestana(2));

        // 3. Evaluar el ROL del Usuario
        String rol = SplashStorage.obtenerRol(requireContext());
        if ("IMPULSADOR".equalsIgnoreCase(rol)) {
            if (layoutSupervisor != null) layoutSupervisor.setVisibility(View.GONE);
            if (layoutImpulsador != null) layoutImpulsador.setVisibility(View.VISIBLE);
            cargarDashboardImpulsador();
        } else {
            if (layoutImpulsador != null) layoutImpulsador.setVisibility(View.GONE);
            if (layoutSupervisor != null) layoutSupervisor.setVisibility(View.VISIBLE);
            seleccionarPestana(0);
        }
    }

    // ==========================================
    // LÓGICA DE PESTAÑAS (UI y Cambio de Datos)
    // ==========================================
    private void seleccionarPestana(int indice) {
        currentTab = indice;
        if (etBuscar != null) {
            etBuscar.setText("");
            if (indice == 0) etBuscar.setHint("Buscar impulsador o local...");
            else if (indice == 1) etBuscar.setHint("Buscar punto de venta o dirección...");
            else if (indice == 2) etBuscar.setHint("Buscar alerta por producto o local...");
        }

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
                if (layoutFiltrosAlertas != null) layoutFiltrosAlertas.setVisibility(View.GONE);
                // APLICAR ESTILO AL CHIP SELECCIONADO (Fondo chocolate, texto blanco)
                tvTabInicio.setBackgroundResource(R.drawable.bg_chip_selected);
                tvTabInicio.setTextColor(Color.WHITE); // Blanco para que contraste con el chocolate
                tvTabInicio.setTypeface(null, Typeface.BOLD);

                tvTituloLista.setText(getString(R.string.label_mejores_impulsadores));
                if (layoutDashboard != null) layoutDashboard.setVisibility(View.VISIBLE);
                rvVendedores.setVisibility(View.GONE);
                if (rvAlertas != null) rvAlertas.setVisibility(View.GONE);
                if (rvPuntosVenta != null) rvPuntosVenta.setVisibility(View.GONE);

                cargarResumenDashboard();
                cargarMejoresVendedores();
                break;

            case 1: // PUNTOS DE VENTA
                if (layoutFiltrosAlertas != null) layoutFiltrosAlertas.setVisibility(View.GONE);
                tvTabPuntos.setBackgroundResource(R.drawable.bg_chip_selected);
                tvTabPuntos.setTextColor(Color.WHITE);
                tvTabPuntos.setTypeface(null, Typeface.BOLD);

                tvTituloLista.setText(getString(R.string.label_lista_pdv));
                if (layoutDashboard != null) layoutDashboard.setVisibility(View.GONE);
                rvVendedores.setVisibility(View.GONE);
                if (rvAlertas != null) rvAlertas.setVisibility(View.GONE);
                if (rvPuntosVenta != null) rvPuntosVenta.setVisibility(View.GONE);
                
                cargarPuntosVenta();
                break;

            case 2: // ALERTAS
                if (layoutFiltrosAlertas != null) layoutFiltrosAlertas.setVisibility(View.VISIBLE);
                tvTabAlertas.setBackgroundResource(R.drawable.bg_chip_selected);
                tvTabAlertas.setTextColor(Color.WHITE);
                tvTabAlertas.setTypeface(null, Typeface.BOLD);

                tvTituloLista.setText(getString(R.string.label_alertas));
                if (layoutDashboard != null) layoutDashboard.setVisibility(View.GONE);
                rvVendedores.setVisibility(View.GONE);
                if (rvPuntosVenta != null) rvPuntosVenta.setVisibility(View.GONE);
                if (rvAlertas != null) rvAlertas.setVisibility(View.GONE);
                
                if (spFiltroEstadoAlerta != null) spFiltroEstadoAlerta.setSelection(0);
                if (spFiltroNivelAlerta != null) spFiltroNivelAlerta.setSelection(0);
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
                if (!isAdded()) return;
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
        if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.VISIBLE);
        String token = SplashStorage.obtenerToken(requireContext());
        if (token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.createService();
        apiService.listarMejoresVendedores().enqueue(new Callback<ApiResponse<List<Vendedor>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Vendedor>>> call, Response<ApiResponse<List<Vendedor>>> response) {
                if (!isAdded()) return;
                if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Vendedor> listaVendedores = response.body().getData();

                    // Conectar la lista real con tu adaptador
                    vendedorAdapter = new VendedorAdapter(listaVendedores, requireContext());
                    rvVendedores.setAdapter(vendedorAdapter);
                    rvVendedores.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_cargar_vendedores), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Vendedor>>> call, Throwable t) {
                if (!isAdded()) return;
                if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.error_red_vendedores), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void cargarAlertas() {
        if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.VISIBLE);
        String token = SplashStorage.obtenerToken(requireContext());
        if (token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.createService();
        apiService.listarAlertasSupervision().enqueue(new Callback<ApiResponse<List<Alerta>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Alerta>>> call, Response<ApiResponse<List<Alerta>>> response) {
                if (!isAdded()) return;
                if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Alerta> rawList = response.body().getData();

                    // Deduplicar alertas por id para evitar repetición en pantalla
                    List<Alerta> listaAlertas = new ArrayList<>();
                    Set<Integer> idsVistos = new HashSet<>();
                    for (Alerta a : rawList) {
                        if (a != null && idsVistos.add(a.getId())) {
                            listaAlertas.add(a);
                        }
                    }

                    alertaAdapter = new AlertaAdapter(listaAlertas, requireContext());
                    rvAlertas.setAdapter(alertaAdapter);
                    if (rvAlertas != null) rvAlertas.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_cargar_alertas), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Alerta>>> call, Throwable t) {
                if (!isAdded()) return;
                if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.error_red_alertas), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPuntosVenta() {
        if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.VISIBLE);
        String token = SplashStorage.obtenerToken(requireContext());
        if (token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.createService();
        apiService.listarPuntosVentaSupervision().enqueue(new Callback<ApiResponse<List<SupervisionPuntoVenta>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SupervisionPuntoVenta>>> call, Response<ApiResponse<List<SupervisionPuntoVenta>>> response) {
                if (!isAdded()) return;
                if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<SupervisionPuntoVenta> lista = response.body().getData();
                    puntoVentaAdapter = new SupervisionPuntoVentaAdapter(lista, requireContext());
                    rvPuntosVenta.setAdapter(puntoVentaAdapter);
                    if (rvPuntosVenta != null) rvPuntosVenta.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_cargar_pdvs), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SupervisionPuntoVenta>>> call, Throwable t) {
                if (!isAdded()) return;
                if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.error_red_pdvs), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================
    // LÓGICA ESPECÍFICA PARA IMPULSADOR
    // ==========================================
    private void cargarDashboardImpulsador() {
        String token = SplashStorage.obtenerToken(requireContext());
        if (token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerDashboardImpulsador().enqueue(new Callback<ApiResponse<ImpulsadorDashboard>>() {
            @Override
            public void onResponse(Call<ApiResponse<ImpulsadorDashboard>> call, Response<ApiResponse<ImpulsadorDashboard>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ImpulsadorDashboard data = response.body().getData();
                    
                    if (data.getPuntoVenta() != null && data.getPuntoVenta().getNombre() != null) {
                        tvLocalAsignado.setText(data.getPuntoVenta().getNombre());
                    } else {
                        tvLocalAsignado.setText("Sin local asignado");
                    }

                    if (data.getHorario() != null && data.getHorario().getJornada() != null) {
                        tvHorario.setText("Horario: " + data.getHorario().getJornada());
                    } else {
                        tvHorario.setText("Horario: ---");
                    }

                    if (data.getProgresoVentas() != null) {
                        tvMetaVentas.setText(String.format("S/ %.2f", data.getProgresoVentas().getMetaMensual()));
                        tvAvanceVentas.setText(String.format("S/ %.2f", data.getProgresoVentas().getMontoMes()));

                        int porcentaje = (int) data.getProgresoVentas().getPorcentajeAlcanzado();
                        pbVentasImpulsador.setProgress(porcentaje);
                    } else if (data.getImpulsador() != null) {
                        tvMetaVentas.setText(String.format("S/ %.2f", data.getImpulsador().getMetaVentas()));
                        tvAvanceVentas.setText("S/ 0.00");
                        pbVentasImpulsador.setProgress(0);
                    }
                } else {
                    if (tvLocalAsignado != null) tvLocalAsignado.setText("Sin asignación activa");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ImpulsadorDashboard>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error al cargar dashboard: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarBottomSheetReposicion(String barcode) {
        Context ctx = getContext();
        if (ctx == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ctx);
        View view = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet_reponer_stock, null);
        bottomSheetDialog.setContentView(view);

        TextView tvEan = view.findViewById(R.id.tvEanReposicion);
        EditText etCantidad = view.findViewById(R.id.etCantidadReposicion);
        EditText etObservacion = view.findViewById(R.id.etObservacionReposicion);
        MaterialButton btnConfirmar = view.findViewById(R.id.btnConfirmarReposicion);

        if (tvEan != null) tvEan.setText("EAN/UPC: " + barcode);

        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> {
                String cantStr = etCantidad != null && etCantidad.getText() != null ? etCantidad.getText().toString().trim() : "10";
                int cantidad = 10;
                try {
                    cantidad = Integer.parseInt(cantStr);
                } catch (NumberFormatException ignored) {}

                String observacion = etObservacion != null && etObservacion.getText() != null ? etObservacion.getText().toString().trim() : "Reposición de stock";

                final int finalCantidad = cantidad;
                btnConfirmar.setEnabled(false);
                ApiService apiService = RetrofitClient.createService();
                ReponerStockRequest request = new ReponerStockRequest(barcode, null, finalCantidad, observacion);

                apiService.reponerStock(request).enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                        if (!isAdded()) return;
                        btnConfirmar.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Stock repuesto exitosamente (+ " + finalCantidad + " unds)", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                            cargarDashboardImpulsador();
                        } else {
                            Toast.makeText(getContext(), "Error al reponer stock", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        btnConfirmar.setEnabled(true);
                        Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        bottomSheetDialog.show();
    }
    private void buscarProducto(String codigoBarras) {
        Toast.makeText(getContext(), "Buscando: " + codigoBarras, Toast.LENGTH_SHORT).show();
        ApiService apiService = RetrofitClient.createService();
        apiService.buscarProductoPorCodigo(codigoBarras).enqueue(new Callback<ApiResponse<ProductoBuscado>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductoBuscado>> call, Response<ApiResponse<ProductoBuscado>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    mostrarBottomSheetVenta(response.body().getData(), codigoBarras);
                } else {
                    Toast.makeText(getContext(), "Producto no encontrado o no asignado a tu punto de venta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductoBuscado>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarBottomSheetVenta(ProductoBuscado producto, String codigoBarras) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet_venta, null);
        bottomSheetDialog.setContentView(view);
        
        ImageView ivProductoScan = view.findViewById(R.id.ivProductoScan);
        TextView tvProductoNombreScan = view.findViewById(R.id.tvProductoNombreScan);
        TextView tvProductoCodigoScan = view.findViewById(R.id.tvProductoCodigoScan);
        TextView tvProductoPrecioScan = view.findViewById(R.id.tvProductoPrecioScan);
        TextView tvProductoStockScan = view.findViewById(R.id.tvProductoStockScan);
        
        MaterialButton btnRestar = view.findViewById(R.id.btnRestar);
        MaterialButton btnSumar = view.findViewById(R.id.btnSumar);
        TextView tvCantidadVenta = view.findViewById(R.id.tvCantidadVenta);
        MaterialButton btnConfirmarVenta = view.findViewById(R.id.btnConfirmarVenta);
        
        tvProductoNombreScan.setText(producto.getNombre());
        tvProductoCodigoScan.setText("EAN/UPC: " + codigoBarras);
        tvProductoPrecioScan.setText("S/ " + String.format("%.2f", producto.getPrecio()));
        tvProductoStockScan.setText(producto.getStockActual() + " unds");
        
        final int[] cantidad = {1};
        
        btnRestar.setOnClickListener(v -> {
            if (cantidad[0] > 1) {
                cantidad[0]--;
                tvCantidadVenta.setText(String.valueOf(cantidad[0]));
            }
        });
        
        btnSumar.setOnClickListener(v -> {
            if (cantidad[0] < producto.getStockActual()) {
                cantidad[0]++;
                tvCantidadVenta.setText(String.valueOf(cantidad[0]));
            } else {
                Toast.makeText(getContext(), "No hay stock suficiente", Toast.LENGTH_SHORT).show();
            }
        });
        
        if (producto.getId() > 0) {
            ApiService apiService = RetrofitClient.createService();
            apiService.obtenerFotoProducto(producto.getId()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        ivProductoScan.post(() -> ivProductoScan.setImageBitmap(bmp));
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {}
            });
        }
        
        btnConfirmarVenta.setOnClickListener(v -> {
            btnConfirmarVenta.setEnabled(false);
            ApiService apiService = RetrofitClient.createService();
            RegistrarVentaRequest request = new RegistrarVentaRequest(codigoBarras, cantidad[0]);
            
            apiService.registrarVenta(request).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                    if (!isAdded()) return;
                    btnConfirmarVenta.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Venta registrada exitosamente", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        cargarDashboardImpulsador();
                    } else {
                        Toast.makeText(getContext(), "Error al registrar la venta", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                    if (!isAdded()) return;
                    btnConfirmarVenta.setEnabled(true);
                    Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        bottomSheetDialog.show();
    }
}