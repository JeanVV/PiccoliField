package com.piccolifield.ui.despacho;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.piccolifield.R;
import com.piccolifield.adapter.ItemPedidoSeleccionadoAdapter;
import com.piccolifield.adapter.ProductoCatalogoPedidoAdapter;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.repository.PedidoRepository;
import com.piccolifield.model.ItemNuevoPedido;
import com.piccolifield.model.Pedido;
import com.piccolifield.model.Producto;
import com.piccolifield.sharedpreferences.PuntosDistribucionStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoPedidoFragment extends Fragment {

    private int idPuntoVenta;
    private String nombrePuntoVenta;
    private String codigoPuntoVenta;
    private String criterioNegocio;
    private boolean esMayorista;
    private Runnable onPedidoRegistrado;

    private ImageView btnBackNuevoPedido;
    private TextView tvNombreClientePedido, tvCodigoClientePedido, tvCriterioClienteBadge;
    private EditText etBuscarProductoPedido;
    private Spinner spFiltroCategoriaPedido, spFiltroPresentacionPedido;
    private TextView tvContadorCatalogo, tvBadgeCantidadLista, tvListaPedidoVacia;
    private TextView tvTotalUnidadesNuevoPedido, tvTotalMontoNuevoPedido;
    private ProgressBar pbLoadingCatalogo;
    private RecyclerView rvCatalogoProductosPedido, rvListaNuevoPedido;
    private MaterialButton btnRegistrarNuevoPedido;

    private final List<Producto> listaProductosOriginal = new ArrayList<>();
    private final List<Producto> listaProductosFiltrada = new ArrayList<>();
    private final List<ItemNuevoPedido> listaItemsSeleccionados = new ArrayList<>();

    private ProductoCatalogoPedidoAdapter catalogoAdapter;
    private ItemPedidoSeleccionadoAdapter seleccionadosAdapter;

    private final List<String> opcionesCategoria = new ArrayList<>();
    private final List<String> opcionesPresentacion = new ArrayList<>();
    private ArrayAdapter<String> adapterCategoria;
    private ArrayAdapter<String> adapterPresentacion;

    private String categoriaSeleccionada = "TODAS";
    private String presentacionSeleccionada = "TODAS";

    public NuevoPedidoFragment() {}

    public static NuevoPedidoFragment newInstance(int idPuntoVenta, String nombrePuntoVenta, String codigoPuntoVenta, String criterioNegocio) {
        NuevoPedidoFragment fragment = new NuevoPedidoFragment();
        Bundle args = new Bundle();
        args.putInt("idPuntoVenta", idPuntoVenta);
        args.putString("nombrePuntoVenta", nombrePuntoVenta);
        args.putString("codigoPuntoVenta", codigoPuntoVenta);
        args.putString("criterioNegocio", criterioNegocio);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnPedidoRegistradoListener(Runnable listener) {
        this.onPedidoRegistrado = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idPuntoVenta = getArguments().getInt("idPuntoVenta");
            nombrePuntoVenta = getArguments().getString("nombrePuntoVenta");
            codigoPuntoVenta = getArguments().getString("codigoPuntoVenta");
            criterioNegocio = getArguments().getString("criterioNegocio", "Minorista");
            esMayorista = "Mayorista".equalsIgnoreCase(criterioNegocio);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nuevo_pedido, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBackNuevoPedido = view.findViewById(R.id.btnBackNuevoPedido);
        tvNombreClientePedido = view.findViewById(R.id.tvNombreClientePedido);
        tvCodigoClientePedido = view.findViewById(R.id.tvCodigoClientePedido);
        tvCriterioClienteBadge = view.findViewById(R.id.tvCriterioClienteBadge);
        etBuscarProductoPedido = view.findViewById(R.id.etBuscarProductoPedido);
        spFiltroCategoriaPedido = view.findViewById(R.id.spFiltroCategoriaPedido);
        spFiltroPresentacionPedido = view.findViewById(R.id.spFiltroPresentacionPedido);
        tvContadorCatalogo = view.findViewById(R.id.tvContadorCatalogo);
        pbLoadingCatalogo = view.findViewById(R.id.pbLoadingCatalogo);
        rvCatalogoProductosPedido = view.findViewById(R.id.rvCatalogoProductosPedido);
        rvListaNuevoPedido = view.findViewById(R.id.rvListaNuevoPedido);
        tvBadgeCantidadLista = view.findViewById(R.id.tvBadgeCantidadLista);
        tvListaPedidoVacia = view.findViewById(R.id.tvListaPedidoVacia);
        tvTotalUnidadesNuevoPedido = view.findViewById(R.id.tvTotalUnidadesNuevoPedido);
        tvTotalMontoNuevoPedido = view.findViewById(R.id.tvTotalMontoNuevoPedido);
        btnRegistrarNuevoPedido = view.findViewById(R.id.btnRegistrarNuevoPedido);

        configurarCabeceraCliente();
        configurarAdapters();
        configurarFiltrosBusqueda();
        cargarProductosCatalogo();

        btnBackNuevoPedido.setOnClickListener(v -> cerrarFragment());
        btnRegistrarNuevoPedido.setOnClickListener(v -> registrarNuevoPedidoCompleto());
    }

    private void configurarCabeceraCliente() {
        tvNombreClientePedido.setText(nombrePuntoVenta != null && !nombrePuntoVenta.isEmpty()
                ? nombrePuntoVenta
                : "Punto de Distribución #" + idPuntoVenta);

        String cod = codigoPuntoVenta != null && !codigoPuntoVenta.isEmpty()
                ? codigoPuntoVenta
                : String.format(Locale.US, "PDV-%03d", Math.max(1, idPuntoVenta));
        if (!cod.toLowerCase().startsWith("código") && !cod.toLowerCase().startsWith("codigo")) {
            cod = "Código: " + cod;
        }
        tvCodigoClientePedido.setText(cod);

        String crit = esMayorista ? "Mayorista" : "Minorista";
        tvCriterioClienteBadge.setText(crit);
        if (esMayorista) {
            tvCriterioClienteBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32")));
        } else {
            tvCriterioClienteBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2B170F")));
        }
    }

    private void configurarAdapters() {
        rvCatalogoProductosPedido.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2));
        catalogoAdapter = new ProductoCatalogoPedidoAdapter(listaProductosFiltrada, esMayorista, (producto, cantidad, precioUnitario) -> {
            agregarProductoALista(producto, cantidad, precioUnitario);
        });
        rvCatalogoProductosPedido.setAdapter(catalogoAdapter);

        rvListaNuevoPedido.setLayoutManager(new LinearLayoutManager(requireContext()));
        seleccionadosAdapter = new ItemPedidoSeleccionadoAdapter(listaItemsSeleccionados, new ItemPedidoSeleccionadoAdapter.OnItemPedidoChangeListener() {
            @Override
            public void onCantidadCambiada() {
                actualizarResumenListaPedido();
            }

            @Override
            public void onItemEliminado(int position) {
                if (position >= 0 && position < listaItemsSeleccionados.size()) {
                    listaItemsSeleccionados.remove(position);
                    seleccionadosAdapter.notifyDataSetChanged();
                    actualizarResumenListaPedido();
                }
            }
        });
        rvListaNuevoPedido.setAdapter(seleccionadosAdapter);
    }

    private void configurarFiltrosBusqueda() {
        opcionesCategoria.clear();
        opcionesCategoria.add("Categoría: Todas");
        adapterCategoria = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, opcionesCategoria);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltroCategoriaPedido.setAdapter(adapterCategoria);

        opcionesPresentacion.clear();
        opcionesPresentacion.add("Presentación: Todas");
        adapterPresentacion = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, opcionesPresentacion);
        adapterPresentacion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltroPresentacionPedido.setAdapter(adapterPresentacion);

        spFiltroCategoriaPedido.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0) {
                    categoriaSeleccionada = "TODAS";
                } else {
                    categoriaSeleccionada = opcionesCategoria.get(position);
                }
                filtrarCatalogo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spFiltroPresentacionPedido.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0) {
                    presentacionSeleccionada = "TODAS";
                } else {
                    presentacionSeleccionada = opcionesPresentacion.get(position);
                }
                filtrarCatalogo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etBuscarProductoPedido.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarCatalogo();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void cargarProductosCatalogo() {
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = com.piccolifield.sharedpreferences.SplashStorage.obtenerToken(requireContext());
        }
        pbLoadingCatalogo.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.createService();
        apiService.listarProductos(null).enqueue(new Callback<ApiResponse<List<Producto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Producto>>> call, @NonNull Response<ApiResponse<List<Producto>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    pbLoadingCatalogo.setVisibility(View.GONE);
                    listaProductosOriginal.clear();
                    for (Producto p : response.body().getData()) {
                        if (p != null && !"INACTIVO".equalsIgnoreCase(p.getEstado())) {
                            listaProductosOriginal.add(p);
                        }
                    }
                    if (listaProductosOriginal.isEmpty()) {
                        listaProductosOriginal.addAll(response.body().getData());
                    }
                    poblarSpinnersDinamicos();
                    filtrarCatalogo();
                } else {
                    cargarProductosActivosFallback(apiService);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Producto>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                cargarProductosActivosFallback(apiService);
            }
        });
    }

    private void cargarProductosActivosFallback(ApiService apiService) {
        apiService.listarProductos("ACTIVO").enqueue(new Callback<ApiResponse<List<Producto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Producto>>> call, @NonNull Response<ApiResponse<List<Producto>>> response) {
                if (!isAdded()) return;
                pbLoadingCatalogo.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    listaProductosOriginal.clear();
                    listaProductosOriginal.addAll(response.body().getData());
                } else {
                    cargarProductosMock();
                }
                poblarSpinnersDinamicos();
                filtrarCatalogo();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Producto>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                pbLoadingCatalogo.setVisibility(View.GONE);
                cargarProductosMock();
                poblarSpinnersDinamicos();
                filtrarCatalogo();
            }
        });
    }

    private void cargarProductosMock() {
        listaProductosOriginal.clear();
        listaProductosOriginal.add(new Producto(1, "SKU-01", "7750001", "Pisco Italia Piccoli 750ml", "Licores", "Botella 750ml", "ACTIVO", 45.00, 36.00));
        listaProductosOriginal.add(new Producto(2, "SKU-02", "7750002", "Licor de Chocolate Piccoli 750ml", "Cremas", "Botella 750ml", "ACTIVO", 42.00, 33.60));
        listaProductosOriginal.add(new Producto(3, "SKU-03", "7750003", "Crema de Cacao Piccoli 750ml", "Cremas", "Botella 750ml", "ACTIVO", 40.00, 32.00));
        listaProductosOriginal.add(new Producto(4, "SKU-04", "7750004", "Crema de Vainilla Piccoli 750ml", "Cremas", "Botella 750ml", "ACTIVO", 38.00, 30.40));
        listaProductosOriginal.add(new Producto(5, "SKU-05", "7750005", "Vino Tinto Reserva 750ml", "Vinos", "Botella 750ml", "ACTIVO", 39.00, 31.20));
        listaProductosOriginal.add(new Producto(6, "SKU-06", "7750006", "Cocktail Café Profundo 355ml", "Bebidas", "Lata 355ml", "ACTIVO", 14.00, 11.20));
    }

    private void poblarSpinnersDinamicos() {
        Set<String> cats = new LinkedHashSet<>();
        Set<String> pres = new LinkedHashSet<>();

        for (Producto p : listaProductosOriginal) {
            if (p.getCategoria() != null && !p.getCategoria().trim().isEmpty()) {
                cats.add(p.getCategoria().trim());
            }
            if (p.getPresentacion() != null && !p.getPresentacion().trim().isEmpty()) {
                pres.add(p.getPresentacion().trim());
            }
        }

        opcionesCategoria.clear();
        opcionesCategoria.add("Categoría: Todas");
        opcionesCategoria.addAll(cats);
        adapterCategoria.notifyDataSetChanged();

        opcionesPresentacion.clear();
        opcionesPresentacion.add("Presentación: Todas");
        opcionesPresentacion.addAll(pres);
        adapterPresentacion.notifyDataSetChanged();
    }

    private void filtrarCatalogo() {
        String query = etBuscarProductoPedido.getText() != null
                ? etBuscarProductoPedido.getText().toString().trim().toLowerCase()
                : "";

        listaProductosFiltrada.clear();
        for (Producto p : listaProductosOriginal) {
            boolean coincideTexto = query.isEmpty()
                    || p.getProducto().toLowerCase().contains(query)
                    || p.getSku().toLowerCase().contains(query)
                    || p.getCategoria().toLowerCase().contains(query)
                    || p.getPresentacion().toLowerCase().contains(query);

            boolean coincideCat = "TODAS".equalsIgnoreCase(categoriaSeleccionada)
                    || categoriaSeleccionada.equalsIgnoreCase(p.getCategoria());

            boolean coincidePres = "TODAS".equalsIgnoreCase(presentacionSeleccionada)
                    || presentacionSeleccionada.equalsIgnoreCase(p.getPresentacion());

            if (coincideTexto && coincideCat && coincidePres) {
                listaProductosFiltrada.add(p);
            }
        }

        tvContadorCatalogo.setText(listaProductosFiltrada.size() + " productos");
        catalogoAdapter.notifyDataSetChanged();
    }

    private void agregarProductoALista(Producto producto, int cantidad, double precioUnitario) {
        if (producto == null || cantidad <= 0) return;

        boolean encontrado = false;
        for (ItemNuevoPedido item : listaItemsSeleccionados) {
            if (item.getProducto().getIdProductos() == producto.getIdProductos()) {
                item.setCantidad(item.getCantidad() + cantidad);
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            listaItemsSeleccionados.add(new ItemNuevoPedido(producto, cantidad, precioUnitario));
        }

        seleccionadosAdapter.notifyDataSetChanged();
        actualizarResumenListaPedido();
        Toast.makeText(requireContext(), "Agregado: " + cantidad + " x " + producto.getProducto(), Toast.LENGTH_SHORT).show();
    }

    private void actualizarResumenListaPedido() {
        int totalProductos = listaItemsSeleccionados.size();
        int totalUnidades = 0;
        double totalMonto = 0.0;

        for (ItemNuevoPedido item : listaItemsSeleccionados) {
            totalUnidades += item.getCantidad();
            totalMonto += item.getSubtotal();
        }

        tvBadgeCantidadLista.setText(totalProductos + (totalProductos == 1 ? " producto" : " productos"));
        tvTotalUnidadesNuevoPedido.setText(totalUnidades + (totalUnidades == 1 ? " unidad" : " unidades"));
        tvTotalMontoNuevoPedido.setText(String.format(Locale.US, "S/ %.2f", totalMonto));

        if (listaItemsSeleccionados.isEmpty()) {
            tvListaPedidoVacia.setVisibility(View.VISIBLE);
            rvListaNuevoPedido.setVisibility(View.GONE);
        } else {
            tvListaPedidoVacia.setVisibility(View.GONE);
            rvListaNuevoPedido.setVisibility(View.VISIBLE);
        }
    }

    private void registrarNuevoPedidoCompleto() {
        if (listaItemsSeleccionados.isEmpty()) {
            Toast.makeText(requireContext(), "Agregue al menos un producto a la lista del nuevo pedido", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegistrarNuevoPedido.setEnabled(false);

        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        int totalUnidades = 0;
        double totalMonto = 0.0;
        StringBuilder resumenBuilder = new StringBuilder();

        for (int i = 0; i < listaItemsSeleccionados.size(); i++) {
            ItemNuevoPedido item = listaItemsSeleccionados.get(i);
            totalUnidades += item.getCantidad();
            totalMonto += item.getSubtotal();
            if (i == 0) {
                resumenBuilder.append(item.getProducto().getProducto());
            }
        }
        if (listaItemsSeleccionados.size() > 1) {
            resumenBuilder.append(" (+").append(listaItemsSeleccionados.size() - 1).append(" más)");
        }

        int idPedidoGenerado = (int) (System.currentTimeMillis() % 9000) + 1000;
        int primerProductoId = listaItemsSeleccionados.get(0).getProducto().getIdProductos();

        Pedido pedidoResumen = new Pedido(
                idPedidoGenerado,
                totalUnidades,
                fechaHoy,
                idPuntoVenta,
                primerProductoId,
                totalMonto,
                resumenBuilder.toString(),
                criterioNegocio
        );
        pedidoResumen.setEstado("Registrado");

        // Guardar en almacenamiento local para garantizar visualización inmediata
        PuntosDistribucionStorage.guardarPedidoLocal(requireContext(), idPuntoVenta, pedidoResumen);

        // Registrar cada línea del pedido con el backend / repositorio offline
        PedidoRepository repository = new PedidoRepository(requireContext());
        final int totalItems = listaItemsSeleccionados.size();
        final int[] completados = {0};

        for (ItemNuevoPedido item : listaItemsSeleccionados) {
            Pedido pedidoIndividual = new Pedido(
                    0,
                    item.getCantidad(),
                    fechaHoy,
                    idPuntoVenta,
                    item.getProducto().getIdProductos()
            );

            repository.registrarPedido(pedidoIndividual, new PedidoRepository.PedidoCallback() {
                @Override
                public void onSuccess() {
                    verificarFinalizacion();
                }

                @Override
                public void onError(String mensaje) {
                    verificarFinalizacion();
                }

                @Override
                public void onOfflineSaved() {
                    verificarFinalizacion();
                }

                private void verificarFinalizacion() {
                    completados[0]++;
                    if (completados[0] >= totalItems && isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            btnRegistrarNuevoPedido.setEnabled(true);
                            Toast.makeText(requireContext(), "Nuevo pedido registrado con éxito", Toast.LENGTH_SHORT).show();
                            if (onPedidoRegistrado != null) {
                                onPedidoRegistrado.run();
                            }
                            cerrarFragment();
                        });
                    }
                }
            });
        }
    }

    private void cerrarFragment() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
}
