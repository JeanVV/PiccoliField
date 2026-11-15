package com.piccolifield.ui.despacho;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.piccolifield.R;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.model.PuntoDistribucion;
import com.piccolifield.sharedpreferences.PuntosDistribucionStorage;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoPuntoDistribucionFragment extends Fragment {

    private EditText etNombrePunto, etCodigoPunto, etDireccionPunto;
    private Spinner spCriterioNegocio, spDistritoPunto, spEstadoPunto;
    private MaterialButton btnCapturarGpsPunto, btnGuardarNuevoPunto;
    private TextView tvCoordenadasPunto;
    private ImageView btnBackNuevoPunto;

    private FusedLocationProviderClient fusedLocationClient;
    private double latitudActual = -12.046374;
    private double longitudActual = -77.042793;
    private int siguienteNumeroSugerido = 35;
    private Runnable onPuntoRegistrado;

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
        etCodigoPunto = view.findViewById(R.id.etCodigoPunto);
        etDireccionPunto = view.findViewById(R.id.etDireccionPunto);
        spCriterioNegocio = view.findViewById(R.id.spCriterioNegocio);
        spDistritoPunto = view.findViewById(R.id.spDistritoPunto);
        spEstadoPunto = view.findViewById(R.id.spEstadoPunto);
        btnCapturarGpsPunto = view.findViewById(R.id.btnCapturarGpsPunto);
        btnGuardarNuevoPunto = view.findViewById(R.id.btnGuardarNuevoPunto);
        tvCoordenadasPunto = view.findViewById(R.id.tvCoordenadasPunto);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        etCodigoPunto.setText(String.format(Locale.US, "PDV-%03d", siguienteNumeroSugerido));

        String[] criterios = {"Minorista", "Mayorista"};
        ArrayAdapter<String> adapterCriterio = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, criterios);
        adapterCriterio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCriterioNegocio.setAdapter(adapterCriterio);

        String[] distritos = {"Miraflores", "San Isidro", "Santiago de Surco", "Lima Cercado", "La Molina", "San Borja", "Los Olivos", "Barranco"};
        ArrayAdapter<String> adapterDistrito = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, distritos);
        adapterDistrito.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDistritoPunto.setAdapter(adapterDistrito);

        String[] estados = {"ACTIVO", "INACTIVO"};
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, estados);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEstadoPunto.setAdapter(adapterEstado);

        btnBackNuevoPunto.setOnClickListener(v -> cerrarFragment());

        btnCapturarGpsPunto.setOnClickListener(v -> capturarUbicacion());

        btnGuardarNuevoPunto.setOnClickListener(v -> guardarPuntoDistribucion());
    }

    private void capturarUbicacion() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (!isAdded()) return;
                if (location != null) {
                    latitudActual = location.getLatitude();
                    longitudActual = location.getLongitude();
                }
                tvCoordenadasPunto.setText(String.format(Locale.US, "%.5f, %.5f", latitudActual, longitudActual));
                tvCoordenadasPunto.setTextColor(ContextCompat.getColor(requireContext(), R.color.chocolate));
                Toast.makeText(requireContext(), "Coordenadas actualizadas", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void guardarPuntoDistribucion() {
        String nombre = etNombrePunto.getText().toString().trim();
        String codigo = etCodigoPunto.getText().toString().trim();
        String direccion = etDireccionPunto.getText().toString().trim();
        String criterio = spCriterioNegocio.getSelectedItem() != null ? spCriterioNegocio.getSelectedItem().toString() : "Minorista";
        String distrito = spDistritoPunto.getSelectedItem() != null ? spDistritoPunto.getSelectedItem().toString() : "Lima";
        String estado = spEstadoPunto.getSelectedItem() != null ? spEstadoPunto.getSelectedItem().toString() : "ACTIVO";

        if (nombre.isEmpty()) {
            etNombrePunto.setError("Ingrese el nombre del punto de distribución");
            Toast.makeText(requireContext(), "El nombre del punto es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (direccion.isEmpty()) {
            etDireccionPunto.setError("Ingrese la dirección del local");
            Toast.makeText(requireContext(), "La dirección es obligatoria", Toast.LENGTH_SHORT).show();
            return;
        }

        if (codigo.isEmpty()) {
            codigo = String.format(Locale.US, "PDV-%03d", siguienteNumeroSugerido);
        }

        String direccionCompleta = direccion.toLowerCase().contains(distrito.toLowerCase())
                ? direccion
                : direccion + ", " + distrito;

        int idGenerado = Math.max(siguienteNumeroSugerido, (int) (System.currentTimeMillis() % 900) + 100);

        PuntoDistribucion nuevoPunto = new PuntoDistribucion();
        nuevoPunto.setIdPuntosVenta(idGenerado);
        nuevoPunto.setPuntosVenta(nombre);
        nuevoPunto.setCodigo(codigo);
        nuevoPunto.setTipo(criterio);
        nuevoPunto.setCategoria("DISTRIBUCION");
        nuevoPunto.setDireccion(direccionCompleta);
        nuevoPunto.setEstado(estado);
        nuevoPunto.setLatitud(latitudActual);
        nuevoPunto.setLongitud(longitudActual);
        nuevoPunto.setIdDistrito(spDistritoPunto.getSelectedItemPosition() + 1);
        nuevoPunto.setDistancia("0.5 km");
        nuevoPunto.setBadgeNumero(String.valueOf(siguienteNumeroSugerido));

        btnGuardarNuevoPunto.setEnabled(false);

        ApiService apiService = RetrofitClient.createService();
        apiService.registrarPuntoDistribucion(nuevoPunto).enqueue(new Callback<ApiResponse<PuntoDistribucion>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PuntoDistribucion>> call, @NonNull Response<ApiResponse<PuntoDistribucion>> response) {
                if (!isAdded()) return;
                btnGuardarNuevoPunto.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    PuntoDistribucion creado = response.body().getData();
                    if (creado.getPuntosVenta() == null || creado.getPuntosVenta().isEmpty()) {
                        creado.setPuntosVenta(nuevoPunto.getNombre());
                    }
                    creado.setCodigo(nuevoPunto.getCodigo());
                    creado.setTipo(nuevoPunto.getCriterioNegocio());
                    creado.setDireccion(nuevoPunto.getDireccion());
                    PuntosDistribucionStorage.guardarPuntoLocal(requireContext(), creado);
                } else {
                    PuntosDistribucionStorage.guardarPuntoLocal(requireContext(), nuevoPunto);
                }

                Toast.makeText(requireContext(), "Punto de distribución registrado con éxito", Toast.LENGTH_SHORT).show();
                if (onPuntoRegistrado != null) {
                    onPuntoRegistrado.run();
                }
                cerrarFragment();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PuntoDistribucion>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnGuardarNuevoPunto.setEnabled(true);
                PuntosDistribucionStorage.guardarPuntoLocal(requireContext(), nuevoPunto);
                Toast.makeText(requireContext(), "Punto de distribución registrado", Toast.LENGTH_SHORT).show();
                if (onPuntoRegistrado != null) {
                    onPuntoRegistrado.run();
                }
                cerrarFragment();
            }
        });
    }

    private void cerrarFragment() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
}
