package com.piccolifield.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.data.model.Incidencia;

import java.util.ArrayList;
import java.util.List;

public class IncidenciaAdapter extends RecyclerView.Adapter<IncidenciaAdapter.ViewHolder> {

    public interface OnIncidenciaClickListener {
        void onIncidenciaClick(Incidencia incidencia);
    }

    private final List<Incidencia> listaOriginal;
    private final List<Incidencia> listaFiltrada;
    private Context context;
    private OnIncidenciaClickListener listener;

    public IncidenciaAdapter(List<Incidencia> listaIncidencias, Context context, OnIncidenciaClickListener listener) {
        this.listaOriginal = new ArrayList<>(listaIncidencias != null ? listaIncidencias : new ArrayList<>());
        this.listaFiltrada = new ArrayList<>(this.listaOriginal);
        this.context = context;
        this.listener = listener;
    }

    public void filtrar(String query) {
        listaFiltrada.clear();
        if (query == null || query.trim().isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            String q = query.toLowerCase().trim();
            for (Incidencia i : listaOriginal) {
                if (i != null) {
                    boolean matchTipo = i.getTipo().toLowerCase().contains(q);
                    boolean matchDesc = i.getDescripcion().toLowerCase().contains(q);
                    boolean matchLocal = i.getLocalNombre().toLowerCase().contains(q);
                    if (matchTipo || matchDesc || matchLocal) {
                        listaFiltrada.add(i);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incidencia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Incidencia item = listaFiltrada.get(position);
        if (item == null) return;

        holder.tvTipoIncidencia.setText(item.getTipo().replace("_", " "));
        
        String infoLocalImpulsador = item.getLocalNombre();
        if (item.getResponsableNombre() != null && !item.getResponsableNombre().isEmpty()) {
            infoLocalImpulsador += " • " + item.getResponsableNombre();
        }
        holder.tvLocalIncidencia.setText(infoLocalImpulsador);
        
        holder.tvDescripcionIncidencia.setText(item.getDescripcion());
        holder.tvFechaIncidencia.setText(item.getFechaRegistro());

        String estado = item.getEstado();
        holder.tvEstadoIncidencia.setText(estado);
        if ("RESUELTO".equalsIgnoreCase(estado) || "ATENDIDA".equalsIgnoreCase(estado)) {
            holder.tvEstadoIncidencia.setBackgroundColor(Color.parseColor("#388E3C"));
        } else if ("EN_PROCESO".equalsIgnoreCase(estado)) {
            holder.tvEstadoIncidencia.setBackgroundColor(Color.parseColor("#F57C00"));
        } else {
            holder.tvEstadoIncidencia.setBackgroundColor(Color.parseColor("#D32F2F"));
        }

        String severidad = item.getSeveridad();
        holder.tvSeveridadIncidencia.setText("Severidad: " + severidad);
        if ("ALTA".equalsIgnoreCase(severidad) || "CRÍTICA".equalsIgnoreCase(severidad)) {
            holder.tvSeveridadIncidencia.setTextColor(Color.parseColor("#D32F2F"));
        } else if ("MEDIA".equalsIgnoreCase(severidad)) {
            holder.tvSeveridadIncidencia.setTextColor(Color.parseColor("#F57C00"));
        } else {
            holder.tvSeveridadIncidencia.setTextColor(Color.parseColor("#388E3C"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onIncidenciaClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return listaFiltrada != null ? listaFiltrada.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipoIncidencia, tvEstadoIncidencia, tvLocalIncidencia, tvDescripcionIncidencia, tvSeveridadIncidencia, tvFechaIncidencia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipoIncidencia = itemView.findViewById(R.id.tvTipoIncidencia);
            tvEstadoIncidencia = itemView.findViewById(R.id.tvEstadoIncidencia);
            tvLocalIncidencia = itemView.findViewById(R.id.tvLocalIncidencia);
            tvDescripcionIncidencia = itemView.findViewById(R.id.tvDescripcionIncidencia);
            tvSeveridadIncidencia = itemView.findViewById(R.id.tvSeveridadIncidencia);
            tvFechaIncidencia = itemView.findViewById(R.id.tvFechaIncidencia);
        }
    }
}