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

import com.google.android.material.button.MaterialButton;
import com.piccolifield.DetalleAlertaActivity;
import com.piccolifield.R;
import com.piccolifield.model.Alerta;

import java.util.ArrayList;
import java.util.List;

public class AlertaAdapter extends RecyclerView.Adapter<AlertaAdapter.ViewHolder> {

    private List<Alerta> listaOriginal;
    private List<Alerta> listaFiltrada;
    private Context context;
    private String filtroEstado = "TODOS";
    private String filtroNivel = "TODOS";
    private String textoBusqueda = "";

    public AlertaAdapter(List<Alerta> listaAlertas, Context context) {
        this.listaOriginal = new ArrayList<>(listaAlertas);
        this.listaFiltrada = new ArrayList<>(listaAlertas);
        this.context = context;
    }

    public void filtrarPorTexto(String query) {
        this.textoBusqueda = query != null ? query.toLowerCase().trim() : "";
        aplicarFiltros(this.filtroEstado, this.filtroNivel);
    }

    public void aplicarFiltros(String estadoFiltro, String nivelFiltro) {
        if (estadoFiltro != null) this.filtroEstado = estadoFiltro;
        if (nivelFiltro != null) this.filtroNivel = nivelFiltro;

        listaFiltrada.clear();
        for (Alerta a : listaOriginal) {
            boolean coincideEstado = "TODOS".equalsIgnoreCase(this.filtroEstado) ||
                    (a.getEstado() != null && a.getEstado().equalsIgnoreCase(this.filtroEstado));

            boolean coincideNivel = "TODOS".equalsIgnoreCase(this.filtroNivel) ||
                    (a.getSeveridad() != null && a.getSeveridad().equalsIgnoreCase(this.filtroNivel));

            boolean coincideTexto = this.textoBusqueda.isEmpty() ||
                    (a.getLocal() != null && a.getLocal().toLowerCase().contains(this.textoBusqueda)) ||
                    (a.getNombreProducto() != null && a.getNombreProducto().toLowerCase().contains(this.textoBusqueda)) ||
                    (a.getSeveridad() != null && a.getSeveridad().toLowerCase().contains(this.textoBusqueda));

            if (coincideEstado && coincideNivel && coincideTexto) {
                listaFiltrada.add(a);
            }
        }
        notifyDataSetChanged();
    }

    public void filtrar(String estadoFiltro) {
        aplicarFiltros(estadoFiltro, this.filtroNivel);
    }

    public void filtrarPorNivel(String nivelFiltro) {
        aplicarFiltros(this.filtroEstado, nivelFiltro);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alerta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Alerta alerta = listaFiltrada.get(position);

        holder.tvTituloAlerta.setText(alerta.getNivelFormateado());
        holder.tvLocalAlerta.setText(alerta.getLocal());
        holder.tvDetalleAlerta.setText(alerta.getDetalle());
        holder.tvTiempoAlerta.setText(alerta.getTiempo());
        
        String estado = alerta.getEstado() != null ? alerta.getEstado() : "PENDIENTE";
        holder.tvSeveridadAlerta.setText(estado);

        if ("ATENDIDA".equalsIgnoreCase(estado) || "RESUELTA".equalsIgnoreCase(estado)) {
            holder.tvSeveridadAlerta.setTextColor(Color.parseColor("#388E3C")); // Verde (Atendida SIEMPRE en verde)
        } else {
            holder.tvSeveridadAlerta.setTextColor(Color.parseColor("#D32F2F")); // Rojo (Pendiente SIEMPRE en rojo)
        }

        View.OnClickListener listener = v -> {
            Intent intent = new Intent(context, DetalleAlertaActivity.class);
            intent.putExtra("ID_ALERTA", alerta.getId());
            context.startActivity(intent);
        };

        holder.btnVerDetalleAlerta.setOnClickListener(listener);
        holder.itemView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() { return listaFiltrada != null ? listaFiltrada.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloAlerta, tvLocalAlerta, tvDetalleAlerta, tvTiempoAlerta, tvSeveridadAlerta;
        MaterialButton btnVerDetalleAlerta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloAlerta = itemView.findViewById(R.id.tvTituloAlerta);
            tvLocalAlerta = itemView.findViewById(R.id.tvLocalAlerta);
            tvDetalleAlerta = itemView.findViewById(R.id.tvDetalleAlerta);
            tvTiempoAlerta = itemView.findViewById(R.id.tvTiempoAlerta);
            tvSeveridadAlerta = itemView.findViewById(R.id.tvSeveridadAlerta);
            btnVerDetalleAlerta = itemView.findViewById(R.id.btnVerDetalleAlerta);
        }
    }
}