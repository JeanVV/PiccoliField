package com.piccolifield.ui.asistencia;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
            cargarHistorialTurnos(registro.getIdUsuario());
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