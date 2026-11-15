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
import android.util.Base64;
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
import com.piccolifield.adapter.IncidenciaAdapter;
import com.piccolifield.adapter.VendedorAdapter;
import com.piccolifield.adapter.AlertaAdapter;
import com.piccolifield.adapter.SupervisionPuntoVentaAdapter;
import com.piccolifield.data.model.ImpulsadorDashboard;
import com.piccolifield.data.model.Incidencia;
import com.piccolifield.data.model.ProductoBuscado;
import com.piccolifield.data.model.ResumenDashboard;
import com.piccolifield.data.model.TipoIncidenciaDto;
import com.piccolifield.data.model.TiposIncidenciaResponse;
import com.piccolifield.data.remote.dto.request.AtenderIncidenciaRequest;
import com.piccolifield.data.remote.dto.request.RegistrarVentaRequest;
import com.piccolifield.data.remote.dto.request.ReasignarVendedorRequest;
import com.piccolifield.data.remote.dto.request.ReportarIncidenciaRequest;
import com.piccolifield.data.remote.dto.request.ReponerStockRequest;
import com.piccolifield.data.remote.dto.request.RevisionIncidenciaRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
    private TextView tvTabInicio, tvTabPuntos, tvTabAlertas, tvTabIncidencias;

    // Listas y Contenedores
    private RecyclerView rvVendedores;
    private RecyclerView rvAlertas;
    private RecyclerView rvPuntosVenta;
    private RecyclerView rvIncidencias;
    private View layoutDashboard; // Para ocultar/mostrar las tarjetas de resumen
    private TextView tvTituloLista;
    private ProgressBar pbLoadingListas;

    private VendedorAdapter vendedorAdapter;
    private SupervisionPuntoVentaAdapter puntoVentaAdapter;
    private AlertaAdapter alertaAdapter;
    private IncidenciaAdapter incidenciaAdapter;

    private EditText etBuscar;
    private Spinner spFiltroEstadoAlerta, spFiltroNivelAlerta;
    private View layoutFiltrosAlertas;
    private int currentTab = 0;

    // Foto Evidencia Incidencia
    private ActivityResultLauncher<String> galleryLauncher;
    private String fotoBase64Incidencia = null;
    private MaterialButton btnAdjuntarFotoRef = null;

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

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && getContext() != null) {
                        try {
                            InputStream is = getContext().getContentResolver().openInputStream(uri);
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            if (bitmap != null) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                                byte[] bytes = baos.toByteArray();
                                fotoBase64Incidencia = Base64.encodeToString(bytes, Base64.NO_WRAP);
                                if (btnAdjuntarFotoRef != null) {
                                    btnAdjuntarFotoRef.setText("Evidencia adjuntada ✓");
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error al cargar imagen", Toast.LENGTH_SHORT).show();
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
        tvTabIncidencias = view.findViewById(R.id.tvTabIncidencias);
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

        // El RecyclerView de Incidencias
        rvIncidencias = view.findViewById(R.id.rvIncidencias);
        rvIncidencias.setLayoutManager(new LinearLayoutManager(getContext()));

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
                    } else if (currentTab == 3 && incidenciaAdapter != null) {
                        incidenciaAdapter.filtrar(query);
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
            btnReportarIncidencia.setOnClickListener(v -> mostrarBottomSheetIncidencia());
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
        if (tvTabIncidencias != null) {
            tvTabIncidencias.setOnClickListener(v -> seleccionarPestana(3));
        }

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
                if (rvVendedores != null) rvVendedores.setVisibility(View.VISIBLE);
                if (rvAlertas != null) rvAlertas.setVisibility(View.GONE);
                if (rvPuntosVenta != null) rvPuntosVenta.setVisibility(View.GONE);
                if (rvIncidencias != null) rvIncidencias.setVisibility(View.GONE);

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
                if (rvVendedores != null) rvVendedores.setVisibility(View.GONE);
                if (rvAlertas != null) rvAlertas.setVisibility(View.GONE);
                if (rvPuntosVenta != null) rvPuntosVenta.setVisibility(View.VISIBLE);
                if (rvIncidencias != null) rvIncidencias.setVisibility(View.GONE);
                
                cargarPuntosVenta();
                break;

            case 2: // ALERTAS
                if (layoutFiltrosAlertas != null) layoutFiltrosAlertas.setVisibility(View.VISIBLE);
                tvTabAlertas.setBackgroundResource(R.drawable.bg_chip_selected);
                tvTabAlertas.setTextColor(Color.WHITE);
                tvTabAlertas.setTypeface(null, Typeface.BOLD);

                tvTituloLista.setText(getString(R.string.label_alertas));
                if (layoutDashboard != null) layoutDashboard.setVisibility(View.GONE);
                if (rvVendedores != null) rvVendedores.setVisibility(View.GONE);
                if (rvPuntosVenta != null) rvPuntosVenta.setVisibility(View.GONE);
                if (rvAlertas != null) rvAlertas.setVisibility(View.VISIBLE);
                if (rvIncidencias != null) rvIncidencias.setVisibility(View.GONE);
                
                if (spFiltroEstadoAlerta != null) spFiltroEstadoAlerta.setSelection(0);
                if (spFiltroNivelAlerta != null) spFiltroNivelAlerta.setSelection(0);
                cargarAlertas();
                break;

            case 3: // INCIDENCIAS
                if (layoutFiltrosAlertas != null) layoutFiltrosAlertas.setVisibility(View.GONE);
                if (tvTabIncidencias != null) {
                    tvTabIncidencias.setBackgroundResource(R.drawable.bg_chip_selected);
                    tvTabIncidencias.setTextColor(Color.WHITE);
                    tvTabIncidencias.setTypeface(null, Typeface.BOLD);
                }

                tvTituloLista.setText("Incidencias Reportadas");
                if (layoutDashboard != null) layoutDashboard.setVisibility(View.GONE);
                if (rvVendedores != null) rvVendedores.setVisibility(View.GONE);
                if (rvAlertas != null) rvAlertas.setVisibility(View.GONE);
                if (rvPuntosVenta != null) rvPuntosVenta.setVisibility(View.GONE);
                if (rvIncidencias != null) rvIncidencias.setVisibility(View.VISIBLE);

                cargarIncidencias();
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
                    if (listaVendedores == null) {
                        listaVendedores = new ArrayList<>();
                    }

                    // Conectar la lista real con tu adaptador
                    vendedorAdapter = new VendedorAdapter(listaVendedores, requireContext());
                    rvVendedores.setAdapter(vendedorAdapter);
                    rvVendedores.setVisibility(View.VISIBLE);
                } else {
                    String errorMsg = "Error";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        } else if (response.body() != null) {
                            errorMsg = response.body().getMessage();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(getContext(), getString(R.string.error_cargar_vendedores) + ": " + response.code() + " " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Vendedor>>> call, Throwable t) {
                if (!isAdded()) return;
                if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.error_red_vendedores) + ": " + t.getMessage(), Toast.LENGTH_LONG).show();
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

        ImageView ivProducto = view.findViewById(R.id.ivProductoReposicion);
        TextView tvNombreProducto = view.findViewById(R.id.tvNombreProductoReposicion);
        TextView tvStockActual = view.findViewById(R.id.tvStockActualReposicion);
        TextView tvEan = view.findViewById(R.id.tvEanReposicion);
        EditText etCantidad = view.findViewById(R.id.etCantidadReposicion);
        EditText etObservacion = view.findViewById(R.id.etObservacionReposicion);
        MaterialButton btnConfirmar = view.findViewById(R.id.btnConfirmarReposicion);

        if (tvEan != null) tvEan.setText("EAN/UPC: " + barcode);

        // Buscar información del producto para mostrar su foto, nombre y stock actual
        ApiService apiService = RetrofitClient.createService();
        apiService.buscarProductoPorCodigo(barcode).enqueue(new Callback<ApiResponse<ProductoBuscado>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ProductoBuscado>> call, @NonNull Response<ApiResponse<ProductoBuscado>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ProductoBuscado prod = response.body().getData();
                    if (tvNombreProducto != null) tvNombreProducto.setText(prod.getNombre());
                    if (tvStockActual != null) tvStockActual.setText("Stock actual: " + prod.getStockActual() + " unds");

                    if (prod.getId() > 0 && ivProducto != null) {
                        apiService.obtenerFotoProducto(prod.getId()).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                                    if (bmp != null) ivProducto.post(() -> ivProducto.setImageBitmap(bmp));
                                }
                            }
                            @Override public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {}
                        });
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<ProductoBuscado>> call, @NonNull Throwable t) {}
        });

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

    private void cargarIncidencias() {
        if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.VISIBLE);
        String token = SplashStorage.obtenerToken(requireContext());
        if (token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.createService();
        apiService.listarIncidenciasSupervision(null).enqueue(new Callback<ApiResponse<List<Incidencia>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Incidencia>>> call, @NonNull Response<ApiResponse<List<Incidencia>>> response) {
                if (!isAdded()) return;
                if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Incidencia> listaIncidencias = response.body().getData();
                    incidenciaAdapter = new IncidenciaAdapter(listaIncidencias, requireContext(), item -> mostrarBottomSheetRevisarIncidencia(item));
                    if (rvIncidencias != null) {
                        rvIncidencias.setAdapter(incidenciaAdapter);
                        rvIncidencias.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar incidencias", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Incidencia>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                if (pbLoadingListas != null) pbLoadingListas.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarBottomSheetRevisarIncidencia(Incidencia item) {
        Context ctx = getContext();
        if (ctx == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ctx);
        View view = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet_revisar_incidencia, null);
        bottomSheetDialog.setContentView(view);

        TextView tvTitulo = view.findViewById(R.id.tvTituloRevisarIncidencia);
        TextView tvLocal = view.findViewById(R.id.tvDetalleLocalIncidencia);
        TextView tvDescripcion = view.findViewById(R.id.tvDetalleDescripcionIncidencia);
        ImageView ivEvidencia = view.findViewById(R.id.ivEvidenciaIncidencia);
        EditText etComentario = view.findViewById(R.id.etComentarioAtencionIncidencia);
        MaterialButton btnEnProceso = view.findViewById(R.id.btnMarcarEnProcesoIncidencia);
        MaterialButton btnResolver = view.findViewById(R.id.btnResolverIncidencia);

        if (tvTitulo != null) tvTitulo.setText(item.getTipo().replace("_", " ") + " (" + item.getSeveridad() + ")");
        if (tvLocal != null) tvLocal.setText("Local: " + item.getLocalNombre());
        if (tvDescripcion != null) tvDescripcion.setText("Descripción: " + item.getDescripcion());
        if (etComentario != null && item.getComentarioAtencion() != null) {
            etComentario.setText(item.getComentarioAtencion());
        }

        if (item.hasEvidencia() && ivEvidencia != null) {
            ivEvidencia.setVisibility(View.VISIBLE);
            ApiService apiService = RetrofitClient.createService();
            apiService.obtenerFotoIncidenciaSupervision(item.getIdIncidencias()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        if (bmp != null) ivEvidencia.post(() -> ivEvidencia.setImageBitmap(bmp));
                    }
                }
                @Override public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {}
            });
        }

        View.OnClickListener listener = v -> {
            String estadoNuevo = (v.getId() == R.id.btnResolverIncidencia) ? "RESUELTO" : "EN_PROCESO";
            String comentario = etComentario != null && etComentario.getText() != null ? etComentario.getText().toString().trim() : "";

            if (comentario.isEmpty()) {
                comentario = estadoNuevo.equals("RESUELTO") ? "Incidencia resuelta por supervisor" : "Incidencia en proceso";
            }

            ApiService apiService = RetrofitClient.createService();
            AtenderIncidenciaRequest request = new AtenderIncidenciaRequest(estadoNuevo, comentario);

            apiService.atenderIncidenciaSupervision(item.getIdIncidencias(), request).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Incidencia marcada como " + estadoNuevo + " exitosamente", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        cargarIncidencias();
                    } else {
                        String errorMsg = "Error al actualizar incidencia";
                        if (response.errorBody() != null) {
                            try {
                                errorMsg = response.errorBody().string();
                            } catch (Exception ignored) {}
                        }
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        };

        if (btnEnProceso != null) btnEnProceso.setOnClickListener(listener);
        if (btnResolver != null) btnResolver.setOnClickListener(listener);

        bottomSheetDialog.show();
    }

    private void mostrarBottomSheetIncidencia() {
        fotoBase64Incidencia = null;
        Context ctx = getContext();
        if (ctx == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ctx);
        View view = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet_incidencia, null);
        bottomSheetDialog.setContentView(view);

        Spinner spTipo = view.findViewById(R.id.spTipoIncidencia);
        Spinner spSeveridad = view.findViewById(R.id.spSeveridadIncidencia);
        EditText etDescripcion = view.findViewById(R.id.etDescripcionIncidencia);
        MaterialButton btnAdjuntar = view.findViewById(R.id.btnAdjuntarFotoIncidencia);
        MaterialButton btnEnviar = view.findViewById(R.id.btnEnviarIncidencia);

        btnAdjuntarFotoRef = btnAdjuntar;
        if (btnAdjuntar != null) {
            btnAdjuntar.setOnClickListener(v -> {
                if (galleryLauncher != null) {
                    galleryLauncher.launch("image/*");
                }
            });
        }

        List<TipoIncidenciaDto> listaTipos = new ArrayList<>();
        List<String> severidades = new ArrayList<>();

        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerTiposIncidencia().enqueue(new Callback<ApiResponse<TiposIncidenciaResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<TiposIncidenciaResponse>> call, @NonNull Response<ApiResponse<TiposIncidenciaResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    TiposIncidenciaResponse data = response.body().getData();
                    if (data.getTipos() != null) listaTipos.addAll(data.getTipos());
                    if (data.getSeveridades() != null) severidades.addAll(data.getSeveridades());

                    if (severidades.isEmpty()) {
                        severidades.add("BAJA");
                        severidades.add("MEDIA");
                        severidades.add("ALTA");
                    }

                    ArrayAdapter<TipoIncidenciaDto> adapterTipo = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, listaTipos);
                    adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    if (spTipo != null) spTipo.setAdapter(adapterTipo);

                    ArrayAdapter<String> adapterSeveridad = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, severidades);
                    adapterSeveridad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    if (spSeveridad != null) spSeveridad.setAdapter(adapterSeveridad);

                    if (spTipo != null) {
                        spTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position >= 0 && position < listaTipos.size()) {
                                    TipoIncidenciaDto item = listaTipos.get(position);
                                    if (item.getSeveridadSugerida() != null && spSeveridad != null) {
                                        for (int i = 0; i < severidades.size(); i++) {
                                            if (severidades.get(i).equalsIgnoreCase(item.getSeveridadSugerida())) {
                                                spSeveridad.setSelection(i);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            @Override public void onNothingSelected(AdapterView<?> parent) {}
                        });
                    }
                }
            }

            @Override public void onFailure(@NonNull Call<ApiResponse<TiposIncidenciaResponse>> call, @NonNull Throwable t) {}
        });

        if (btnEnviar != null) {
            btnEnviar.setOnClickListener(v -> {
                String tipo = "OTRO";
                if (spTipo != null && spTipo.getSelectedItem() instanceof TipoIncidenciaDto) {
                    tipo = ((TipoIncidenciaDto) spTipo.getSelectedItem()).getTipo();
                } else if (spTipo != null && spTipo.getSelectedItem() != null) {
                    tipo = spTipo.getSelectedItem().toString();
                }

                String severidad = spSeveridad != null && spSeveridad.getSelectedItem() != null ? spSeveridad.getSelectedItem().toString() : "MEDIA";
                String descripcion = etDescripcion != null && etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

                if (descripcion.isEmpty()) {
                    if (etDescripcion != null) etDescripcion.setError("Ingrese una descripción del inconveniente");
                    return;
                }

                btnEnviar.setEnabled(false);
                ReportarIncidenciaRequest request = new ReportarIncidenciaRequest(tipo, severidad, descripcion, null, fotoBase64Incidencia);

                apiService.reportarIncidencia(request).enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                        if (!isAdded()) return;
                        btnEnviar.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Incidencia reportada exitosamente", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "Error al reportar incidencia", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        btnEnviar.setEnabled(true);
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