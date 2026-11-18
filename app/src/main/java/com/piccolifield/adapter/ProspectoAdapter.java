package com.piccolifield.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.model.Prospecto;

import java.util.List;
import java.util.Locale;

public class ProspectoAdapter extends RecyclerView.Adapter<ProspectoAdapter.ViewHolder> {

    private final List<Prospecto> prospectos;
    private final OnProspectoClickListener listener;
    private OnCambiarEstadoClickListener cambiarEstadoListener;

    public interface OnProspectoClickListener {
        void onProspectoClick(Prospecto prospecto);
    }

    public interface OnCambiarEstadoClickListener {
        void onCambiarEstadoClick(Prospecto prospecto);
    }

    public ProspectoAdapter(List<Prospecto> prospectos, OnProspectoClickListener listener) {
        this.prospectos = prospectos;
        this.listener = listener;
    }

    public ProspectoAdapter(List<Prospecto> prospectos, OnProspectoClickListener listener, OnCambiarEstadoClickListener cambiarEstadoListener) {
        this.prospectos = prospectos;
        this.listener = listener;
        this.cambiarEstadoListener = cambiarEstadoListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prospecto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Prospecto prospecto = prospectos.get(position);
        holder.bind(prospecto, listener, cambiarEstadoListener);
    }

    @Override
    public int getItemCount() {
        return prospectos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final android.widget.ImageView ivProspectoFoto;
        private final TextView tvProspectoNombre;
        private final TextView tvProspectoZona;
        private final TextView tvProspectoEstado;
        private final TextView tvCambiarEstadoProspecto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProspectoFoto = itemView.findViewById(R.id.ivProspectoFoto);
            tvProspectoNombre = itemView.findViewById(R.id.tvProspectoNombre);
            tvProspectoZona = itemView.findViewById(R.id.tvProspectoZona);
            tvProspectoEstado = itemView.findViewById(R.id.tvProspectoEstado);
            tvCambiarEstadoProspecto = itemView.findViewById(R.id.tvCambiarEstadoProspecto);
        }

        public void bind(Prospecto prospecto, OnProspectoClickListener listener, OnCambiarEstadoClickListener cambiarEstadoListener) {
            tvProspectoNombre.setText(prospecto.getNombre() != null && !prospecto.getNombre().isEmpty()
                    ? prospecto.getNombre() : "Sin nombre");

            StringBuilder sub = new StringBuilder();
            if (prospecto.getDireccion() != null && !prospecto.getDireccion().isEmpty()) {
                sub.append(prospecto.getDireccion());
            }
            if (prospecto.getZona() != null && !prospecto.getZona().isEmpty()) {
                if (sub.length() > 0) sub.append(" · ");
                sub.append(prospecto.getZona());
            }
            String tipoInfo = prospecto.getTipo() != null && !prospecto.getTipo().isEmpty()
                    ? prospecto.getTipo()
                    : prospecto.getPotencial();
            if (tipoInfo != null && !tipoInfo.isEmpty()) {
                if (sub.length() > 0) sub.append(" · ");
                sub.append(tipoInfo);
            }
            tvProspectoZona.setText(sub.length() > 0 ? sub.toString() : "Prospecto");

            String estado = prospecto.getEstado() != null && !prospecto.getEstado().isEmpty()
                    ? prospecto.getEstado().toUpperCase(Locale.ROOT)
                    : "NUEVO";
            
            // Adaptar los nombres reales de la base de datos a nombres amigables para el usuario
            String textoEstadoVisible = estado;
            if (estado.equals("EN_NEGOCIACION")) textoEstadoVisible = "NEGOCIACIÓN";
            else if (estado.equals("DESCARTADO")) textoEstadoVisible = "DESCARTADO";
            
            tvProspectoEstado.setText(textoEstadoVisible);
            aplicarEstiloEstado(tvProspectoEstado, estado);

            if (tvCambiarEstadoProspecto != null) {
                if (cambiarEstadoListener != null) {
                    tvCambiarEstadoProspecto.setVisibility(View.VISIBLE);
                    tvCambiarEstadoProspecto.setOnClickListener(v -> cambiarEstadoListener.onCambiarEstadoClick(prospecto));
                } else {
                    tvCambiarEstadoProspecto.setVisibility(View.GONE);
                }
            }

            if (ivProspectoFoto != null) {
                ivProspectoFoto.setPadding(30, 30, 30, 30);
                ivProspectoFoto.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
                ivProspectoFoto.setImageResource(R.drawable.ic_store);
                androidx.core.widget.ImageViewCompat.setImageTintList(
                        ivProspectoFoto,
                        ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.cafe_profundo))
                );
                String fotoUrl = prospecto.getFotoUrl();
                if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {
                    android.graphics.Bitmap b64 = com.piccolifield.util.UserPhotoLoader.decodeBase64OrDataUri(fotoUrl);
                    if (b64 != null) {
                        androidx.core.widget.ImageViewCompat.setImageTintList(ivProspectoFoto, null);
                        ivProspectoFoto.setPadding(0, 0, 0, 0);
                        ivProspectoFoto.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                        ivProspectoFoto.setImageBitmap(b64);
                    } else {
                        String fullUrl = com.piccolifield.util.UserPhotoLoader.buildFullImageUrl(fotoUrl);
                        if (fullUrl != null) {
                            com.piccolifield.data.remote.RetrofitClient.createService().descargarArchivoPorUrl(fullUrl)
                                    .enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                                        @Override
                                        public void onResponse(@NonNull retrofit2.Call<okhttp3.ResponseBody> call, @NonNull retrofit2.Response<okhttp3.ResponseBody> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                android.graphics.Bitmap bmp = com.piccolifield.util.UserPhotoLoader.decodeBitmapFromResponseBody(response.body());
                                                if (bmp != null) {
                                                    ivProspectoFoto.post(() -> {
                                                        androidx.core.widget.ImageViewCompat.setImageTintList(ivProspectoFoto, null);
                                                        ivProspectoFoto.setPadding(0, 0, 0, 0);
                                                        ivProspectoFoto.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                                                        ivProspectoFoto.setImageBitmap(bmp);
                                                    });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull retrofit2.Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {}
                                    });
                        }
                    }
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProspectoClick(prospecto);
                }
            });
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
    }
}
