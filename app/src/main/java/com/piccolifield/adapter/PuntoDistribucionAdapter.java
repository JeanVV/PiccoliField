package com.piccolifield.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardPuntoDistribucion;
        View containerContentLayout;
        ImageView ivChevron;
        TextView tvBadgeNumero, tvNombrePdv, tvDireccionPdv, tvNumeroPedido, tvEstadoDespacho;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPuntoDistribucion = itemView.findViewById(R.id.cardPuntoDistribucion);
            containerContentLayout = itemView.findViewById(R.id.containerContentLayout);
            ivChevron = itemView.findViewById(R.id.ivChevron);
            tvBadgeNumero = itemView.findViewById(R.id.tvBadgeNumero);
            tvNombrePdv = itemView.findViewById(R.id.tvNombrePdv);
            tvDireccionPdv = itemView.findViewById(R.id.tvDireccionPdv);
            tvNumeroPedido = itemView.findViewById(R.id.tvNumeroPedido);
            tvEstadoDespacho = itemView.findViewById(R.id.tvEstadoDespacho);
        }
    }
}
