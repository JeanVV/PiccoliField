package com.piccolifield;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.piccolifield.adapter.PedidoAdapter;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Pedido;
import com.piccolifield.model.PuntoDistribucion;
import com.piccolifield.sharedpreferences.PuntosDistribucionStorage;
import com.piccolifield.ui.despacho.EstadoPedidoBottomSheet;
import com.piccolifield.ui.despacho.NuevoPedidoFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallePuntoDistribucionActivity extends AppCompatActivity {

    private ImageView btnBack, ivHeroPuntoFoto;
    private TextView tvBadgeNumero, tvNombreTienda, tvCodigoDetalleSub, tvTipoNegocioBadge, tvEstadoBadge;
    private TextView tvDireccionVal, tvDistanciaVal, tvPedidoNumVal, tvRepartidorVal, btnNuevoPedido, tvPedidosSub;
    private MaterialButton btnVerRuta;
    private RecyclerView rvPedidos;
    private FrameLayout fragmentContainerDetalle;

    private PedidoAdapter pedidoAdapter;
    private final List<Pedido> listaPedidos = new ArrayList<>();

    private int idPuntoVenta;
    private String nombrePuntoVenta;
    private String codigoPuntoVenta;
    private String tipoNegocioPunto;
    private String direccionPunto;
    private String distanciaPunto;
    private String estadoPunto;
    private String fotoUrlPunto;
    private int badgeNumero = 1;
    private PuntoDistribucion puntoDistribucionInfo;

    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_punto_distribucion);

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

        // 0. Asegurar token de sesión
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = com.piccolifield.sharedpreferences.SplashStorage.obtenerToken(this);
        }

        // 1. Vincular Vistas
        btnBack = findViewById(R.id.btnBack);
        ivHeroPuntoFoto = findViewById(R.id.ivHeroPuntoFoto);
        pbLoading = findViewById(R.id.pbLoading);
        tvBadgeNumero = findViewById(R.id.tvBadgeNumero);
        tvNombreTienda = findViewById(R.id.tvNombreTienda);
        tvCodigoDetalleSub = findViewById(R.id.tvCodigoDetalleSub);
        tvTipoNegocioBadge = findViewById(R.id.tvTipoNegocioBadge);
        tvEstadoBadge = findViewById(R.id.tvEstadoBadge);
        tvDireccionVal = findViewById(R.id.tvDireccionVal);
        tvDistanciaVal = findViewById(R.id.tvDistanciaVal);
        tvPedidoNumVal = findViewById(R.id.tvPedidoNumVal);
        tvRepartidorVal = findViewById(R.id.tvRepartidorVal);
        btnNuevoPedido = findViewById(R.id.btnNuevoPedido);
        tvPedidosSub = findViewById(R.id.tvPedidosSub);
        btnVerRuta = findViewById(R.id.btnVerRuta);
        rvPedidos = findViewById(R.id.rvPedidos);
        fragmentContainerDetalle = findViewById(R.id.fragmentContainerDetalle);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (fragmentContainerDetalle != null) {
                boolean hayFragmentActivo = getSupportFragmentManager().getBackStackEntryCount() > 0;
                fragmentContainerDetalle.setVisibility(hayFragmentActivo ? View.VISIBLE : View.GONE);
            }
        });

        rvPedidos.setLayoutManager(new LinearLayoutManager(this));
        pedidoAdapter = new PedidoAdapter(listaPedidos, pedido -> {
            EstadoPedidoBottomSheet bottomSheet = new EstadoPedidoBottomSheet(pedido, () -> {
                cargarPedidos(idPuntoVenta);
            });
            bottomSheet.show(getSupportFragmentManager(), "EstadoPedido");
        });
        rvPedidos.setAdapter(pedidoAdapter);

        // 2. Recibir los datos enviados por Intent
        if (getIntent() != null) {
            idPuntoVenta = getIntent().getIntExtra("ID_PDV", -1);
            nombrePuntoVenta = getIntent().getStringExtra("NOMBRE_PDV");
            codigoPuntoVenta = getIntent().getStringExtra("CODIGO_PDV");
            tipoNegocioPunto = getIntent().getStringExtra("TIPO_NEGOCIO");
            direccionPunto = getIntent().getStringExtra("DIRECCION");
            distanciaPunto = getIntent().getStringExtra("DISTANCIA");
            estadoPunto = getIntent().getStringExtra("ESTADO_PDV");
            fotoUrlPunto = getIntent().getStringExtra("FOTO_URL_PDV");
            badgeNumero = getIntent().getIntExtra("BADGE_NUMERO", -1);
            if (badgeNumero <= 0) {
                String badgeStr = getIntent().getStringExtra("BADGE_NUMERO");
                try {
                    badgeNumero = (badgeStr != null && !badgeStr.trim().isEmpty())
                            ? Integer.parseInt(badgeStr.trim())
                            : Math.max(1, idPuntoVenta);
                } catch (NumberFormatException e) {
                    badgeNumero = Math.max(1, idPuntoVenta);
                }
            }

            if (codigoPuntoVenta == null || codigoPuntoVenta.trim().isEmpty()) {
                codigoPuntoVenta = String.format(Locale.getDefault(), "PDV-%04d", Math.max(1, idPuntoVenta));
            }
            if (tipoNegocioPunto == null || tipoNegocioPunto.trim().isEmpty()) {
                tipoNegocioPunto = (idPuntoVenta % 2 == 0) ? "Mayorista" : "Minorista";
            }

            actualizarVistaDetalle();

            pbLoading.setVisibility(View.VISIBLE);
            cargarHeroFotoPunto();
            cargarDetallePunto(idPuntoVenta);
            cargarPedidos(idPuntoVenta);
        }

        // 3. Listener del botón atrás
        btnBack.setOnClickListener(v -> finish());

        // 4. Listener Nuevo Pedido -> Abrir NuevoPedidoFragment con cartillas de productos y precios Mayorista/Minorista
        if (btnNuevoPedido != null) {
            btnNuevoPedido.setOnClickListener(v -> abrirFragmentNuevoPedido());
        }

        // 5. Listener Ver Ruta de Entrega
        if (btnVerRuta != null) {
            btnVerRuta.setOnClickListener(v ->
                    Toast.makeText(this, "Navegando a ruta de entrega: " + (nombrePuntoVenta != null ? nombrePuntoVenta : ""), Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void abrirFragmentNuevoPedido() {
        if (fragmentContainerDetalle != null) {
            fragmentContainerDetalle.setVisibility(View.VISIBLE);
        }
        NuevoPedidoFragment fragment = NuevoPedidoFragment.newInstance(
                idPuntoVenta,
                nombrePuntoVenta != null ? nombrePuntoVenta : "Punto de Distribución",
                codigoPuntoVenta != null ? codigoPuntoVenta : String.format(Locale.getDefault(), "PDV-%04d", Math.max(1, idPuntoVenta)),
                tipoNegocioPunto != null ? tipoNegocioPunto : "Minorista"
        );
        fragment.setOnPedidoRegistradoListener(() -> cargarPedidos(idPuntoVenta));

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainerDetalle, fragment)
                .addToBackStack("NuevoPedido")
                .commit();
    }

    private void actualizarVistaDetalle() {
        String nombreMostrar = (nombrePuntoVenta != null && !nombrePuntoVenta.trim().isEmpty())
                ? nombrePuntoVenta.trim()
                : "Punto #" + Math.max(1, idPuntoVenta);
        String codigoMostrar = (codigoPuntoVenta != null && !codigoPuntoVenta.trim().isEmpty())
                ? codigoPuntoVenta.trim()
                : String.format(Locale.getDefault(), "PDV-%04d", Math.max(1, idPuntoVenta));
        String criterioMostrar = (tipoNegocioPunto != null && tipoNegocioPunto.toLowerCase(Locale.ROOT).contains("mayor"))
                ? "Mayorista"
                : "Minorista";

        tvNombreTienda.setText(nombreMostrar);
        tvBadgeNumero.setText(String.valueOf(badgeNumero > 0 ? badgeNumero : Math.max(1, idPuntoVenta)));

        if (tvCodigoDetalleSub != null) {
            tvCodigoDetalleSub.setText("Código: " + codigoMostrar);
        }
        if (tvPedidoNumVal != null) {
            tvPedidoNumVal.setText(codigoMostrar);
        }
        if (tvTipoNegocioBadge != null) {
            tvTipoNegocioBadge.setText(criterioMostrar);
        }
        if (tvRepartidorVal != null) {
            tvRepartidorVal.setText(criterioMostrar);
        }
        if (tvDireccionVal != null && direccionPunto != null && !direccionPunto.trim().isEmpty()) {
            tvDireccionVal.setText(direccionPunto);
        }
        if (tvDistanciaVal != null && distanciaPunto != null && !distanciaPunto.trim().isEmpty()) {
            tvDistanciaVal.setText(distanciaPunto);
        }
        if (tvEstadoBadge != null && estadoPunto != null && !estadoPunto.trim().isEmpty()) {
            tvEstadoBadge.setText(estadoPunto);
        }
    }

    private void cargarDetallePunto(int id) {
        PuntoDistribucion local = PuntosDistribucionStorage.buscarPuntoLocalPorId(this, id);
        if (local != null) {
            aplicarDatosPunto(local);
        }

        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerDetallePunto(id).enqueue(new Callback<ApiResponse<PuntoDistribucion>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PuntoDistribucion>> call, @NonNull Response<ApiResponse<PuntoDistribucion>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    puntoDistribucionInfo = response.body().getData();
                    aplicarDatosPunto(puntoDistribucionInfo);
                } else {
                    cargarDetalleSupervisionFallback(apiService, id);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PuntoDistribucion>> call, @NonNull Throwable t) {
                cargarDetalleSupervisionFallback(apiService, id);
            }
        });
    }

    private void cargarDetalleSupervisionFallback(ApiService apiService, int id) {
        apiService.obtenerDetallePuntoVentaSupervision(id).enqueue(new Callback<ApiResponse<com.piccolifield.data.model.PuntoVentaDetalle>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<com.piccolifield.data.model.PuntoVentaDetalle>> call, @NonNull Response<ApiResponse<com.piccolifield.data.model.PuntoVentaDetalle>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    com.piccolifield.data.model.PuntoVentaDetalle d = response.body().getData();
                    if (d.getPuntoVenta() != null && !d.getPuntoVenta().trim().isEmpty()) {
                        nombrePuntoVenta = d.getPuntoVenta().trim();
                    }
                    if (d.getCodigo() != null && !d.getCodigo().trim().isEmpty()) {
                        codigoPuntoVenta = d.getCodigo().trim();
                    }
                    if (d.getTipo() != null && !d.getTipo().trim().isEmpty()) {
                        tipoNegocioPunto = d.getTipo().trim().toUpperCase(Locale.ROOT).contains("MAYOR") ? "Mayorista" : "Minorista";
                    }
                    if (d.getDireccion() != null && !d.getDireccion().trim().isEmpty()) {
                        direccionPunto = d.getDireccion().trim();
                    }
                    if (d.getEstado() != null && !d.getEstado().trim().isEmpty()) {
                        estadoPunto = d.getEstado().trim();
                    }
                }
                actualizarVistaDetalle();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<com.piccolifield.data.model.PuntoVentaDetalle>> call, @NonNull Throwable t) {
                actualizarVistaDetalle();
            }
        });
    }

    private void aplicarDatosPunto(PuntoDistribucion pdv) {
        if (pdv == null) return;
        if (pdv.getNombre() != null && !pdv.getNombre().trim().isEmpty()) {
            nombrePuntoVenta = pdv.getNombre().trim();
        }
        if (pdv.getCodigo() != null && !pdv.getCodigo().trim().isEmpty()) {
            codigoPuntoVenta = pdv.getCodigo().trim();
        }
        if (pdv.getCriterioNegocio() != null && !pdv.getCriterioNegocio().trim().isEmpty()) {
            tipoNegocioPunto = pdv.getCriterioNegocio().trim();
        }
        if (pdv.getDireccion() != null && !pdv.getDireccion().trim().isEmpty()) {
            direccionPunto = pdv.getDireccion().trim();
        }
        if (pdv.getEstado() != null && !pdv.getEstado().trim().isEmpty()) {
            estadoPunto = pdv.getEstado().trim();
        }
        if (pdv.getFotoUrl() != null && !pdv.getFotoUrl().trim().isEmpty()) {
            fotoUrlPunto = pdv.getFotoUrl().trim();
        }
        actualizarVistaDetalle();
        cargarHeroFotoPunto();
    }

    private void cargarHeroFotoPunto() {
        if (ivHeroPuntoFoto == null) return;

        android.graphics.Bitmap cachedPdv = com.piccolifield.adapter.PuntoDistribucionAdapter.obtenerFotoCache(idPuntoVenta);
        if (cachedPdv != null) {
            ivHeroPuntoFoto.setImageBitmap(cachedPdv);
            return;
        }

        int idProspectoOrigen = obtenerIdProspectoDesdeCodigo();
        if (idProspectoOrigen > 0) {
            android.graphics.Bitmap cachedProspecto = com.piccolifield.adapter.ProspectoAdapter.obtenerFotoCache(idProspectoOrigen);
            if (cachedProspecto == null) {
                cachedProspecto = com.piccolifield.util.UserPhotoLoader.cargarFotoProspectoLocal(this, idProspectoOrigen);
                if (cachedProspecto != null) {
                    com.piccolifield.adapter.ProspectoAdapter.guardarFotoCache(idProspectoOrigen, cachedProspecto);
                }
            }
            if (cachedProspecto != null) {
                com.piccolifield.adapter.PuntoDistribucionAdapter.guardarFotoCache(idPuntoVenta, cachedProspecto);
                ivHeroPuntoFoto.setImageBitmap(cachedProspecto);
                return;
            }
        }

        if (fotoUrlPunto != null && !fotoUrlPunto.trim().isEmpty()) {
            android.graphics.Bitmap b64 = com.piccolifield.util.UserPhotoLoader.decodeBase64OrDataUri(fotoUrlPunto);
            if (b64 != null) {
                com.piccolifield.adapter.PuntoDistribucionAdapter.guardarFotoCache(idPuntoVenta, b64);
                ivHeroPuntoFoto.setImageBitmap(b64);
                return;
            }
            String fullUrl = com.piccolifield.util.UserPhotoLoader.buildFullImageUrl(fotoUrlPunto);
            if (fullUrl != null) {
                descargarHeroFotoUrl(fullUrl, idProspectoOrigen);
                return;
            }
        }

        if (idProspectoOrigen > 0) {
            RetrofitClient.createService().obtenerProspecto(idProspectoOrigen)
                    .enqueue(new Callback<ApiResponse<com.piccolifield.model.Prospecto>>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponse<com.piccolifield.model.Prospecto>> call,
                                               @NonNull Response<ApiResponse<com.piccolifield.model.Prospecto>> response) {
                            if (isFinishing() || isDestroyed()) return;
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                String pFoto = response.body().getData().getFotoUrl();
                                if (pFoto != null && !pFoto.trim().isEmpty()) {
                                    android.graphics.Bitmap b64 = com.piccolifield.util.UserPhotoLoader.decodeBase64OrDataUri(pFoto);
                                    if (b64 != null) {
                                        com.piccolifield.adapter.PuntoDistribucionAdapter.guardarFotoCache(idPuntoVenta, b64);
                                        com.piccolifield.adapter.ProspectoAdapter.guardarFotoCache(idProspectoOrigen, b64);
                                        ivHeroPuntoFoto.setImageBitmap(b64);
                                        return;
                                    }
                                    String fullUrl = com.piccolifield.util.UserPhotoLoader.buildFullImageUrl(pFoto);
                                    if (fullUrl != null) {
                                        descargarHeroFotoUrl(fullUrl, idProspectoOrigen);
                                        return;
                                    }
                                }
                            }
                            descargarHeroFotoUrl(
                                    RetrofitClient.BASE_SERVER_URL + "api/prospectos/" + idProspectoOrigen + "/foto",
                                    idProspectoOrigen
                            );
                        }

                        @Override
                        public void onFailure(@NonNull Call<ApiResponse<com.piccolifield.model.Prospecto>> call, @NonNull Throwable t) {
                        }
                    });
        } else if (idPuntoVenta > 0) {
            descargarHeroFotoUrl(
                    RetrofitClient.BASE_SERVER_URL + "api/puntos-venta/" + idPuntoVenta + "/foto",
                    0
            );
        }
    }

    private void descargarHeroFotoUrl(String url, int idProspectoOrigen) {
        RetrofitClient.createService().descargarArchivoPorUrl(url)
                .enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Response<okhttp3.ResponseBody> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            android.graphics.Bitmap bmp = com.piccolifield.util.UserPhotoLoader.decodeBitmapFromResponseBody(response.body());
                            if (bmp != null) {
                                com.piccolifield.adapter.PuntoDistribucionAdapter.guardarFotoCache(idPuntoVenta, bmp);
                                if (idProspectoOrigen > 0) {
                                    com.piccolifield.adapter.ProspectoAdapter.guardarFotoCache(idProspectoOrigen, bmp);
                                    com.piccolifield.util.UserPhotoLoader.guardarFotoProspectoLocal(DetallePuntoDistribucionActivity.this, idProspectoOrigen, bmp);
                                }
                                ivHeroPuntoFoto.post(() -> ivHeroPuntoFoto.setImageBitmap(bmp));
                                return;
                            }
                        }
                        String pdvEndpoint = RetrofitClient.BASE_SERVER_URL
                                + "api/puntos-venta/" + idPuntoVenta + "/foto";
                        if (idPuntoVenta > 0 && !pdvEndpoint.equals(url)) {
                            descargarHeroFotoUrl(pdvEndpoint, idProspectoOrigen);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        String pdvEndpoint = RetrofitClient.BASE_SERVER_URL
                                + "api/puntos-venta/" + idPuntoVenta + "/foto";
                        if (idPuntoVenta > 0 && !pdvEndpoint.equals(url)) {
                            descargarHeroFotoUrl(pdvEndpoint, idProspectoOrigen);
                        }
                    }
                });
    }

    private int obtenerIdProspectoDesdeCodigo() {
        if (codigoPuntoVenta != null && codigoPuntoVenta.toUpperCase(Locale.ROOT).startsWith("DIST-")) {
            try {
                return Integer.parseInt(codigoPuntoVenta.substring(5).trim());
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    private void cargarPedidos(int idPdv) {
        List<Pedido> locales = PuntosDistribucionStorage.obtenerPedidosLocales(this, idPdv);

        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerPedidosPorPdv(idPdv).enqueue(new Callback<ApiResponse<List<Pedido>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Pedido>>> call, @NonNull Response<ApiResponse<List<Pedido>>> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    combinarYMostrarPedidos(locales, response.body().getData(), false);
                } else {
                    cargarPedidosConFallback(locales);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Pedido>>> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                cargarPedidosConFallback(locales);
            }
        });
    }

    private void combinarYMostrarPedidos(List<Pedido> locales, List<Pedido> remotos, boolean usarMockSiVacio) {
        listaPedidos.clear();
        if (locales != null) {
            listaPedidos.addAll(locales);
        }
        if (remotos != null) {
            for (Pedido r : remotos) {
                boolean existe = false;
                for (Pedido l : listaPedidos) {
                    if (l.getIdPedido() == r.getIdPedido()) {
                        existe = true;
                        break;
                    }
                }
                if (!existe) {
                    listaPedidos.add(r);
                }
            }
        }
        if (listaPedidos.isEmpty() && usarMockSiVacio) {
            listaPedidos.add(new Pedido(1001, 10, "12 Oct 2023", idPuntoVenta, 1));
            listaPedidos.add(new Pedido(1002, 5, "10 Oct 2023", idPuntoVenta, 2));
        }
        pedidoAdapter.notifyDataSetChanged();
        tvPedidosSub.setText(listaPedidos.size() + " pedidos registrados");
    }

    private void cargarPedidosConFallback(List<Pedido> locales) {
        combinarYMostrarPedidos(locales, null, true);
    }
}
