package com.piccolifield.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.model.Prospecto;

import java.util.List;

public class ProspectoAdapter extends RecyclerView.Adapter<ProspectoAdapter.ViewHolder> {

    private final List<Prospecto> prospectos;
    private final OnProspectoClickListener listener;

    public interface OnProspectoClickListener {
        void onProspectoClick(Prospecto prospecto);
    }

    public ProspectoAdapter(List<Prospecto> prospectos, OnProspectoClickListener listener) {
        this.prospectos = prospectos;
        this.listener = listener;
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
        holder.bind(prospecto, listener);
    }

    @Override
    public int getItemCount() {
        return prospectos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProspectoNombre;
        private final TextView tvProspectoZona;
        private final TextView tvProspectoEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProspectoNombre = itemView.findViewById(R.id.tvProspectoNombre);
            tvProspectoZona = itemView.findViewById(R.id.tvProspectoZona);
            tvProspectoEstado = itemView.findViewById(R.id.tvProspectoEstado);
        }

        public void bind(Prospecto prospecto, OnProspectoClickListener listener) {
            tvProspectoNombre.setText(prospecto.getNombre() != null ? prospecto.getNombre() : "Sin nombre");
            
            String zonaTipo = "";
            if (prospecto.getZona() != null) zonaTipo += prospecto.getZona();
            if (prospecto.getTipo() != null) zonaTipo += " · " + prospecto.getTipo();
            tvProspectoZona.setText(zonaTipo);
            
            tvProspectoEstado.setText(prospecto.getEstado() != null ? prospecto.getEstado() : "NUEVO");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProspectoClick(prospecto);
                }
            });
        }
    }
}