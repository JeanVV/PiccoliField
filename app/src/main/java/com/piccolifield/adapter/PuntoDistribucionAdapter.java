package com.piccolifield.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.piccolifield.DetallePuntoDistribucionActivity;
import com.piccolifield.R;
import com.piccolifield.model.PuntoDistribucion;

import java.util.List;

public class PuntoDistribucionAdapter extends RecyclerView.Adapter<PuntoDistribucionAdapter.ViewHolder> {

    public interface OnPuntoDistribucionClickListener {
        void onPuntoDistribucionClick(PuntoDistribucion pdv);
    }

    private List<PuntoDistribucion> listaPuntos;
    private Context context;
    private OnPuntoDistribucionClickListener listener;

    public PuntoDistribucionAdapter(List<PuntoDistribucion> listaPuntos, Context context) {
        this(listaPuntos, context, null);
    }

    public PuntoDistribucionAdapter(List<PuntoDistribucion> listaPuntos, Context context, OnPuntoDistribucionClickListener listener) {
        this.listaPuntos = listaPuntos;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_punto_distribucion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PuntoDistribucion pdv = listaPuntos.get(position);
        if (pdv == null) return;

        holder.tvNombrePdv.setText(pdv.getNombre());
        holder.tvDireccionPdv.setText(pdv.getDireccion());

        String codPdv = pdv.getCodigo();
        if (codPdv != null && !codPdv.toLowerCase().startsWith("código") && !codPdv.toLowerCase().startsWith("codigo")) {
            codPdv = "Código: " + codPdv;
        }
        holder.tvNumeroPedido.setText(codPdv != null ? codPdv : "Código: PDV-" + (position + 1));

        holder.tvBadgeNumero.setText(pdv.getBadgeNumero() != null ? pdv.getBadgeNumero() : String.valueOf(position + 1));

        if (holder.tvEstadoDespacho != null) {
            holder.tvEstadoDespacho.setVisibility(View.GONE);
        }

        cargarFotoPunto(holder.ivPuntoFoto, pdv);

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onPuntoDistribucionClick(pdv);
            } else {
                try {
                    Context ctx = v.getContext();
                    if (ctx == null) ctx = context;

                    Intent intent = new Intent(ctx, DetallePuntoDistribucionActivity.class);
                    if (!(ctx instanceof Activity)) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    intent.putExtra("ID_PDV", pdv.getId());
                    intent.putExtra("NOMBRE_PDV", pdv.getNombre());
                    intent.putExtra("CODIGO_PDV", pdv.getCodigo());
                    intent.putExtra("TIPO_NEGOCIO", pdv.getCriterioNegocio());
                    intent.putExtra("DIRECCION", pdv.getDireccion());
                    intent.putExtra("DISTANCIA", pdv.getDistancia());
                    intent.putExtra("ESTADO_PDV", pdv.getEstado());
                    intent.putExtra("BADGE_NUMERO", pdv.getBadgeNumero());
                    intent.putExtra("FOTO_URL_PDV", pdv.getFotoUrl());
                    ctx.startActivity(intent);
                } catch (Exception e) {
                    Log.e("PuntoDistribucionAdapter", "Error al abrir DetallePuntoDistribucionActivity: " + e.getMessage(), e);
                }
            }
        };

        if (holder.cardPuntoDistribucion != null) {
            holder.cardPuntoDistribucion.setOnClickListener(clickListener);
        }
        if (holder.containerContentLayout != null) {
            holder.containerContentLayout.setOnClickListener(clickListener);
        }
        if (holder.ivChevron != null) {
            holder.ivChevron.setOnClickListener(clickListener);
        }
        holder.itemView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() { return listaPuntos.size(); }

    private static final java.util.Map<Integer, android.graphics.Bitmap> cacheFotosPdv = new java.util.HashMap<>();

    public static android.graphics.Bitmap obtenerFotoCache(int idPdv) {
        return cacheFotosPdv.get(idPdv);
    }

    public static void guardarFotoCache(int idPdv, android.graphics.Bitmap bmp) {
        if (idPdv > 0 && bmp != null) {
            cacheFotosPdv.put(idPdv, bmp);
        }
    }

    private void cargarFotoPunto(ImageView iv, PuntoDistribucion pdv) {
        if (iv == null || pdv == null) return;
        int idPdv = pdv.getId();
        iv.setTag(idPdv);
        iv.setVisibility(View.VISIBLE);
        iv.setImageResource(R.drawable.ic_store);
        androidx.core.widget.ImageViewCompat.setImageTintList(iv, ColorStateList.valueOf(Color.parseColor("#8D6E63")));

        android.graphics.Bitmap cached = cacheFotosPdv.get(idPdv);
        if (cached != null) {
            mostrarBitmapEnItem(iv, cached);
            return;
        }

        int idProspectoOrigen = pdv.getIdProspectoOrigen();
        if (idProspectoOrigen > 0) {
            android.graphics.Bitmap cachedProspecto = ProspectoAdapter.obtenerFotoCache(idProspectoOrigen);
            if (cachedProspecto == null) {
                cachedProspecto = com.piccolifield.util.UserPhotoLoader.cargarFotoProspectoLocal(iv.getContext(), idProspectoOrigen);
                if (cachedProspecto != null) {
                    ProspectoAdapter.guardarFotoCache(idProspectoOrigen, cachedProspecto);
                }
            }
            if (cachedProspecto != null) {
                guardarFotoCache(idPdv, cachedProspecto);
                mostrarBitmapEnItem(iv, cachedProspecto);
                return;
            }
        }

        String fotoUrl = pdv.getFotoUrl();
        if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {
            android.graphics.Bitmap b64 = com.piccolifield.util.UserPhotoLoader.decodeBase64OrDataUri(fotoUrl);
            if (b64 != null) {
                guardarFotoCache(idPdv, b64);
                mostrarBitmapEnItem(iv, b64);
                return;
            }
            String fullUrl = com.piccolifield.util.UserPhotoLoader.buildFullImageUrl(fotoUrl);
            if (fullUrl != null) {
                descargarFotoPdv(iv, idPdv, idProspectoOrigen, fullUrl);
                return;
            }
        }

        if (idProspectoOrigen > 0) {
            com.piccolifield.data.remote.RetrofitClient.createService().obtenerProspecto(idProspectoOrigen)
                    .enqueue(new retrofit2.Callback<com.piccolifield.data.remote.dto.response.ApiResponse<com.piccolifield.model.Prospecto>>() {
                        @Override
                        public void onResponse(@NonNull retrofit2.Call<com.piccolifield.data.remote.dto.response.ApiResponse<com.piccolifield.model.Prospecto>> call,
                                               @NonNull retrofit2.Response<com.piccolifield.data.remote.dto.response.ApiResponse<com.piccolifield.model.Prospecto>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                String prospectoFoto = response.body().getData().getFotoUrl();
                                if (prospectoFoto != null && !prospectoFoto.trim().isEmpty()) {
                                    android.graphics.Bitmap b64 = com.piccolifield.util.UserPhotoLoader.decodeBase64OrDataUri(prospectoFoto);
                                    if (b64 != null) {
                                        guardarFotoCache(idPdv, b64);
                                        ProspectoAdapter.guardarFotoCache(idProspectoOrigen, b64);
                                        iv.post(() -> {
                                            if (Integer.valueOf(idPdv).equals(iv.getTag())) {
                                                mostrarBitmapEnItem(iv, b64);
                                            }
                                        });
                                        return;
                                    }
                                    String fullUrl = com.piccolifield.util.UserPhotoLoader.buildFullImageUrl(prospectoFoto);
                                    if (fullUrl != null) {
                                        descargarFotoPdv(iv, idPdv, idProspectoOrigen, fullUrl);
                                        return;
                                    }
                                }
                            }
                            String fallback = com.piccolifield.data.remote.RetrofitClient.BASE_SERVER_URL
                                    + "api/prospectos/" + idProspectoOrigen + "/foto";
                            descargarFotoPdv(iv, idPdv, idProspectoOrigen, fallback);
                        }

                        @Override
                        public void onFailure(@NonNull retrofit2.Call<com.piccolifield.data.remote.dto.response.ApiResponse<com.piccolifield.model.Prospecto>> call,
                                              @NonNull Throwable t) {
                        }
                    });
        }
    }

    private void descargarFotoPdv(ImageView iv, int idPdv, int idProspectoOrigen, String url) {
        com.piccolifield.data.remote.RetrofitClient.createService().descargarArchivoPorUrl(url)
                .enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<okhttp3.ResponseBody> call,
                                           @NonNull retrofit2.Response<okhttp3.ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            android.graphics.Bitmap bmp = com.piccolifield.util.UserPhotoLoader.decodeBitmapFromResponseBody(response.body());
                            if (bmp != null) {
                                guardarFotoCache(idPdv, bmp);
                                if (idProspectoOrigen > 0) {
                                    ProspectoAdapter.guardarFotoCache(idProspectoOrigen, bmp);
                                    com.piccolifield.util.UserPhotoLoader.guardarFotoProspectoLocal(iv.getContext(), idProspectoOrigen, bmp);
                                }
                                iv.post(() -> {
                                    if (Integer.valueOf(idPdv).equals(iv.getTag())) {
                                        mostrarBitmapEnItem(iv, bmp);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                    }
                });
    }

    private void mostrarBitmapEnItem(ImageView iv, android.graphics.Bitmap bmp) {
        iv.setImageBitmap(bmp);
        iv.setVisibility(View.VISIBLE);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardPuntoDistribucion;
        View containerContentLayout;
        ImageView ivChevron, ivPuntoFoto;
        TextView tvBadgeNumero, tvNombrePdv, tvDireccionPdv, tvNumeroPedido, tvEstadoDespacho;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPuntoDistribucion = itemView.findViewById(R.id.cardPuntoDistribucion);
            containerContentLayout = itemView.findViewById(R.id.containerContentLayout);
            ivChevron = itemView.findViewById(R.id.ivChevron);
            ivPuntoFoto = itemView.findViewById(R.id.ivPuntoFoto);
            tvBadgeNumero = itemView.findViewById(R.id.tvBadgeNumero);
            tvNombrePdv = itemView.findViewById(R.id.tvNombrePdv);
            tvDireccionPdv = itemView.findViewById(R.id.tvDireccionPdv);
            tvNumeroPedido = itemView.findViewById(R.id.tvNumeroPedido);
            tvEstadoDespacho = itemView.findViewById(R.id.tvEstadoDespacho);
        }
    }
}
