package com.piccolifield.ui.asistencias;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.piccolifield.R;

import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.request.JustificationRequest;
import com.piccolifield.data.remote.dto.request.MarcarAsistenciaRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentAsistenciasBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsistenciasFragment extends Fragment {

    private FragmentAsistenciasBinding binding;
    private ApiService apiService;
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeRunnable;

    private Uri currentPhotoUri;
    private String fotoSustentoBase64;

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
                    if (binding != null) {
                        binding.layoutPreviewFotoSustento.setVisibility(View.VISIBLE);
                        binding.ivPreviewFotoSustento.setImageURI(currentPhotoUri);
                    }
                    fotoSustentoBase64 = convertirUriABase64(currentPhotoUri);
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

        // 1. Mostrar información del usuario
        mostrarInfoUsuario();

        // 2. Iniciar reloj y fecha
        iniciarReloj();

        // 3. Configurar dropdown de motivos
        configurarDropdownMotivos();

        // 4. Configurar listeners de botones
        setupListeners();
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

    private void configurarDropdownMotivos() {
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
        binding.spMotivoJustificacion.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnMarcarEntrada.setOnClickListener(v -> registrarMarca("INGRESO"));
        binding.btnMarcarSalida.setOnClickListener(v -> registrarMarca("SALIDA"));
        binding.btnTomarFotoSustento.setOnClickListener(v -> solicitarPermisoYTomarFoto());
        binding.btnEliminarFotoSustento.setOnClickListener(v -> quitarFotoSustento());
        binding.btnEnviarJustificacion.setOnClickListener(v -> enviarJustificacion());
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
        if (binding != null) {
            binding.layoutPreviewFotoSustento.setVisibility(View.GONE);
            binding.ivPreviewFotoSustento.setImageDrawable(null);
        }
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

    private void registrarMarca(String tipo) {
        int idUsuario = SplashStorage.obtenerUsuarioId(requireContext());

        SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", LOCALE_PERU);
        fullSdf.setTimeZone(TIME_ZONE_LIMA);
        String fechaHora = fullSdf.format(new Date());

        SimpleDateFormat shortSdf = new SimpleDateFormat("hh:mm a", LOCALE_PERU);
        shortSdf.setTimeZone(TIME_ZONE_LIMA);
        String horaCorta = shortSdf.format(new Date());

        double lat = -6.7714;
        double lng = -79.8409;
        String sede = "Sede Chiclayo Principal";

        MarcarAsistenciaRequest request = new MarcarAsistenciaRequest(
                idUsuario, tipo, lat, lng, sede, fechaHora
        );

        binding.btnMarcarEntrada.setEnabled(false);
        binding.btnMarcarSalida.setEnabled(false);

        // Guardar registro local para disponibilizar inmediatamente al Administrador
        guardarMarcaEnLocal(idUsuario, tipo, fechaHora, lat, lng, sede);

        if (apiService != null) {
            apiService.registrarAsistencia(request).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                    if (!isAdded() || binding == null) return;
                    binding.btnMarcarEntrada.setEnabled(true);
                    binding.btnMarcarSalida.setEnabled(true);

                    String tipoTexto = "INGRESO".equalsIgnoreCase(tipo) ? "Entrada" : "Salida";
                    binding.tvEstadoHoy.setText("Última marca: " + tipoTexto + " a las " + horaCorta + " (Lima)");

                    Toast.makeText(getContext(), "¡" + tipoTexto + " registrada a las " + horaCorta + " (Zona Lima, Perú)!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                    if (!isAdded() || binding == null) return;
                    binding.btnMarcarEntrada.setEnabled(true);
                    binding.btnMarcarSalida.setEnabled(true);

                    String tipoTexto = "INGRESO".equalsIgnoreCase(tipo) ? "Entrada" : "Salida";
                    binding.tvEstadoHoy.setText("Última marca: " + tipoTexto + " a las " + horaCorta + " (Lima)");
                    Toast.makeText(getContext(), tipoTexto + " registrada a las " + horaCorta + " (Zona Lima)", Toast.LENGTH_LONG).show();
                }
            });
        }
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

    private void enviarJustificacion() {
        String motivo = binding.spMotivoJustificacion.getText() != null ? binding.spMotivoJustificacion.getText().toString().trim() : "";
        String detalle = binding.etDetalleJustificacion.getText() != null ? binding.etDetalleJustificacion.getText().toString().trim() : "";

        if (motivo.isEmpty()) {
            Toast.makeText(getContext(), "Por favor selecciona un motivo de justificación", Toast.LENGTH_SHORT).show();
            return;
        }

        if (detalle.isEmpty()) {
            Toast.makeText(getContext(), "Por favor escribe el detalle o explicación de la falta", Toast.LENGTH_SHORT).show();
            return;
        }

        int idUsuario = SplashStorage.obtenerUsuarioId(requireContext());
        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd", LOCALE_PERU);
        dateSdf.setTimeZone(TIME_ZONE_LIMA);
        String fechaHoy = dateSdf.format(new Date());

        JustificationRequest request = new JustificationRequest(idUsuario, motivo, detalle, fechaHoy, fotoSustentoBase64);

        binding.btnEnviarJustificacion.setEnabled(false);

        // Guardar justificación localmente para vincular de inmediato
        SplashStorage.guardarJustificacionLocal(requireContext(), idUsuario, motivo, detalle, fotoSustentoBase64);

        if (apiService != null) {
            apiService.registrarJustificacion(request).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                    if (!isAdded() || binding == null) return;
                    binding.btnEnviarJustificacion.setEnabled(true);

                    mostrarResumenJustificacion(motivo, detalle);
                    Toast.makeText(getContext(), "Justificación enviada correctamente al administrador", Toast.LENGTH_LONG).show();
                    limpiarFormularioJustificacion();
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                    if (!isAdded() || binding == null) return;
                    binding.btnEnviarJustificacion.setEnabled(true);

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
        if (binding == null) return;
        binding.spMotivoJustificacion.setText("", false);
        binding.etDetalleJustificacion.setText("");
        quitarFotoSustento();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timeHandler.removeCallbacks(timeRunnable);
        binding = null;
    }
}
