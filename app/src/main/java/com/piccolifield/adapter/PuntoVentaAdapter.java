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
import com.piccolifield.DetallePuntoVentaActivity;
import com.piccolifield.R;
import com.piccolifield.model.PuntoVenta;

import java.util.List;

public class PuntoVentaAdapter extends RecyclerView.Adapter<PuntoVentaAdapter.ViewHolder> {

    public interface OnPuntoVentaClickListener {
        void onPuntoVentaClick(PuntoVenta pdv);
    }

    private List<PuntoVenta> listaPuntos;
    private Context context;
    private OnPuntoVentaClickListener listener;

    public PuntoVentaAdapter(List<PuntoVenta> listaPuntos, Context context) {
        this(listaPuntos, context, null);
    }

    public PuntoVentaAdapter(List<PuntoVenta> listaPuntos, Context context, OnPuntoVentaClickListener listener) {
        this.listaPuntos = listaPuntos;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_punto_venta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PuntoVenta pdv = listaPuntos.get(position);
        if (pdv == null) return;

        holder.tvNombrePdv.setText(pdv.getNombre() != null ? pdv.getNombre() : "Punto de Venta");
        holder.tvDireccionPdv.setText(pdv.getDireccion() != null ? pdv.getDireccion() : "Sin dirección");
        
        String numPed = pdv.getNumeroPedido();
        if (numPed != null && !numPed.startsWith("Nº Pedido")) {
            numPed = "Nº Pedido: " + numPed;
        }
        holder.tvNumeroPedido.setText(numPed != null ? numPed : "Nº Pedido: 0245");

        holder.tvBadgeNumero.setText(pdv.getBadgeNumero() != null ? pdv.getBadgeNumero() : String.valueOf(position + 1));

        String estadoDisp = pdv.getEstadoDespacho();
        if (estadoDisp == null || estadoDisp.isEmpty()) {
            estadoDisp = "En preparación";
        }
        holder.tvEstadoDespacho.setText(estadoDisp);

        // Estilizado dinámico según el estado del pedido
        try {
            if ("Pendiente".equalsIgnoreCase(estadoDisp)) {
                holder.tvEstadoDespacho.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FADBD8")));
                holder.tvEstadoDespacho.setTextColor(Color.parseColor("#C0392B"));
            } else if ("En camino".equalsIgnoreCase(estadoDisp)) {
                holder.tvEstadoDespacho.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FDEBD0")));
                holder.tvEstadoDespacho.setTextColor(Color.parseColor("#B9783E"));
            } else if ("En preparación".equalsIgnoreCase(estadoDisp)) {
                holder.tvEstadoDespacho.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FDEBD0")));
                holder.tvEstadoDespacho.setTextColor(Color.parseColor("#9C640C"));
            } else {
                holder.tvEstadoDespacho.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
                holder.tvEstadoDespacho.setTextColor(Color.parseColor("#2E7D32"));
            }
        } catch (Exception e) {
            Log.e("PuntoVentaAdapter", "Error aplicando colores: " + e.getMessage());
        }

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onPuntoVentaClick(pdv);
            } else {
                try {
                    Context ctx = v.getContext();
                    if (ctx == null) ctx = context;

                    Intent intent = new Intent(ctx, DetallePuntoVentaActivity.class);
                    if (!(ctx instanceof Activity)) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    intent.putExtra("ID_PDV", pdv.getId());
                    intent.putExtra("NOMBRE_PDV", pdv.getNombre());
                    intent.putExtra("DIRECCION", pdv.getDireccion());
                    intent.putExtra("DISTANCIA", pdv.getDistancia());
                    intent.putExtra("NUMERO_PEDIDO", pdv.getNumeroPedido());
                    intent.putExtra("ESTADO_DESPACHO", pdv.getEstadoDespacho());
                    intent.putExtra("BADGE_NUMERO", pdv.getBadgeNumero());
                    ctx.startActivity(intent);
                } catch (Exception e) {
                    Log.e("PuntoVentaAdapter", "Error al abrir DetallePuntoVentaActivity: " + e.getMessage(), e);
                }
            }
        };

        if (holder.cardPuntoVenta != null) {
            holder.cardPuntoVenta.setOnClickListener(clickListener);
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
        MaterialCardView cardPuntoVenta;
        View containerContentLayout;
        ImageView ivChevron;
        TextView tvBadgeNumero, tvNombrePdv, tvDireccionPdv, tvNumeroPedido, tvEstadoDespacho;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPuntoVenta = itemView.findViewById(R.id.cardPuntoVenta);
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