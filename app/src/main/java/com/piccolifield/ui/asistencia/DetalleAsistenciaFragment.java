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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.piccolifield.data.remote.Resource;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.dto.request.RevisionJustificacionRequest;
import com.piccolifield.databinding.FragmentDetalleAsistenciaBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.util.List;

public class DetalleAsistenciaFragment extends Fragment {

    private static final String ARG_REGISTRO = "registro_asistencia";
    private FragmentDetalleAsistenciaBinding binding;
    private RegistroAsistencia registro;
    private AsistenciaViewModel viewModel;

    private final androidx.activity.result.ActivityResultLauncher<String> pickUserPhotoLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && registro != null && registro.getIdUsuario() > 0 && binding != null) {
                    Toast.makeText(getContext(), "Actualizando foto del colaborador...", Toast.LENGTH_SHORT).show();
                    com.piccolifield.util.UserPhotoLoader.subirFotoUsuario(
                            requireContext(),
                            registro.getIdUsuario(),
                            uri,
                            false,
                            new com.piccolifield.util.UserPhotoLoader.OnFotoActualizadaListener() {
                                @Override
                                public void onSuccess(Bitmap nuevoBitmap, String nuevaUrlFoto, String mensaje) {
                                    if (!isAdded() || binding == null) return;
                                    registro.setFotoPerfil(nuevaUrlFoto);
                                    binding.ivUserPhoto.setImageBitmap(nuevoBitmap);
                                    Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(String mensajeError) {
                                    if (!isAdded()) return;
                                    Toast.makeText(getContext(), mensajeError, Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                }
            });

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

        viewModel = new ViewModelProvider(requireActivity()).get(AsistenciaViewModel.class);
        viewModel.resetRevisionJustificacionLiveData();
        observarViewModel();

        cargarMapaEstatico();

        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        if (registro != null) {
            binding.tvUserName.setText(registro.getNombreCompleto());
            binding.tvUserLocation.setText(registro.getSede());

            com.piccolifield.util.UserPhotoLoader.loadUserPhoto(
                    requireContext(),
                    registro.getIdUsuario(),
                    registro.getFotoPerfil(),
                    registro.getNombreCompleto(),
                    binding.ivUserPhoto,
                    R.drawable.ic_avatar_placeholder
            );

            binding.ivUserPhoto.setOnClickListener(v -> {
                if (registro.getIdUsuario() > 0) {
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Actualizar foto de " + registro.getNombreCompleto())
                            .setItems(new String[]{"Elegir nueva foto de la galería"}, (d, w) -> pickUserPhotoLauncher.launch("image/*"))
                            .setNegativeButton("Cancelar", null)
                            .show();
                }
            });

            String horaDisp = registro.getHoraMarcado();
            String horaServidor = registro.getFechaHoraServidor();

            String fmtDisp = formatearHoraCorta(horaDisp);
            String fmtServ = formatearHoraCorta(horaServidor);

            if ("--:--".equals(fmtDisp) && !"--:--".equals(fmtServ)) fmtDisp = fmtServ;
            if ("--:--".equals(fmtServ) && !"--:--".equals(fmtDisp)) fmtServ = fmtDisp;

            binding.tvAttendanceTime.setText(fmtDisp);
            binding.tvServerTime.setText(fmtServ);

            String fechaRaw = horaServidor != null && !horaServidor.isEmpty() ? horaServidor : horaDisp;
            if (fechaRaw == null || fechaRaw.isEmpty()) fechaRaw = registro.getFechaJustificacion();
            binding.tvAttendanceDate.setText(formatearFechaLarga(fechaRaw));

            if (registro.getIdUsuario() > 0) {
                viewModel.cargarUltimaAsistencia(registro.getIdUsuario());
            }

            String tipo = registro.getTipo();
            binding.tvStatusTitle.setText(tipo != null && !tipo.isEmpty() ? tipo : "INGRESO");

            int minTardanza = registro.getMinutosTardanza();
            binding.tvTardanzaMinutes.setText(minTardanza > 0 ? minTardanza + " min" : "Sin tardanza");

            configurarBadge(binding.tvUserStatusBadge, registro.getEstado());
            cargarJustificacionAdmin();
            configurarControlesRevisionAdmin();
            viewModel.cargarHistorialTurnos(registro.getIdUsuario());
        }
    }

    private void observarViewModel() {
        // Observador de Revisión de Justificación
        viewModel.getRevisionJustificacionLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (!isAdded() || binding == null || resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.btnAprobarJustificacion.setEnabled(false);
                    binding.btnRechazarJustificacion.setEnabled(false);
                    if (binding.pbLoadingDetalle != null) binding.pbLoadingDetalle.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    if (binding.pbLoadingDetalle != null) binding.pbLoadingDetalle.setVisibility(View.GONE);
                    binding.btnAprobarJustificacion.setEnabled(true);
                    binding.btnRechazarJustificacion.setEnabled(true);
                    String msj = resource.data != null ? resource.data : "Procesada exitosamente.";
                    mostrarDialogoExitoRevision("¡Justificación Procesada!", msj);
                    break;
                case ERROR:
                    if (binding.pbLoadingDetalle != null) binding.pbLoadingDetalle.setVisibility(View.GONE);
                    binding.btnAprobarJustificacion.setEnabled(true);
                    binding.btnRechazarJustificacion.setEnabled(true);
                    String err = resource.message != null ? resource.message : "Error al revisar justificación";
                    Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                    break;
            }
        });

        // Observador de Justificación de Usuario
        viewModel.getJustificacionUsuarioLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (!isAdded() || binding == null || resource == null) return;
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                RegistroAsistencia resJust = resource.data;
                if (resJust.getJustificacion() != null && !resJust.getJustificacion().trim().isEmpty()) {
                    binding.cardJustificacionAdmin.setVisibility(View.VISIBLE);
                    binding.tvMotivoJustificacionAdmin.setText("Motivo: " + (resJust.getMotivoJustificacion() != null ? resJust.getMotivoJustificacion() : "Inasistencia"));
                    binding.tvDetalleJustificacionAdmin.setText("Detalle: " + resJust.getJustificacion());
                    String estJ = resJust.getEstadoJustificacion();
                    actualizarEstadoBadgeJustificacion(estJ != null ? estJ : "PENDIENTE");
                    mostrarFotoSustento(resJust.getRutaSustento());

                    registro.setEstadoJustificacion(estJ);
                    configurarControlesRevisionAdmin();
                } else {
                    mostrarJustificacionPorDefectoOEstado();
                }
            } else if (resource.status == Resource.Status.ERROR) {
                mostrarJustificacionPorDefectoOEstado();
            }
        });

        // Observador de Historial de Turnos Desplegable
        viewModel.getHistorialTurnosLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (!isAdded() || binding == null || resource == null) return;
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                List<TurnoHistorial> historial = resource.data;
                int count = historial.size();
                binding.tvShiftHistorySubtext.setText(count + " evento(s) registrado(s) · Toca para desplegar");
                binding.rvShiftHistory.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.rvShiftHistory.setAdapter(new TurnoHistorialAdapter(historial));

                binding.layoutShiftHistoryHeader.setOnClickListener(v -> {
                    boolean estaVisible = binding.rvShiftHistory.getVisibility() == View.VISIBLE;
                    binding.rvShiftHistory.setVisibility(estaVisible ? View.GONE : View.VISIBLE);
                    binding.ivShiftHistoryExpandArrow.animate().rotation(estaVisible ? 0f : 180f).setDuration(200).start();
                });
            } else if (resource.status == Resource.Status.ERROR) {
                binding.tvShiftHistorySubtext.setText("Sin historial disponible");
            }
        });

        // Observador de Última Asistencia (Carga dinámica de hora y datos del servidor)
        viewModel.getUltimaAsistencia().observe(getViewLifecycleOwner(), ultima -> {
            if (!isAdded() || binding == null || ultima == null) return;
            String horaUltima = formatearHoraCorta(ultima.getHora());
            if ("--:--".equals(horaUltima) && ultima.getFecha() != null) {
                horaUltima = formatearHoraCorta(ultima.getFecha());
            }
            if (!"--:--".equals(horaUltima)) {
                if (binding.tvAttendanceTime != null) {
                    binding.tvAttendanceTime.setText(horaUltima);
                }
                if (binding.tvServerTime != null) {
                    binding.tvServerTime.setText(horaUltima);
                }
            }
            if (ultima.getFecha() != null && !ultima.getFecha().isEmpty() && binding.tvAttendanceDate != null) {
                binding.tvAttendanceDate.setText(formatearFechaLarga(ultima.getFecha()));
            }
            if (ultima.getTipo() != null && !ultima.getTipo().isEmpty() && binding.tvStatusTitle != null) {
                binding.tvStatusTitle.setText(ultima.getTipo());
            }
            if (ultima.getEstado() != null && !ultima.getEstado().isEmpty() && binding.tvUserStatusBadge != null) {
                configurarBadge(binding.tvUserStatusBadge, ultima.getEstado());
            }
        });
    }

    private static final java.util.TimeZone TIME_ZONE_LIMA = java.util.TimeZone.getTimeZone("America/Lima");
    private static final java.util.Locale LOCALE_PERU = new java.util.Locale("es", "PE");

    private String formatearFechaLarga(String str) {
        if (str == null || str.trim().isEmpty() || str.contains("%H")) {
            return "Fecha: --";
        }
        try {
            String s = str.trim().replace('T', ' ');
            if (s.contains(" ")) {
                s = s.split(" ")[0];
            }
            if (s.length() >= 10) {
                java.text.SimpleDateFormat inFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", LOCALE_PERU);
                java.text.SimpleDateFormat outFormat = new java.text.SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", LOCALE_PERU);
                java.util.Date date = inFormat.parse(s.substring(0, 10));
                if (date != null) {
                    String formatted = outFormat.format(date);
                    if (!formatted.isEmpty()) {
                        formatted = formatted.substring(0, 1).toUpperCase(LOCALE_PERU) + formatted.substring(1);
                    }
                    return "Fecha: " + formatted;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Fecha: " + str;
    }

    private String formatearHoraCorta(String str) {
        if (str == null || str.trim().isEmpty() || str.contains("%H") || str.contains("%i") || str.contains("%s")) {
            return "--:--";
        }
        try {
            String s = str.trim().replace('T', ' ');

            if (s.toLowerCase().contains("am") || s.toLowerCase().contains("pm") || s.toLowerCase().contains("a. m.") || s.toLowerCase().contains("p. m.")) {
                return s;
            }

            if (s.contains(" ")) {
                String[] parts = s.split(" ");
                for (String part : parts) {
                    if (part.contains(":")) {
                        s = part;
                        break;
                    }
                }
            }

            if (s.contains(":")) {
                String[] timeParts = s.split(":");
                if (timeParts.length >= 2) {
                    int hh = Integer.parseInt(timeParts[0].trim());
                    int mm = Integer.parseInt(timeParts[1].trim());

                    String amPm = (hh >= 12) ? "p. m." : "a. m.";
                    int hh12 = hh % 12;
                    if (hh12 == 0) hh12 = 12;

                    return String.format(java.util.Locale.US, "%02d:%02d %s", hh12, mm, amPm);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    private void configurarControlesRevisionAdmin() {
        if (binding == null || registro == null) return;

        String rol = SplashStorage.obtenerRol(requireContext());
        boolean esAdminOSuper = "ADMINISTRADOR".equalsIgnoreCase(rol) || "SUPERVISOR".equalsIgnoreCase(rol);

        String estJ = registro.getEstadoJustificacion();
        boolean yaRevisada = "APROBADA".equalsIgnoreCase(estJ) || "RECHAZADA".equalsIgnoreCase(estJ);

        if (esAdminOSuper && !yaRevisada && binding.cardJustificacionAdmin.getVisibility() == View.VISIBLE) {
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
        int idAsistencia = registro.getIdAsistencias();

        RevisionJustificacionRequest request = new RevisionJustificacionRequest(
                idUsuarioColaborador, idAsistencia, idRevisor, estadoNuevo, comentario
        );

        actualizarEstadoBadgeJustificacion(estadoNuevo);
        viewModel.revisarJustificacion(request);
    }

    private void mostrarDialogoExitoRevision(String titulo, String mensaje) {
        if (!isAdded() || getContext() == null) return;
        if (viewModel != null) {
            viewModel.resetRevisionJustificacionLiveData();
        }
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirmacion_minimal, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvDialogMessage);
        View btnEntendido = dialogView.findViewById(R.id.btnEntendido);

        if (tvTitle != null) tvTitle.setText(titulo);
        if (tvMessage != null) tvMessage.setText(mensaje);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        if (btnEntendido != null) {
            btnEntendido.setOnClickListener(v -> {
                dialog.dismiss();
                if (isAdded() && getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        dialog.show();
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

        if (registro.getJustificacion() != null && !registro.getJustificacion().isEmpty()) {
            binding.cardJustificacionAdmin.setVisibility(View.VISIBLE);
            binding.tvMotivoJustificacionAdmin.setText("Motivo: " + (registro.getMotivoJustificacion() != null ? registro.getMotivoJustificacion() : "Inasistencia"));
            binding.tvDetalleJustificacionAdmin.setText("Detalle: " + registro.getJustificacion());
            mostrarFotoSustento(registro.getRutaSustento());
            return;
        }

        if (registro.getIdUsuario() > 0) {
            viewModel.cargarJustificacionUsuario(registro.getIdUsuario());
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

                if (lat == 0.0 && lng == 0.0) {
                    lat = -6.760481;
                    lng = -79.863352;
                }

                LatLng ubicacion = new LatLng(lat, lng);

                googleMap.clear();
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                BitmapDescriptor iconoPersonalizado = obtenerIconoVectorial(R.drawable.ic_location_pin);

                if (iconoPersonalizado != null) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(ubicacion)
                            .icon(iconoPersonalizado)
                            .title(registro.getSede()));
                } else {
                    googleMap.addMarker(new MarkerOptions().position(ubicacion).title(registro.getSede()));
                }

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16f));
            });
        }
    }

    private BitmapDescriptor obtenerIconoVectorial(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(requireContext(), vectorResId);
        if (vectorDrawable == null) return null;

        vectorDrawable.setTint(Color.parseColor("#00B050"));

        int ancho = vectorDrawable.getIntrinsicWidth() > 0 ? vectorDrawable.getIntrinsicWidth() : 100;
        int alto = vectorDrawable.getIntrinsicHeight() > 0 ? vectorDrawable.getIntrinsicHeight() : 100;

        Bitmap bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
