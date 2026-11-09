package com.piccolifield.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.data.model.RendimientoMensualItem;

import java.util.List;

public class RendimientoMensualAdapter extends RecyclerView.Adapter<RendimientoMensualAdapter.ViewHolder> {

    private List<RendimientoMensualItem> lista;
    private Context context;

    public RendimientoMensualAdapter(List<RendimientoMensualItem> lista, Context context) {
        this.lista = lista;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rendimiento_mensual, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RendimientoMensualItem item = lista.get(position);

        holder.tvMesAnio.setText(item.getMesAnioFormatted());
        holder.tvMontoVendido.setText(String.format("S/ %.2f", item.getMontoVendido()));
        holder.tvMetaAsignada.setText(String.format("S/ %.2f", item.getMetaAsignadaEfectiva()));

        if ("EN_PROCESO".equalsIgnoreCase(item.getEstadoMeta())) {
            holder.tvBadgeMetaCumplida.setText("EN PROCESO");
            holder.tvBadgeMetaCumplida.setBackgroundResource(R.drawable.bg_badge_permission);
        } else if (item.isMetaCumplida()) {
            holder.tvBadgeMetaCumplida.setText("META CUMPLIDA");
            holder.tvBadgeMetaCumplida.setBackgroundResource(R.drawable.bg_badge_on_time);
        } else {
            holder.tvBadgeMetaCumplida.setText("NO ALCANZADA");
            holder.tvBadgeMetaCumplida.setBackgroundResource(R.drawable.bg_badge_late);
        }

        int porcentaje = (int) Math.round(item.getPorcentajeEfectivo());
        holder.pbProgresoMes.setProgress(porcentaje);
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMesAnio, tvBadgeMetaCumplida, tvMontoVendido, tvMetaAsignada;
        ProgressBar pbProgresoMes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMesAnio = itemView.findViewById(R.id.tvMesAnio);
            tvBadgeMetaCumplida = itemView.findViewById(R.id.tvBadgeMetaCumplida);
            tvMontoVendido = itemView.findViewById(R.id.tvMontoVendido);
            tvMetaAsignada = itemView.findViewById(R.id.tvMetaAsignada);
            pbProgresoMes = itemView.findViewById(R.id.pbProgresoMes);
        }
    }
}