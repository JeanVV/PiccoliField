package com.piccolifield.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.data.model.PuntoVentaHistorial;

import java.util.List;

public class VendedorAsignacionAdapter extends RecyclerView.Adapter<VendedorAsignacionAdapter.ViewHolder> {

    private List<PuntoVentaHistorial> lista;
    private Context context;

    public VendedorAsignacionAdapter(List<PuntoVentaHistorial> lista, Context context) {
        this.lista = lista;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_historial_vendedor_asignacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PuntoVentaHistorial item = lista.get(position);

        holder.tvNombrePuntoVenta.setText(item.getPuntoVenta());
        
        String fechaFinText = item.getFechaFin() != null ? item.getFechaFin() : "Presente";
        holder.tvFechasAsignacion.setText("Desde: " + item.getFechaInicio() + " - " + fechaFinText);
        
        if (item.getMotivo() != null && !item.getMotivo().isEmpty()) {
            holder.tvMotivoAsignacion.setText("Motivo: " + item.getMotivo());
            holder.tvMotivoAsignacion.setVisibility(View.VISIBLE);
        } else {
            holder.tvMotivoAsignacion.setVisibility(View.GONE);
        }

        if (item.getRegistradoPor() != null && !item.getRegistradoPor().isEmpty()) {
            holder.tvRegistradoPor.setText("Registrado por: " + item.getRegistradoPor());
            holder.tvRegistradoPor.setVisibility(View.VISIBLE);
        } else {
            holder.tvRegistradoPor.setVisibility(View.GONE);
        }

        if (item.getEsActual() == 1) {
            holder.tvBadgeEstadoAsignacion.setVisibility(View.VISIBLE);
        } else {
            holder.tvBadgeEstadoAsignacion.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombrePuntoVenta, tvBadgeEstadoAsignacion, tvFechasAsignacion, tvMotivoAsignacion, tvRegistradoPor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombrePuntoVenta = itemView.findViewById(R.id.tvNombrePuntoVenta);
            tvBadgeEstadoAsignacion = itemView.findViewById(R.id.tvBadgeEstadoAsignacion);
            tvFechasAsignacion = itemView.findViewById(R.id.tvFechasAsignacion);
            tvMotivoAsignacion = itemView.findViewById(R.id.tvMotivoAsignacion);
            tvRegistradoPor = itemView.findViewById(R.id.tvRegistradoPor);
        }
    }
}