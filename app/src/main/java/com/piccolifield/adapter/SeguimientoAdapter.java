package com.piccolifield.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.model.Seguimiento;

import java.util.List;

public class SeguimientoAdapter extends RecyclerView.Adapter<SeguimientoAdapter.ViewHolder> {

    private final List<Seguimiento> seguimientos;

    public SeguimientoAdapter(List<Seguimiento> seguimientos) {
        this.seguimientos = seguimientos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seguimiento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(seguimientos.get(position));
    }

    @Override
    public int getItemCount() {
        return seguimientos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFechaSeguimiento;
        private final TextView tvCambioEstado;
        private final TextView tvNotasSeguimiento;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFechaSeguimiento = itemView.findViewById(R.id.tvFechaSeguimiento);
            tvCambioEstado = itemView.findViewById(R.id.tvCambioEstado);
            tvNotasSeguimiento = itemView.findViewById(R.id.tvNotasSeguimiento);
        }

        public void bind(Seguimiento seguimiento) {
            tvFechaSeguimiento.setText(seguimiento.getFecha() != null ? seguimiento.getFecha() : "Sin fecha");
            
            if (seguimiento.getEstadoAnterior() != null && seguimiento.getEstadoNuevo() != null) {
                if (seguimiento.getEstadoAnterior().equals(seguimiento.getEstadoNuevo())) {
                    tvCambioEstado.setText(seguimiento.getEstadoNuevo());
                } else {
                    tvCambioEstado.setText(seguimiento.getEstadoAnterior() + " ➔ " + seguimiento.getEstadoNuevo());
                }
            } else {
                tvCambioEstado.setVisibility(View.GONE);
            }
            
            tvNotasSeguimiento.setText(seguimiento.getNotas() != null ? seguimiento.getNotas() : "");
        }
    }
}