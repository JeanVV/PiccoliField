package com.piccolifield.ui.despacho;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.piccolifield.R;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.request.EstadoRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentDetalleProspectoBinding;
import com.piccolifield.model.Prospecto;
import com.piccolifield.sharedpreferences.SplashStorage;
import com.piccolifield.util.UserPhotoLoader;

import org.json.JSONObject;

import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleProspectoBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PROSPECTO = "arg_prospecto";

    public interface OnEstadoActualizadoListener {
        void onEstadoActualizado(Prospecto prospecto, String nuevoEstado);
    }

    private FragmentDetalleProspectoBinding binding;
    private Prospecto prospecto;
    private OnEstadoActualizadoListener listener;

    public static DetalleProspectoBottomSheet newInstance(Prospecto prospecto) {
        DetalleProspectoBottomSheet fragment = new DetalleProspectoBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PROSPECTO, prospecto);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnEstadoActualizadoListener(OnEstadoActualizadoListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetalleProspectoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            prospecto = (Prospecto) getArguments().getSerializable(ARG_PROSPECTO);
        }
        if (prospecto == null) {
            dismiss();
            return;
        }

        binding.btnCerrarDetalleProspecto.setOnClickListener(v -> dismiss());

        renderizarProspecto(prospecto);
        configurarSelectorEstado();
        recargarDetalleDesdeApi();
    }

    private void renderizarProspecto(Prospecto p) {
        if (binding == null || p == null) return;

        binding.tvDetalleProspectoNombre.setText(
                p.getNombre() != null && !p.getNombre().trim().isEmpty() ? p.getNombre() : "Sin nombre"
        );

        String estado = p.getEstado() != null && !p.getEstado().trim().isEmpty()
                ? p.getEstado().toUpperCase(Locale.ROOT)
                : "NUEVO";

        String estadoVisible = estado;
        if ("EN_NEGOCIACION".equals(estado)) {
            estadoVisible = "NEGOCIACIÓN";
        }
        binding.tvDetalleProspectoEstado.setText(estadoVisible);
        aplicarEstiloEstado(binding.tvDetalleProspectoEstado, estado);

        String potencial = p.getPotencial() != null && !p.getPotencial().trim().isEmpty()
                ? p.getPotencial().toUpperCase(Locale.ROOT)
                : "MEDIO";
        binding.tvDetalleProspectoPotencial.setText("Potencial: " + potencial);

        if (p.getTipo() != null && !p.getTipo().trim().isEmpty()) {
            binding.tvDetalleProspectoTipo.setVisibility(View.VISIBLE);
            binding.tvDetalleProspectoTipo.setText(p.getTipo());
        } else {
            binding.tvDetalleProspectoTipo.setVisibility(View.GONE);
        }

        binding.tvDetalleProspectoDireccion.setText("Dirección: " + valorONoDisponible(p.getDireccion()));
        binding.tvDetalleProspectoZona.setText("Distrito / Zona: " + valorONoDisponible(p.getZona()));
        binding.tvDetalleProspectoContacto.setText("Contacto: " + valorONoDisponible(p.getContacto()));
        binding.tvDetalleProspectoTelefono.setText("Teléfono: " + valorONoDisponible(p.getTelefono()));

        if (p.getLatitud() != 0 || p.getLongitud() != 0) {
            binding.tvDetalleProspectoCoordenadas.setText(
                    String.format(Locale.US, "Coordenadas: %.6f, %.6f", p.getLatitud(), p.getLongitud())
            );
            binding.tvDetalleProspectoCoordenadas.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetalleProspectoCoordenadas.setVisibility(View.GONE);
        }

        if (p.getNotas() != null && !p.getNotas().trim().isEmpty()) {
            binding.tvDetalleProspectoNotas.setText("Notas: " + p.getNotas().trim());
            binding.tvDetalleProspectoNotas.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetalleProspectoNotas.setVisibility(View.GONE);
        }

        if (p.getMotivoDescarte() != null && !p.getMotivoDescarte().trim().isEmpty()) {
            binding.tvDetalleProspectoMotivoDescarte.setText("Motivo de descarte: " + p.getMotivoDescarte().trim());
            binding.tvDetalleProspectoMotivoDescarte.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetalleProspectoMotivoDescarte.setVisibility(View.GONE);
        }

        cargarFotoProspecto(p.getFotoUrl());
    }

    private void cargarFotoProspecto(String fotoUrl) {
        if (binding == null) return;
        ImageView iv = binding.ivDetalleProspectoFoto;
        iv.setPadding(46, 46, 46, 46);
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iv.setImageResource(R.drawable.ic_store);
        ImageViewCompat.setImageTintList(
                iv,
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.cafe_profundo))
        );

        int idProspecto = prospecto != null ? prospecto.getId() : 0;
        Bitmap cached = com.piccolifield.adapter.ProspectoAdapter.obtenerFotoCache(idProspecto);
        if (cached == null && idProspecto > 0) {
            cached = UserPhotoLoader.cargarFotoProspectoLocal(requireContext(), idProspecto);
            if (cached != null) {
                com.piccolifield.adapter.ProspectoAdapter.guardarFotoCache(idProspecto, cached);
            }
        }
        if (cached != null) {
            mostrarBitmapEnBanner(iv, cached);
            return;
        }

        if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {
            Bitmap b64 = UserPhotoLoader.decodeBase64OrDataUri(fotoUrl);
            if (b64 != null) {
                com.piccolifield.adapter.ProspectoAdapter.guardarFotoCache(idProspecto, b64);
                UserPhotoLoader.guardarFotoProspectoLocal(requireContext(), idProspecto, b64);
                mostrarBitmapEnBanner(iv, b64);
                return;
            }
        }

        String fullUrl = UserPhotoLoader.buildFullImageUrl(fotoUrl);
        String endpointFallback = idProspecto > 0
                ? RetrofitClient.BASE_SERVER_URL + "api/prospectos/" + idProspecto + "/foto"
                : null;
        String urlAUsar = fullUrl != null ? fullUrl : endpointFallback;
        if (urlAUsar == null) return;

        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }

        descargarFotoBanner(iv, idProspecto, urlAUsar, endpointFallback);
    }

    private void descargarFotoBanner(ImageView iv, int idProspecto, String urlAUsar, String endpointFallback) {
        RetrofitClient.createService().descargarArchivoPorUrl(urlAUsar)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (!isAdded() || binding == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            Bitmap bmp = UserPhotoLoader.decodeBitmapFromResponseBody(response.body());
                            if (bmp != null) {
                                com.piccolifield.adapter.ProspectoAdapter.guardarFotoCache(idProspecto, bmp);
                                UserPhotoLoader.guardarFotoProspectoLocal(requireContext(), idProspecto, bmp);
                                iv.post(() -> mostrarBitmapEnBanner(iv, bmp));
                                return;
                            }
                        }
                        if (endpointFallback != null && !endpointFallback.equals(urlAUsar)) {
                            descargarFotoBanner(iv, idProspecto, endpointFallback, null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        if (endpointFallback != null && !endpointFallback.equals(urlAUsar)) {
                            descargarFotoBanner(iv, idProspecto, endpointFallback, null);
                        }
                    }
                });
    }

    private void mostrarBitmapEnBanner(ImageView iv, Bitmap bmp) {
        if (!isAdded() || binding == null) return;
        ImageViewCompat.setImageTintList(iv, null);
        iv.setPadding(0, 0, 0, 0);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setImageBitmap(bmp);
    }

    private void configurarSelectorEstado() {
        if (binding == null || prospecto == null) return;

        String estadoActual = prospecto.getEstado() != null
                ? prospecto.getEstado().toUpperCase(Locale.ROOT)
                : "NUEVO";

        switch (estadoActual) {
            case "EN_NEGOCIACION":
            case "NEGOCIACION":
            case "NEGOCIACIÓN":
                binding.rbEstadoNegociacion.setChecked(true);
                break;
            case "CONVERTIDO":
                binding.rbEstadoConvertido.setChecked(true);
                break;
            case "DESCARTADO":
            case "RECHAZADO":
                binding.rbEstadoDescartado.setChecked(true);
                break;
            case "NUEVO":
            default:
                binding.rbEstadoNuevo.setChecked(true);
                break;
        }

        if ("CONVERTIDO".equals(estadoActual)) {
            binding.tvProspectoConvertidoInfo.setVisibility(View.VISIBLE);
            for (int i = 0; i < binding.rgEstadoProspecto.getChildCount(); i++) {
                binding.rgEstadoProspecto.getChildAt(i).setEnabled(false);
            }
            binding.btnActualizarEstadoProspecto.setVisibility(View.GONE);
            return;
        }

        binding.tvProspectoConvertidoInfo.setVisibility(View.GONE);
        binding.btnActualizarEstadoProspecto.setVisibility(View.VISIBLE);
        binding.btnActualizarEstadoProspecto.setOnClickListener(v -> {
            String nuevoEstado = obtenerEstadoSeleccionado();
            if (nuevoEstado.equalsIgnoreCase(estadoActual)) {
                Toast.makeText(requireContext(), "Selecciona un estado diferente al actual", Toast.LENGTH_SHORT).show();
                return;
            }
            actualizarEstadoEnApi(nuevoEstado);
        });
    }

    private String obtenerEstadoSeleccionado() {
        int checkedId = binding.rgEstadoProspecto.getCheckedRadioButtonId();
        if (checkedId == R.id.rbEstadoNegociacion) return "EN_NEGOCIACION";
        if (checkedId == R.id.rbEstadoConvertido) return "CONVERTIDO";
        if (checkedId == R.id.rbEstadoDescartado) return "DESCARTADO";
        return "NUEVO";
    }

    private void recargarDetalleDesdeApi() {
        if (prospecto.getId() <= 0 || binding == null) return;

        binding.pbDetalleProspecto.setVisibility(View.VISIBLE);
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }

        RetrofitClient.createService().obtenerProspecto(prospecto.getId())
                .enqueue(new Callback<ApiResponse<Prospecto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Prospecto>> call, @NonNull Response<ApiResponse<Prospecto>> response) {
                        if (!isAdded() || binding == null) return;
                        binding.pbDetalleProspecto.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus() && response.body().getData() != null) {
                            prospecto = response.body().getData();
                            renderizarProspecto(prospecto);
                            configurarSelectorEstado();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Prospecto>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.pbDetalleProspecto.setVisibility(View.GONE);
                    }
                });
    }

    private void actualizarEstadoEnApi(String nuevoEstado) {
        if (binding == null) return;

        binding.pbDetalleProspecto.setVisibility(View.VISIBLE);
        binding.btnActualizarEstadoProspecto.setEnabled(false);
        binding.btnActualizarEstadoProspecto.setText("Actualizando...");

        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }

        ApiService apiService = RetrofitClient.createService();
        apiService.actualizarEstadoProspecto(prospecto.getId(), new EstadoRequest(nuevoEstado))
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                        if (!isAdded() || binding == null) return;
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            onCambioExitoso(nuevoEstado);
                        } else if (response.code() == 400) {
                            intentarPutGeneral(apiService, nuevoEstado, extraerMensajeError(response));
                        } else {
                            restaurarBoton();
                            Toast.makeText(requireContext(), extraerMensajeError(response), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        restaurarBoton();
                        Toast.makeText(requireContext(), "Error de red al cambiar estado: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void intentarPutGeneral(ApiService apiService, String nuevoEstado, String errorOriginal) {
        apiService.actualizarProspecto(prospecto.getId(), new EstadoRequest(nuevoEstado))
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                        if (!isAdded() || binding == null) return;
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            onCambioExitoso(nuevoEstado);
                        } else {
                            restaurarBoton();
                            String detalle = errorOriginal != null && !errorOriginal.isEmpty()
                                    ? errorOriginal
                                    : extraerMensajeError(response);
                            Toast.makeText(requireContext(), detalle, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        restaurarBoton();
                        Toast.makeText(requireContext(),
                                errorOriginal != null && !errorOriginal.isEmpty() ? errorOriginal : "Error al actualizar estado",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void onCambioExitoso(String nuevoEstado) {
        if (binding != null) {
            binding.pbDetalleProspecto.setVisibility(View.GONE);
        }
        prospecto.setEstado(nuevoEstado);
        if (listener != null) {
            listener.onEstadoActualizado(prospecto, nuevoEstado);
        }
        dismiss();
    }

    private void restaurarBoton() {
        if (binding == null) return;
        binding.pbDetalleProspecto.setVisibility(View.GONE);
        binding.btnActualizarEstadoProspecto.setEnabled(true);
        binding.btnActualizarEstadoProspecto.setText("Actualizar Estado");
    }

    private String extraerMensajeError(Response<?> response) {
        String fallback = "No se pudo actualizar el estado (HTTP " + response.code() + ")";
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                if (body != null && !body.trim().isEmpty()) {
                    try {
                        JSONObject json = new JSONObject(body);
                        if (json.has("message")) return json.optString("message", fallback);
                        if (json.has("error")) return json.optString("error", fallback);
                    } catch (Exception ignored) {
                        return body;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private void aplicarEstiloEstado(TextView tv, String estado) {
        switch (estado) {
            case "CONVERTIDO":
                tv.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
                tv.setTextColor(Color.parseColor("#2E7D32"));
                break;
            case "EN_NEGOCIACION":
            case "NEGOCIACION":
            case "NEGOCIACIÓN":
                tv.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F3E5F5")));
                tv.setTextColor(Color.parseColor("#6A1B9A"));
                break;
            case "DESCARTADO":
            case "RECHAZADO":
                tv.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
                tv.setTextColor(Color.parseColor("#C62828"));
                break;
            case "NUEVO":
            default:
                tv.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF3E0")));
                tv.setTextColor(Color.parseColor("#E65100"));
                break;
        }
    }

    private String valorONoDisponible(String valor) {
        return valor != null && !valor.trim().isEmpty() ? valor.trim() : "No registrado";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
