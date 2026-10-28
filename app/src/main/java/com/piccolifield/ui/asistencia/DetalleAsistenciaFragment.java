package com.piccolifield.ui.asistencia;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.piccolifield.data.remote.dto.request.RevisionJustificacionRequest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.piccolifield.R;
import com.piccolifield.adapter.TurnoHistorialAdapter;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.model.TurnoHistorial;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentDetalleAsistenciaBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleAsistenciaFragment extends Fragment {

    private static final String ARG_REGISTRO = "registro_asistencia";
    private FragmentDetalleAsistenciaBinding binding;
    private RegistroAsistencia registro;
    private ApiService apiService;

    public DetalleAsistenciaFragment() {
        // Required empty public constructor
    }

    public static DetalleAsistenciaFragment newInstance(RegistroAsistencia item) {
        DetalleAsistenciaFragment fragment = new DetalleAsistenciaFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REGISTRO, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            registro = (RegistroAsistencia) getArguments().getSerializable(ARG_REGISTRO);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetalleAsistenciaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }
        apiService = RetrofitClient.createService();

        cargarMapaEstatico();

        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        if (registro != null) {
            binding.tvUserName.setText(registro.getNombreCompleto());
            binding.tvUserLocation.setText(registro.getSede());

            String horaDisp = registro.getHoraMarcado();
            if (horaDisp != null && horaDisp.length() >= 16) {
                binding.tvAttendanceTime.setText(horaDisp.substring(11, 16));
            } else if (horaDisp != null) {
                binding.tvAttendanceTime.setText(horaDisp);
            } else {
                binding.tvAttendanceTime.setText("--:--");
            }

            String horaServidor = registro.getFechaHoraServidor();
            if (horaServidor != null && horaServidor.length() >= 19) {
                binding.tvServerTime.setText(horaServidor.substring(11, 19));
            } else if (horaServidor != null) {
                binding.tvServerTime.setText(horaServidor);
            } else {
                binding.tvServerTime.setText("--:--");
            }

            String tipo = registro.getTipo();
            binding.tvStatusTitle.setText(tipo != null && !tipo.isEmpty() ? tipo : "INGRESO");

            int minTardanza = registro.getMinutosTardanza();
            binding.tvTardanzaMinutes.setText(minTardanza > 0 ? minTardanza + " min" : "Sin tardanza");

            configurarBadge(binding.tvUserStatusBadge, registro.getEstado());
            cargarJustificacionAdmin();
            configurarControlesRevisionAdmin();
            cargarHistorialTurnos(registro.getIdUsuario());
        }
    }

    private void configurarControlesRevisionAdmin() {
        if (binding == null || registro == null) return;

        String rol = SplashStorage.obtenerRol(requireContext());
        if ("ADMINISTRADOR".equalsIgnoreCase(rol)) {
            binding.layoutControlesRevisionAdmin.setVisibility(View.VISIBLE);

            binding.btnAprobarJustificacion.setOnClickListener(v -> procesarRevision("APROBADA"));
            binding.btnRechazarJustificacion.setOnClickListener(v -> procesarRevision("RECHAZADA"));
        } else {
            binding.layoutControlesRevisionAdmin.setVisibility(View.GONE);
        }
    }

    private void procesarRevision(String estadoNuevo) {
        if (binding == null || registro == null) return;

        String comentario = binding.etComentarioRevisionAdmin.getText() != null ?
                binding.etComentarioRevisionAdmin.getText().toString().trim() : "";

        int idRevisor = SplashStorage.obtenerUsuarioId(requireContext());
        int idUsuarioColaborador = registro.getIdUsuario();

        RevisionJustificacionRequest request = new RevisionJustificacionRequest(
                idUsuarioColaborador, idRevisor, estadoNuevo, comentario
        );

        binding.btnAprobarJustificacion.setEnabled(false);
        binding.btnRechazarJustificacion.setEnabled(false);

        actualizarEstadoBadgeJustificacion(estadoNuevo);

        if (apiService != null) {
            apiService.revisarJustificacion(request).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                    if (!isAdded() || binding == null) return;
                    binding.btnAprobarJustificacion.setEnabled(true);
                    binding.btnRechazarJustificacion.setEnabled(true);

                    String msj = "APROBADA".equalsIgnoreCase(estadoNuevo) ? "Justificación Aprobada" : "Justificación Rechazada";
                    Toast.makeText(getContext(), msj + " exitosamente", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                    if (!isAdded() || binding == null) return;
                    binding.btnAprobarJustificacion.setEnabled(true);
                    binding.btnRechazarJustificacion.setEnabled(true);

                    String msj = "APROBADA".equalsIgnoreCase(estadoNuevo) ? "Justificación Aprobada localmente" : "Justificación Rechazada localmente";
                    Toast.makeText(getContext(), msj, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void actualizarEstadoBadgeJustificacion(String estadoNuevo) {
        if (binding == null) return;
        binding.tvEstadoJustificacionBadgeAdmin.setText(estadoNuevo);
        if ("APROBADA".equalsIgnoreCase(estadoNuevo)) {
            binding.tvEstadoJustificacionBadgeAdmin.setBackgroundResource(R.drawable.bg_badge_on_time);
            binding.tvEstadoJustificacionBadgeAdmin.setTextColor(requireContext().getColor(R.color.status_on_time_text));
        } else if ("RECHAZADA".equalsIgnoreCase(estadoNuevo)) {
            binding.tvEstadoJustificacionBadgeAdmin.setBackgroundResource(R.drawable.bg_badge_absent);
            binding.tvEstadoJustificacionBadgeAdmin.setTextColor(requireContext().getColor(R.color.status_absent_text));
        }
    }

    private void cargarJustificacionAdmin() {
        if (registro == null || binding == null) return;

        // Si el objeto ya tiene la información de la justificación
        if (registro.getJustificacion() != null && !registro.getJustificacion().isEmpty()) {
            binding.cardJustificacionAdmin.setVisibility(View.VISIBLE);
            binding.tvMotivoJustificacionAdmin.setText("Motivo: " + (registro.getMotivoJustificacion() != null ? registro.getMotivoJustificacion() : "Inasistencia"));
            binding.tvDetalleJustificacionAdmin.setText("Detalle: " + registro.getJustificacion());
            mostrarFotoSustento(registro.getRutaSustento());
            return;
        }

        // Si no está cargada pero hay un ID de usuario, la solicitamos a la API
        if (apiService != null && registro.getIdUsuario() > 0) {
            apiService.obtenerJustificacionUsuario(registro.getIdUsuario()).enqueue(new Callback<ApiResponse<RegistroAsistencia>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<RegistroAsistencia>> call, @NonNull Response<ApiResponse<RegistroAsistencia>> response) {
                    if (!isAdded() || binding == null) return;

                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        RegistroAsistencia resJust = response.body().getData();
                        if (resJust.getJustificacion() != null && !resJust.getJustificacion().isEmpty()) {
                            binding.cardJustificacionAdmin.setVisibility(View.VISIBLE);
                            binding.tvMotivoJustificacionAdmin.setText("Motivo: " + (resJust.getMotivoJustificacion() != null ? resJust.getMotivoJustificacion() : "Inasistencia"));
                            binding.tvDetalleJustificacionAdmin.setText("Detalle: " + resJust.getJustificacion());
                            mostrarFotoSustento(resJust.getRutaSustento());
                        } else {
                            mostrarJustificacionPorDefectoOEstado();
                        }
                    } else {
                        mostrarJustificacionPorDefectoOEstado();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<RegistroAsistencia>> call, @NonNull Throwable t) {
                    if (!isAdded() || binding == null) return;
                    mostrarJustificacionPorDefectoOEstado();
                }
            });
        } else {
            mostrarJustificacionPorDefectoOEstado();
        }
    }

    private void mostrarFotoSustento(String rutaSustento) {
        if (binding == null) return;
        if (rutaSustento == null || rutaSustento.trim().isEmpty()) {
            binding.layoutFotoSustentoAdmin.setVisibility(View.GONE);
            return;
        }

        binding.layoutFotoSustentoAdmin.setVisibility(View.VISIBLE);

        try {
            if (rutaSustento.startsWith("content://") || rutaSustento.startsWith("file://")) {
                binding.ivFotoSustentoAdmin.setImageURI(Uri.parse(rutaSustento));
            } else {
                byte[] decodedBytes = Base64.decode(rutaSustento, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    binding.ivFotoSustentoAdmin.setImageBitmap(bitmap);
                } else {
                    binding.layoutFotoSustentoAdmin.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            binding.layoutFotoSustentoAdmin.setVisibility(View.GONE);
        }
    }

    private void mostrarJustificacionPorDefectoOEstado() {
        if (registro == null || binding == null) return;

        // Si el estado es FALTA o TARDANZA, mostrar aviso de justificación pendiente o sin registro
        String estado = registro.getEstado();
        if ("FALTA".equalsIgnoreCase(estado) || "PERMISO".equalsIgnoreCase(estado)) {
            binding.cardJustificacionAdmin.setVisibility(View.VISIBLE);
            binding.tvMotivoJustificacionAdmin.setText("Motivo: Justificación no registrada");
            binding.tvDetalleJustificacionAdmin.setText("El colaborador aún no ha enviado una justificación formal para esta falta.");
            binding.tvEstadoJustificacionBadgeAdmin.setText("SIN REGISTRO");
            binding.layoutFotoSustentoAdmin.setVisibility(View.GONE);
        } else {
            binding.cardJustificacionAdmin.setVisibility(View.GONE);
        }
    }

    private void cargarMapaEstatico() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.miniMapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                if (registro == null) return;

                double lat = registro.getLatitud();
                double lng = registro.getLongitud();

                // Validación: Si las coordenadas vienen en 0 (océano), forzamos las de Chiclayo
                if (lat == 0.0 && lng == 0.0) {
                    lat = -6.7714;
                    lng = -79.8409;
                }

                LatLng ubicacion = new LatLng(lat, lng);

                googleMap.clear();
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                // Crear el marcador verde personalizado de forma nativa
                BitmapDescriptor iconoPersonalizado = obtenerIconoVectorial(R.drawable.ic_location_pin);

                if (iconoPersonalizado != null) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(ubicacion)
                            .icon(iconoPersonalizado)
                            .title(registro.getSede()));
                } else {
                    googleMap.addMarker(new MarkerOptions().position(ubicacion).title(registro.getSede()));
                }

                // Ajustamos el zoom a 16f para que las calles sean bien visibles
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16f));
            });
        }
    }

    // Método para convertir tu ícono SVG/XML a un marcador válido de Google Maps
    private BitmapDescriptor obtenerIconoVectorial(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(requireContext(), vectorResId);
        if (vectorDrawable == null) return null;

        // Le aplicamos el color verde que usabas en el tint del XML
        vectorDrawable.setTint(Color.parseColor("#00B050"));

        int ancho = vectorDrawable.getIntrinsicWidth() > 0 ? vectorDrawable.getIntrinsicWidth() : 100;
        int alto = vectorDrawable.getIntrinsicHeight() > 0 ? vectorDrawable.getIntrinsicHeight() : 100;

        Bitmap bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void cargarHistorialTurnos(int idUsuario) {
        if (apiService == null || idUsuario == 0) return;

        apiService.obtenerHistorialTurnos(idUsuario).enqueue(new Callback<ApiResponse<List<TurnoHistorial>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<TurnoHistorial>>> call, @NonNull Response<ApiResponse<List<TurnoHistorial>>> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<TurnoHistorial> historial = response.body().getData();
                    binding.rvShiftHistory.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.rvShiftHistory.setAdapter(new TurnoHistorialAdapter(historial));
                } else {
                    Log.e("DetalleAsistencia", "No se pudo cargar historial: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<TurnoHistorial>>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                Log.e("DetalleAsistencia", "Fallo red historial: " + t.getMessage());
            }
        });
    }

    private void configurarBadge(TextView badge, String estado) {
        if (badge == null) return;
        if (estado == null) {
            badge.setText("Desconocido");
            return;
        }

        switch (estado.toUpperCase()) {
            case "A_TIEMPO":
                badge.setBackgroundResource(R.drawable.bg_badge_on_time);
                badge.setTextColor(requireContext().getColor(R.color.status_on_time_text));
                badge.setText("A tiempo");
                break;
            case "TARDANZA":
                badge.setBackgroundResource(R.drawable.bg_badge_late);
                badge.setTextColor(requireContext().getColor(R.color.status_late_text));
                badge.setText("Tardanza");
                break;
            case "FALTA":
                badge.setBackgroundResource(R.drawable.bg_badge_absent);
                badge.setTextColor(requireContext().getColor(R.color.status_absent_text));
                badge.setText("Falta");
                break;
            default:
                badge.setBackgroundResource(R.drawable.bg_badge_permission);
                badge.setTextColor(requireContext().getColor(R.color.status_permission_text));
                badge.setText(estado);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}