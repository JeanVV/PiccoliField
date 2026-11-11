package com.piccolifield.ui.asistencias;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.piccolifield.R;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.request.JustificationRequest;
import com.piccolifield.data.remote.dto.request.MarcarAsistenciaRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentAsistenciasBinding;
import com.piccolifield.sharedpreferences.SplashStorage;
import com.piccolifield.ui.asistencia.AsistenciaViewModel;
import com.piccolifield.ui.asistencia.MapaFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsistenciasFragment extends Fragment {

    private FragmentAsistenciasBinding binding;
    private ApiService apiService;
    private AsistenciaViewModel viewModel;
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeRunnable;

    private Uri currentPhotoUri;
    private String fotoSustentoBase64;
    private String tipoMarcaActual = "INGRESO";
    private String tipoMarcaPendiente;

    // Referencias para actualizar el preview en la ventana emergente / BottomSheetDialog
    private ImageView currentDialogIvPreview;
    private View currentDialogLayoutPreview;

    private final ActivityResultLauncher<String> requestLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (tipoMarcaPendiente != null) {
                        obtenerUbicacionYMarcarAsistencia(tipoMarcaPendiente);
                    }
                } else {
                    Toast.makeText(getContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    abrirCamara();
                } else {
                    Toast.makeText(getContext(), "Permiso de cámara necesario para tomar la foto", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && currentPhotoUri != null) {
                    fotoSustentoBase64 = convertirUriABase64(currentPhotoUri);
                    if (currentDialogIvPreview != null && currentDialogLayoutPreview != null) {
                        currentDialogLayoutPreview.setVisibility(View.VISIBLE);
                        currentDialogIvPreview.setImageURI(currentPhotoUri);
                    }
                    Toast.makeText(getContext(), "Foto de sustento capturada", Toast.LENGTH_SHORT).show();
                }
            });

    public AsistenciasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAsistenciasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Asegurar token de API
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }
        apiService = RetrofitClient.createService();

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(AsistenciaViewModel.class);
        observarUiState();

        // 1. Mostrar información del usuario
        mostrarInfoUsuario();

        // 2. Iniciar reloj y fecha
        iniciarReloj();

        // 3. Configurar listeners de botones
        setupListeners();
    }

    private void observarUiState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getEstado()) {
                case LOADING:
                    setBotonesEstado(false, "Guardando...");
                    break;
                case SUCCESS:
                    String msgSuccess = state.getMensaje() != null ? state.getMensaje() : "Asistencia registrada correctamente";
                    Toast.makeText(getContext(), msgSuccess, Toast.LENGTH_SHORT).show();
                    setBotonesEstado(true, null);
                    actualizarTextoUltimaMarca(tipoMarcaActual);
                    viewModel.resetUiState();
                    break;
                case ERROR:
                    String msgError = state.getMensaje() != null ? state.getMensaje() : "Error al registrar asistencia";
                    Toast.makeText(getContext(), msgError, Toast.LENGTH_LONG).show();
                    setBotonesEstado(true, null);
                    viewModel.resetUiState();
                    break;
                case IDLE:
                default:
                    break;
            }
        });
    }

    private void mostrarInfoUsuario() {
        String nombre = SplashStorage.obtenerNombreUsuario(requireContext());
        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = "Colaborador";
        }
        binding.tvNombreColaborador.setText("Hola, " + nombre);

        String rol = SplashStorage.obtenerRol(requireContext());
        if (rol == null || rol.trim().isEmpty()) {
            rol = "COLABORADOR";
        }
        binding.tvRolColaborador.setText("Rol: " + rol.toUpperCase());
    }

    private static final TimeZone TIME_ZONE_LIMA = TimeZone.getTimeZone("America/Lima");
    private static final Locale LOCALE_PERU = new Locale("es", "PE");

    private void iniciarReloj() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", LOCALE_PERU);
        dateFormat.setTimeZone(TIME_ZONE_LIMA);

        String fechaHoy = dateFormat.format(new Date());
        fechaHoy = fechaHoy.substring(0, 1).toUpperCase() + fechaHoy.substring(1);
        binding.tvFechaHoy.setText(fechaHoy);

        calcularDistanciaGeocerca();

        timeRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding != null) {
                    SimpleDateFormat clockFormat = new SimpleDateFormat("hh:mm:ss a", LOCALE_PERU);
                    clockFormat.setTimeZone(TIME_ZONE_LIMA);
                    binding.tvHoraReloj.setText(clockFormat.format(new Date()));
                    timeHandler.postDelayed(this, 1000);
                }
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void calcularDistanciaGeocerca() {
        if (binding == null) return;

        double latSede = -6.7714;
        double lngSede = -79.8409;
        double latWorker = -6.7716;
        double lngWorker = -79.8410;

        float[] results = new float[1];
        android.location.Location.distanceBetween(latSede, lngSede, latWorker, lngWorker, results);
        int distanciaMetros = Math.round(results[0]);

        if (distanciaMetros <= 100) {
            binding.tvDistanciaSede.setText("Distancia: " + distanciaMetros + " m (Zona Lima/Perú)");
            binding.tvGeofenceBadge.setText("EN RANGO");
            binding.tvGeofenceBadge.setBackgroundResource(R.drawable.bg_badge_on_time);
            binding.tvGeofenceBadge.setTextColor(requireContext().getColor(R.color.status_on_time_text));
        } else {
            binding.tvDistanciaSede.setText("Distancia: " + distanciaMetros + " m (Fuera de rango)");
            binding.tvGeofenceBadge.setText("OBSERVADO");
            binding.tvGeofenceBadge.setBackgroundResource(R.drawable.bg_badge_late);
            binding.tvGeofenceBadge.setTextColor(requireContext().getColor(R.color.status_late_text));
        }
    }

    private void setupListeners() {
        // Botón Retroceder (Flecha arriba izquierda)
        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Navegación a la pantalla de Monitoreo en Mapa al pulsar el botón o la tarjeta de geocerca
        if (binding.btnVerMapaMonitoreo != null) {
            binding.btnVerMapaMonitoreo.setOnClickListener(v -> abrirPantallaMonitoreoMapa());
        }
        if (binding.cardGeofenceInfo != null) {
            binding.cardGeofenceInfo.setOnClickListener(v -> abrirPantallaMonitoreoMapa());
        }

        // Botones de marcado de entrada / refrigerio / salida
        binding.btnMarcarEntrada.setOnClickListener(v -> solicitarMarcaConUbicacion("INGRESO"));
        if (binding.btnMarcarRefrigerio != null) {
            binding.btnMarcarRefrigerio.setOnClickListener(v -> solicitarMarcaConUbicacion("REFRIGERIO"));
        }
        binding.btnMarcarSalida.setOnClickListener(v -> solicitarMarcaConUbicacion("SALIDA"));

        // Botón para abrir la ventana emergente / BottomSheet de Justificación
        if (binding.btnAbrirJustificacionBS != null) {
            binding.btnAbrirJustificacionBS.setOnClickListener(v -> abrirBottomSheetJustificacion());
        }
    }

    private void abrirPantallaMonitoreoMapa() {
        if (!isAdded()) return;
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new MapaFragment())
                .addToBackStack(null)
                .commit();
    }

    private void abrirBottomSheetJustificacion() {
        if (getContext() == null || !isAdded()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet_justificacion, null);
        dialog.setContentView(view);

        AutoCompleteTextView spMotivo = view.findViewById(R.id.spMotivoJustificacion);
        TextInputEditText etDetalle = view.findViewById(R.id.etDetalleJustificacion);
        MaterialButton btnTomarFoto = view.findViewById(R.id.btnTomarFotoSustento);
        MaterialButton btnEliminarFoto = view.findViewById(R.id.btnEliminarFotoSustento);
        MaterialButton btnEnviar = view.findViewById(R.id.btnEnviarJustificacion);
        View layoutPreview = view.findViewById(R.id.layoutPreviewFotoSustento);
        ImageView ivPreview = view.findViewById(R.id.ivPreviewFotoSustento);

        // Configurar dropdown de motivos dentro del BottomSheet
        String[] opcionesMotivo = new String[]{
                "Salud / Cita Médica",
                "Emergencia Familiar",
                "Inconveniente de Transporte",
                "Asunto Personal / Trámite",
                "Otro Motivo"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                opcionesMotivo
        );
        spMotivo.setAdapter(adapter);

        // Mostrar preview de foto cargada previamente si existiera
        if (currentPhotoUri != null && ivPreview != null && layoutPreview != null) {
            layoutPreview.setVisibility(View.VISIBLE);
            ivPreview.setImageURI(currentPhotoUri);
        }

        btnTomarFoto.setOnClickListener(v -> {
            currentDialogIvPreview = ivPreview;
            currentDialogLayoutPreview = layoutPreview;
            solicitarPermisoYTomarFoto();
        });

        btnEliminarFoto.setOnClickListener(v -> {
            quitarFotoSustento();
            if (layoutPreview != null) layoutPreview.setVisibility(View.GONE);
            if (ivPreview != null) ivPreview.setImageDrawable(null);
        });

        btnEnviar.setOnClickListener(v -> {
            String motivo = spMotivo.getText() != null ? spMotivo.getText().toString().trim() : "";
            String detalle = etDetalle.getText() != null ? etDetalle.getText().toString().trim() : "";

            if (motivo.isEmpty()) {
                Toast.makeText(getContext(), "Por favor selecciona un motivo de justificación", Toast.LENGTH_SHORT).show();
                return;
            }

            if (detalle.isEmpty()) {
                Toast.makeText(getContext(), "Por favor escribe el detalle o explicación de la falta", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            enviarJustificacion(motivo, detalle);
        });

        dialog.show();
    }

    private void solicitarMarcaConUbicacion(String tipo) {
        this.tipoMarcaActual = tipo;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionYMarcarAsistencia(tipo);
        } else {
            tipoMarcaPendiente = tipo;
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void obtenerUbicacionYMarcarAsistencia(String tipo) {
        if (!isAdded() || binding == null) return;
        setBotonesEstado(false, "Obteniendo ubicación...");

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            setBotonesEstado(true, null);
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(getContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                        setBotonesEstado(true, null);
                        return;
                    }
                    obtenerDireccionYMarcar(location.getLatitude(), location.getLongitude(), tipo);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                    setBotonesEstado(true, null);
                });
    }

    private void obtenerDireccionYMarcar(double lat, double lng, String tipo) {
        if (!isAdded() || binding == null) return;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String direccionResult = null;
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    if (address.getMaxAddressLineIndex() >= 0) {
                        direccionResult = address.getAddressLine(0);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        if (address.getThoroughfare() != null) {
                            sb.append(address.getThoroughfare());
                            if (address.getSubThoroughfare() != null) {
                                sb.append(" ").append(address.getSubThoroughfare());
                            }
                        }
                        if (address.getLocality() != null) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(address.getLocality());
                        }
                        direccionResult = sb.length() > 0 ? sb.toString() : null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                direccionResult = null;
            }

            final String finalDireccion = direccionResult;
            mainHandler.post(() -> {
                if (!isAdded() || binding == null) return;
                enviarSolicitudAsistencia(lat, lng, finalDireccion, tipo);
            });
        });
    }

    private void enviarSolicitudAsistencia(double lat, double lng, @Nullable String direccion, String tipo) {
        int idUsuario = SplashStorage.obtenerUsuarioId(requireContext());

        SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", LOCALE_PERU);
        fullSdf.setTimeZone(TIME_ZONE_LIMA);
        String fechaHora = fullSdf.format(new Date());

        String sede = "Sede Chiclayo Principal";

        MarcarAsistenciaRequest request = new MarcarAsistenciaRequest();
        request.setIdUsuario(idUsuario);
        request.setTipo(tipo);
        request.setLatitud(lat);
        request.setLongitud(lng);
        request.setDireccion(direccion);
        request.setSede(sede);
        request.setFechaHoraDispositivo(fechaHora);

        guardarMarcaEnLocal(idUsuario, tipo, fechaHora, lat, lng, sede);

        viewModel.marcarAsistencia(request);
    }

    private void setBotonesEstado(boolean enabled, @Nullable String loadingText) {
        if (binding == null) return;
        binding.btnMarcarEntrada.setEnabled(enabled);
        if (binding.btnMarcarRefrigerio != null) binding.btnMarcarRefrigerio.setEnabled(enabled);
        binding.btnMarcarSalida.setEnabled(enabled);

        if (!enabled && loadingText != null) {
            if ("INGRESO".equalsIgnoreCase(tipoMarcaActual)) {
                binding.btnMarcarEntrada.setText(loadingText);
            } else if ("REFRIGERIO".equalsIgnoreCase(tipoMarcaActual) && binding.btnMarcarRefrigerio != null) {
                binding.btnMarcarRefrigerio.setText(loadingText);
            } else if ("SALIDA".equalsIgnoreCase(tipoMarcaActual)) {
                binding.btnMarcarSalida.setText(loadingText);
            }
        } else {
            binding.btnMarcarEntrada.setText("Marcar Entrada");
            if (binding.btnMarcarRefrigerio != null) binding.btnMarcarRefrigerio.setText("Marcar Refrigerio");
            binding.btnMarcarSalida.setText("Marcar Salida");
        }
    }

    private void actualizarTextoUltimaMarca(String tipo) {
        if (binding == null) return;
        SimpleDateFormat shortSdf = new SimpleDateFormat("hh:mm a", LOCALE_PERU);
        shortSdf.setTimeZone(TIME_ZONE_LIMA);
        String horaCorta = shortSdf.format(new Date());
        String tipoTexto = "INGRESO".equalsIgnoreCase(tipo) ? "Entrada" : ("REFRIGERIO".equalsIgnoreCase(tipo) ? "Refrigerio" : "Salida");
        binding.tvEstadoHoy.setText("Última marca: " + tipoTexto + " a las " + horaCorta + " (Lima)");
    }

    private void guardarMarcaEnLocal(int idUsuario, String tipo, String fechaHora, double lat, double lng, String sede) {
        if (!isAdded()) return;
        RegistroAsistencia reg = new RegistroAsistencia();
        reg.setIdUsuario(idUsuario);
        String nombreCompleto = SplashStorage.obtenerNombreUsuario(requireContext());
        reg.setNombres(nombreCompleto != null && !nombreCompleto.isEmpty() ? nombreCompleto : "Colaborador " + idUsuario);
        reg.setTipo(tipo);
        reg.setSede(sede);
        reg.setEstado("A_TIEMPO");
        reg.setHoraMarcado(fechaHora);
        reg.setFechaHoraServidor(fechaHora);
        reg.setLatitud(lat);
        reg.setLongitud(lng);

        SplashStorage.guardarRegistroAsistenciaLocal(requireContext(), reg);
    }

    private void solicitarPermisoYTomarFoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        try {
            File photoFile = new File(requireContext().getCacheDir(), "sustento_" + System.currentTimeMillis() + ".jpg");
            currentPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );
            takePictureLauncher.launch(currentPhotoUri);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al abrir cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void quitarFotoSustento() {
        currentPhotoUri = null;
        fotoSustentoBase64 = null;
        currentDialogIvPreview = null;
        currentDialogLayoutPreview = null;
    }

    private String convertirUriABase64(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (bitmap == null) return null;

            int maxDimension = 1024;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > maxDimension || height > maxDimension) {
                float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
                int newWidth = Math.round(ratio * width);
                int newHeight = Math.round(ratio * height);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void enviarJustificacion(String motivo, String detalle) {
        int idUsuario = SplashStorage.obtenerUsuarioId(requireContext());
        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd", LOCALE_PERU);
        dateSdf.setTimeZone(TIME_ZONE_LIMA);
        String fechaHoy = dateSdf.format(new Date());

        JustificationRequest request = new JustificationRequest(idUsuario, motivo, detalle, fechaHoy, fotoSustentoBase64);

        if (binding != null && binding.btnAbrirJustificacionBS != null) {
            binding.btnAbrirJustificacionBS.setEnabled(false);
        }

        // Guardar justificación localmente para vincular de inmediato
        SplashStorage.guardarJustificacionLocal(requireContext(), idUsuario, motivo, detalle, fotoSustentoBase64);

        if (apiService != null) {
            apiService.registrarJustificacion(request).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                    if (!isAdded() || binding == null) return;
                    if (binding.btnAbrirJustificacionBS != null) binding.btnAbrirJustificacionBS.setEnabled(true);

                    mostrarResumenJustificacion(motivo, detalle);
                    Toast.makeText(getContext(), "Justificación enviada correctamente al administrador", Toast.LENGTH_LONG).show();
                    limpiarFormularioJustificacion();
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                    if (!isAdded() || binding == null) return;
                    if (binding.btnAbrirJustificacionBS != null) binding.btnAbrirJustificacionBS.setEnabled(true);

                    // fallback local para la UI
                    mostrarResumenJustificacion(motivo, detalle);
                    Toast.makeText(getContext(), "Justificación registrada localmente", Toast.LENGTH_LONG).show();
                    limpiarFormularioJustificacion();
                }
            });
        }
    }

    private void mostrarResumenJustificacion(String motivo, String detalle) {
        if (binding == null) return;
        binding.cardEstadoJustificacion.setVisibility(View.VISIBLE);
        binding.tvMotivoResumen.setText("Motivo: " + motivo);
        binding.tvDetalleResumen.setText("Detalle: " + detalle);
        binding.tvEstadoJustificacionBadge.setText("ENVIADA - PENDIENTE DE REVISIÓN");
    }

    private void limpiarFormularioJustificacion() {
        quitarFotoSustento();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timeHandler.removeCallbacks(timeRunnable);
        binding = null;
    }
}
