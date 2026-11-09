package com.piccolifield.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.DetallePuntoVentaSupervisionActivity;
import com.piccolifield.R;
import com.piccolifield.model.SupervisionPuntoVenta;

import java.util.ArrayList;
import java.util.List;

public class SupervisionPuntoVentaAdapter extends RecyclerView.Adapter<SupervisionPuntoVentaAdapter.ViewHolder> {

    private final List<SupervisionPuntoVenta> listaOriginal;
    private final List<SupervisionPuntoVenta> listaFiltrada;
    private Context context;

    public SupervisionPuntoVentaAdapter(List<SupervisionPuntoVenta> listaPdv, Context context) {
        this.listaOriginal = new ArrayList<>(listaPdv != null ? listaPdv : new ArrayList<>());
        this.listaFiltrada = new ArrayList<>(this.listaOriginal);
        this.context = context;
    }

    public void filtrar(String query) {
        listaFiltrada.clear();
        if (query == null || query.trim().isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            String q = query.toLowerCase().trim();
            for (SupervisionPuntoVenta p : listaOriginal) {
                if (p != null) {
                    boolean matchNombre = p.getNombre() != null && p.getNombre().toLowerCase().contains(q);
                    boolean matchDireccion = p.getDireccion() != null && p.getDireccion().toLowerCase().contains(q);
                    if (matchNombre || matchDireccion) {
                        listaFiltrada.add(p);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supervision_puntoventa, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SupervisionPuntoVenta pdv = listaFiltrada.get(position);
        if (pdv == null) return;

        if (holder.tvNombrePdv != null) {
            holder.tvNombrePdv.setText(pdv.getNombre() != null ? pdv.getNombre() : "Punto de Venta");
        }
        if (holder.tvDireccionPdv != null) {
            holder.tvDireccionPdv.setText(pdv.getDireccion() != null ? pdv.getDireccion() : "Sin dirección");
        }
        if (holder.tvEstadoPdv != null) {
            holder.tvEstadoPdv.setText(pdv.getEstado() != null ? pdv.getEstado() : "ACTIVO");
            holder.tvEstadoPdv.setTextColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (context != null) {
                Intent intent = new Intent(context, DetallePuntoVentaSupervisionActivity.class);
                intent.putExtra("ID_PDV", pdv.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() { return listaFiltrada != null ? listaFiltrada.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombrePdv, tvDireccionPdv, tvEstadoPdv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombrePdv = itemView.findViewById(R.id.tvNombrePdv);
            tvDireccionPdv = itemView.findViewById(R.id.tvDireccionPdv);
            tvEstadoPdv = itemView.findViewById(R.id.tvEstadoPdv);
        }
    }
}