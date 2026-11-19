package com.piccolifield.ui.despacho;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.button.MaterialButton;
import com.piccolifield.R;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.Prospecto;
import com.piccolifield.sharedpreferences.SplashStorage;
import com.piccolifield.util.UserPhotoLoader;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoPuntoDistribucionFragment extends Fragment {

    private EditText etNombrePunto, etDireccionPunto, etContactoProspecto, etTelefonoProspecto, etNotasProspecto;
    private Spinner spCriterioNegocio, spPotencialProspecto, spDistritoPunto, spEstadoPunto;
    private MaterialButton btnCapturarGpsPunto, btnGuardarNuevoPunto, btnSeleccionarFotoProspecto;
    private TextView tvCoordenadasPunto, tvEstadoFotoProspecto;
    private ImageView btnBackNuevoPunto, ivFotoFachadaProspecto;
    private android.widget.ProgressBar pbNuevoPunto;

    private FusedLocationProviderClient fusedLocationClient;
    private double latitudActual = -6.77140;
    private double longitudActual = -79.84090;
    private int siguienteNumeroSugerido = 35;
    private Runnable onPuntoRegistrado;

    private Uri cameraPhotoUri;
    private byte[] fotoFachadaBytes;
    private final Handler gpsTimeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable gpsTimeoutRunnable;
    private CancellationTokenSource gpsCancellationTokenSource;
    private LocationCallback activeLocationCallback;
    private boolean ubicacionResuelta = false;

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    abrirCamaraFachada();
                } else {
                    Toast.makeText(getContext(), "Permiso de cámara requerido", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String[]> requestLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarse = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                if (Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse)) {
                    capturarUbicacion();
                } else {
                    Toast.makeText(getContext(), "Permiso de ubicación requerido para obtener tus coordenadas", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraPhotoUri != null) {
                    procesarUriFoto(cameraPhotoUri);
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    procesarUriFoto(uri);
                }
            });

    public NuevoPuntoDistribucionFragment() {}

    public NuevoPuntoDistribucionFragment(int siguienteNumeroSugerido, Runnable onPuntoRegistrado) {
        this.siguienteNumeroSugerido = siguienteNumeroSugerido;
        this.onPuntoRegistrado = onPuntoRegistrado;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nuevo_punto_distribucion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBackNuevoPunto = view.findViewById(R.id.btnBackNuevoPunto);
        etNombrePunto = view.findViewById(R.id.etNombrePunto);
        etDireccionPunto = view.findViewById(R.id.etDireccionPunto);
        etContactoProspecto = view.findViewById(R.id.etContactoProspecto);
        etTelefonoProspecto = view.findViewById(R.id.etTelefonoProspecto);
        etNotasProspecto = view.findViewById(R.id.etNotasProspecto);
        spCriterioNegocio = view.findViewById(R.id.spCriterioNegocio);
        spPotencialProspecto = view.findViewById(R.id.spPotencialProspecto);
        spDistritoPunto = view.findViewById(R.id.spDistritoPunto);
        spEstadoPunto = view.findViewById(R.id.spEstadoPunto);
        btnCapturarGpsPunto = view.findViewById(R.id.btnCapturarGpsPunto);
        btnGuardarNuevoPunto = view.findViewById(R.id.btnGuardarNuevoPunto);
        tvCoordenadasPunto = view.findViewById(R.id.tvCoordenadasPunto);
        ivFotoFachadaProspecto = view.findViewById(R.id.ivFotoFachadaProspecto);
        tvEstadoFotoProspecto = view.findViewById(R.id.tvEstadoFotoProspecto);
        btnSeleccionarFotoProspecto = view.findViewById(R.id.btnSeleccionarFotoProspecto);
        pbNuevoPunto = view.findViewById(R.id.pbNuevoPunto);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        String[] tiposLocal = {"Bodega (Minorista)", "Minimarket (Minorista)", "Licorería (Minorista)", "Distribuidora (Mayorista)", "Supermercado (Mayorista)"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tiposLocal);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCriterioNegocio.setAdapter(adapterTipo);

        String[] potenciales = {"MEDIO", "ALTO", "BAJO"};
        ArrayAdapter<String> adapterPotencial = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, potenciales);
        adapterPotencial.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPotencialProspecto.setAdapter(adapterPotencial);

        String[] distritos = {"Chiclayo", "José Leonardo Ortiz", "La Victoria", "Pimentel", "Lambayeque", "Miraflores", "San Isidro", "Lima Cercado"};
        ArrayAdapter<String> adapterDistrito = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, distritos);
        adapterDistrito.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDistritoPunto.setAdapter(adapterDistrito);

        String[] estados = {"NUEVO"};
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, estados);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEstadoPunto.setAdapter(adapterEstado);

        btnBackNuevoPunto.setOnClickListener(v -> cerrarFragment());
        btnCapturarGpsPunto.setOnClickListener(v -> capturarUbicacion());
        btnGuardarNuevoPunto.setOnClickListener(v -> guardarProspecto());
        if (btnSeleccionarFotoProspecto != null) {
            btnSeleccionarFotoProspecto.setOnClickListener(v -> mostrarOpcionesFoto());
        }
        if (ivFotoFachadaProspecto != null) {
            ivFotoFachadaProspecto.setOnClickListener(v -> mostrarOpcionesFoto());
        }
    }

    private void mostrarOpcionesFoto() {
        if (!isAdded()) return;
        String[] opciones = {"Tomar foto con la cámara", "Elegir de la galería"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Foto de fachada del negocio")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            abrirCamaraFachada();
                        } else {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                        }
                    } else if (which == 1) {
                        pickImageLauncher.launch("image/*");
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirCamaraFachada() {
        try {
            File photoFile = new File(requireContext().getCacheDir(), "fachada_" + System.currentTimeMillis() + ".jpg");
            cameraPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );
            takePictureLauncher.launch(cameraPhotoUri);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al abrir cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void procesarUriFoto(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
            if (original == null) return;

            int maxDim = 720;
            int w = original.getWidth();
            int h = original.getHeight();
            Bitmap scaled = original;
            if (w > maxDim || h > maxDim) {
                float ratio = Math.min((float) maxDim / w, (float) maxDim / h);
                scaled = Bitmap.createScaledBitmap(original, Math.round(w * ratio), Math.round(h * ratio), true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            fotoFachadaBytes = baos.toByteArray();

            if (ivFotoFachadaProspecto != null) {
                ImageViewCompat.setImageTintList(ivFotoFachadaProspecto, null);
                ivFotoFachadaProspecto.setPadding(0, 0, 0, 0);
                ivFotoFachadaProspecto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivFotoFachadaProspecto.setImageBitmap(scaled);
            }
            if (tvEstadoFotoProspecto != null) {
                tvEstadoFotoProspecto.setText("Foto lista para guardar");
                tvEstadoFotoProspecto.setTextColor(ContextCompat.getColor(requireContext(), R.color.cafe_profundo));
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error procesando foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean estaGpsActivo() {
        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        return lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void mostrarDialogoActivarGps() {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("GPS desactivado")
                .setMessage("El GPS del equipo está apagado. Actívalo para obtener tu latitud y longitud actual.")
                .setPositiveButton("Activar GPS", (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @SuppressLint("MissingPermission")
    private void capturarUbicacion() {
        boolean tieneFine = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean tieneCoarse = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!tieneFine && !tieneCoarse) {
            requestLocationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }

        if (!estaGpsActivo()) {
            mostrarDialogoActivarGps();
            return;
        }

        limpiarSolicitudUbicacion();
        ubicacionResuelta = false;
        btnCapturarGpsPunto.setEnabled(false);
        btnCapturarGpsPunto.setText("Obteniendo...");
        if (pbNuevoPunto != null) pbNuevoPunto.setVisibility(View.VISIBLE);

        gpsTimeoutRunnable = () -> {
            if (!isAdded() || ubicacionResuelta) return;
            limpiarSolicitudUbicacion();
            finalizarCapturaUbicacionSinResultado();
        };
        gpsTimeoutHandler.postDelayed(gpsTimeoutRunnable, 10000L);

        gpsCancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, gpsCancellationTokenSource.getToken())
                .addOnSuccessListener(requireActivity(), location -> {
                    if (!isAdded() || ubicacionResuelta) return;
                    if (location != null) {
                        aplicarUbicacionObtenida(location);
                    } else {
                        intentarUltimaUbicacionOActualizacion();
                    }
                })
                .addOnFailureListener(requireActivity(), e -> {
                    if (!isAdded() || ubicacionResuelta) return;
                    intentarUltimaUbicacionOActualizacion();
                });
    }

    @SuppressLint("MissingPermission")
    private void intentarUltimaUbicacionOActualizacion() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), lastLocation -> {
                    if (!isAdded() || ubicacionResuelta) return;
                    if (lastLocation != null) {
                        aplicarUbicacionObtenida(lastLocation);
                        return;
                    }
                    LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                            .setMaxUpdates(1)
                            .setDurationMillis(8000L)
                            .build();

                    activeLocationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            if (!isAdded() || ubicacionResuelta) return;
                            Location loc = locationResult.getLastLocation();
                            if (loc != null) {
                                aplicarUbicacionObtenida(loc);
                            } else {
                                limpiarSolicitudUbicacion();
                                finalizarCapturaUbicacionSinResultado();
                            }
                        }
                    };
                    fusedLocationClient.requestLocationUpdates(request, activeLocationCallback, Looper.getMainLooper())
                            .addOnFailureListener(e -> {
                                if (!isAdded() || ubicacionResuelta) return;
                                limpiarSolicitudUbicacion();
                                finalizarCapturaUbicacionSinResultado();
                            });
                })
                .addOnFailureListener(requireActivity(), e -> {
                    if (!isAdded() || ubicacionResuelta) return;
                    limpiarSolicitudUbicacion();
                    finalizarCapturaUbicacionSinResultado();
                });
    }

    private void limpiarSolicitudUbicacion() {
        if (gpsTimeoutRunnable != null) {
            gpsTimeoutHandler.removeCallbacks(gpsTimeoutRunnable);
            gpsTimeoutRunnable = null;
        }
        if (gpsCancellationTokenSource != null) {
            gpsCancellationTokenSource.cancel();
            gpsCancellationTokenSource = null;
        }
        if (activeLocationCallback != null && fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(activeLocationCallback);
            activeLocationCallback = null;
        }
    }

    private void aplicarUbicacionObtenida(@NonNull Location location) {
        ubicacionResuelta = true;
        limpiarSolicitudUbicacion();
        latitudActual = location.getLatitude();
        longitudActual = location.getLongitude();
        tvCoordenadasPunto.setText(String.format(Locale.US, "Lat: %.6f  |  Lon: %.6f", latitudActual, longitudActual));
        tvCoordenadasPunto.setTextColor(ContextCompat.getColor(requireContext(), R.color.cafe_profundo));
        btnCapturarGpsPunto.setEnabled(true);
        btnCapturarGpsPunto.setText("Obtener Coordenadas");
        if (pbNuevoPunto != null) pbNuevoPunto.setVisibility(View.GONE);
        Toast.makeText(requireContext(), "Coordenadas actualizadas", Toast.LENGTH_SHORT).show();
    }

    private void finalizarCapturaUbicacionSinResultado() {
        ubicacionResuelta = true;
        btnCapturarGpsPunto.setEnabled(true);
        btnCapturarGpsPunto.setText("Obtener Coordenadas");
        if (pbNuevoPunto != null) pbNuevoPunto.setVisibility(View.GONE);
        Toast.makeText(requireContext(), "No se pudo obtener tu ubicación. Verifica la señal GPS e inténtalo nuevamente.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        limpiarSolicitudUbicacion();
        super.onDestroyView();
    }

    private void guardarProspecto() {
        String nombreComercial = etNombrePunto.getText().toString().trim();
        String direccion = etDireccionPunto.getText().toString().trim();
        String contacto = etContactoProspecto.getText().toString().trim();
        String telefono = etTelefonoProspecto.getText().toString().trim();
        String notas = etNotasProspecto.getText().toString().trim();

        String tipoLocal = spCriterioNegocio.getSelectedItem() != null ? spCriterioNegocio.getSelectedItem().toString() : "Bodega (Minorista)";
        String potencial = spPotencialProspecto.getSelectedItem() != null ? spPotencialProspecto.getSelectedItem().toString() : "MEDIO";
        String distrito = spDistritoPunto.getSelectedItem() != null ? spDistritoPunto.getSelectedItem().toString() : "Chiclayo";

        if (nombreComercial.isEmpty()) {
            etNombrePunto.setError("Ingrese el nombre comercial");
            Toast.makeText(requireContext(), "El nombre comercial es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (direccion.isEmpty()) {
            etDireccionPunto.setError("Ingrese la dirección del local");
            Toast.makeText(requireContext(), "La dirección es obligatoria", Toast.LENGTH_SHORT).show();
            return;
        }

        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }

        int idUsuario = UserPhotoLoader.resolveMyUserId(requireContext());
        int idDistrito = spDistritoPunto.getSelectedItemPosition() + 1;

        Prospecto nuevoProspecto = new Prospecto();
        nuevoProspecto.setNombre(nombreComercial);
        nuevoProspecto.setTipo(tipoLocal);
        nuevoProspecto.setDireccion(direccion);
        if (!contacto.isEmpty()) nuevoProspecto.setContacto(contacto);
        if (!telefono.isEmpty()) nuevoProspecto.setTelefono(telefono);
        nuevoProspecto.setPotencial(potencial);
        nuevoProspecto.setEstado("NUEVO");
        if (!notas.isEmpty()) nuevoProspecto.setNotas(notas);
        nuevoProspecto.setLatitud(latitudActual);
        nuevoProspecto.setLongitud(longitudActual);
        nuevoProspecto.setZona(distrito);
        nuevoProspecto.setIdDistrito(idDistrito);
        if (idUsuario > 0) {
            nuevoProspecto.setIdUsuario(idUsuario);
        }

        btnGuardarNuevoPunto.setEnabled(false);
        if (pbNuevoPunto != null) pbNuevoPunto.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.createService();

        if (fotoFachadaBytes != null && fotoFachadaBytes.length > 0) {
            enviarProspectoMultipart(apiService, nuevoProspecto, idUsuario, idDistrito);
        } else {
            enviarProspectoJson(apiService, nuevoProspecto);
        }
    }

    private okhttp3.RequestBody toTextBody(String value) {
        return okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("text/plain"),
                value != null ? value : ""
        );
    }

    private void enviarProspectoMultipart(ApiService apiService, Prospecto p, int idUsuario, int idDistrito) {
        try {
            File tempFile = new File(requireContext().getCacheDir(), "prospecto_fachada_" + System.currentTimeMillis() + ".jpg");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                fos.write(fotoFachadaBytes);
            }
            okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/jpeg"), tempFile);
            okhttp3.MultipartBody.Part fotoPart = okhttp3.MultipartBody.Part.createFormData("foto", tempFile.getName(), fileBody);

            apiService.registrarProspectoMultipart(
                    toTextBody(p.getNombre()),
                    toTextBody(p.getTipo()),
                    toTextBody(p.getDireccion()),
                    toTextBody(p.getContacto()),
                    toTextBody(p.getTelefono()),
                    toTextBody(p.getPotencial()),
                    toTextBody("NUEVO"),
                    toTextBody(p.getNotas()),
                    toTextBody(String.format(Locale.US, "%.8f", p.getLatitud())),
                    toTextBody(String.format(Locale.US, "%.8f", p.getLongitud())),
                    toTextBody(String.valueOf(idDistrito)),
                    toTextBody(String.valueOf(Math.max(1, idUsuario))),
                    fotoPart
            ).enqueue(new Callback<ApiResponse<Prospecto>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Prospecto>> call, @NonNull Response<ApiResponse<Prospecto>> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful()) {
                        btnGuardarNuevoPunto.setEnabled(true);
                        if (pbNuevoPunto != null) pbNuevoPunto.setVisibility(View.GONE);
                        if (response.body() != null && response.body().getData() != null && response.body().getData().getId() > 0) {
                            UserPhotoLoader.guardarFotoProspectoBytesLocal(
                                    requireContext(),
                                    response.body().getData().getId(),
                                    fotoFachadaBytes
                            );
                        }
                        Toast.makeText(requireContext(), "Prospecto y foto registrados con estado NUEVO", Toast.LENGTH_SHORT).show();
                        if (onPuntoRegistrado != null) {
                            onPuntoRegistrado.run();
                        }
                        cerrarFragment();
                    } else {
                        // Respaldo: registrar sin foto para evitar error 1406 en VARCHAR(255)
                        enviarProspectoJson(apiService, p);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Prospecto>> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    enviarProspectoJson(apiService, p);
                }
            });
        } catch (Exception e) {
            enviarProspectoJson(apiService, p);
        }
    }

    private void enviarProspectoJson(ApiService apiService, Prospecto nuevoProspecto) {
        apiService.registrarProspecto(nuevoProspecto).enqueue(new Callback<ApiResponse<Prospecto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Prospecto>> call, @NonNull Response<ApiResponse<Prospecto>> response) {
                if (!isAdded()) return;
                btnGuardarNuevoPunto.setEnabled(true);
                if (pbNuevoPunto != null) pbNuevoPunto.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Prospecto registrado con estado NUEVO", Toast.LENGTH_SHORT).show();
                    if (onPuntoRegistrado != null) {
                        onPuntoRegistrado.run();
                    }
                    cerrarFragment();
                } else {
                    String errMsg = "No se pudo registrar el prospecto (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String raw = response.errorBody().string();
                            JSONObject json = new JSONObject(raw);
                            String m = json.optString("message", json.optString("error", ""));
                            if (!m.isEmpty()) errMsg = m;
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(requireContext(), errMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Prospecto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnGuardarNuevoPunto.setEnabled(true);
                if (pbNuevoPunto != null) pbNuevoPunto.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error de red al registrar prospecto: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cerrarFragment() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
}
