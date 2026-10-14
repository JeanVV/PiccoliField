package com.piccolifield.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.DetallePuntoVentaActivity;
import com.piccolifield.R;
import com.piccolifield.model.PuntoVenta;

import java.util.List;

public class PuntoVentaAdapter extends RecyclerView.Adapter<PuntoVentaAdapter.ViewHolder> {

    private List<PuntoVenta> listaPuntos;
    private Context context;

    public PuntoVentaAdapter(List<PuntoVenta> listaPuntos, Context context) {
        this.listaPuntos = listaPuntos;
        this.context = context;
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

        holder.tvNombrePdv.setText(pdv.getNombre());
        holder.tvDireccionPdv.setText(pdv.getDireccion());
        
        String numPed = pdv.getNumeroPedido();
        if (numPed != null && !numPed.startsWith("Nº Pedido")) {
            numPed = "Nº Pedido: " + numPed;
        }
        holder.tvNumeroPedido.setText(numPed != null ? numPed : "Nº Pedido: 0245");

        holder.tvBadgeNumero.setText(pdv.getBadgeNumero());

        String estadoDisp = pdv.getEstadoDespacho();
        if (estadoDisp == null || estadoDisp.isEmpty()) {
            estadoDisp = "En preparación";
        }
        holder.tvEstadoDespacho.setText(estadoDisp);

        // Estilizado dinámico según el estado del pedido
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

        // Al hacer clic, abre el detalle del punto
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetallePuntoVentaActivity.class);
            intent.putExtra("ID_PDV", pdv.getId());
            intent.putExtra("NOMBRE_PDV", pdv.getNombre());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return listaPuntos.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBadgeNumero, tvNombrePdv, tvDireccionPdv, tvNumeroPedido, tvEstadoDespacho;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBadgeNumero = itemView.findViewById(R.id.tvBadgeNumero);
            tvNombrePdv = itemView.findViewById(R.id.tvNombrePdv);
            tvDireccionPdv = itemView.findViewById(R.id.tvDireccionPdv);
            tvNumeroPedido = itemView.findViewById(R.id.tvNumeroPedido);
            tvEstadoDespacho = itemView.findViewById(R.id.tvEstadoDespacho);
        }
    }
}