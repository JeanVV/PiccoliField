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

import java.util.List;

public class AlertaAdapter extends RecyclerView.Adapter<AlertaAdapter.ViewHolder> {

    private List<Alerta> listaAlertas;
    private Context context;

    public AlertaAdapter(List<Alerta> listaAlertas, Context context) {
        this.listaAlertas = listaAlertas;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alerta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Alerta alerta = listaAlertas.get(position);

        holder.tvTituloAlerta.setText(alerta.getTitulo());
        holder.tvLocalAlerta.setText(alerta.getLocal());
        holder.tvDetalleAlerta.setText(alerta.getDetalle());
        holder.tvTiempoAlerta.setText(alerta.getTiempo());
        holder.tvSeveridadAlerta.setText(alerta.getSeveridad());

        // Cambiar color si es Crítica o Pendiente
        if (alerta.getSeveridad().equalsIgnoreCase("Crítica")) {
            holder.tvSeveridadAlerta.setTextColor(Color.parseColor("#D32F2F")); // Rojo
        } else {
            holder.tvSeveridadAlerta.setTextColor(Color.parseColor("#F57C00")); // Naranja
        }

        // Clic en el botón para ver detalles
        holder.btnVerDetalleAlerta.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleAlertaActivity.class);
            intent.putExtra("ID_ALERTA", alerta.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return listaAlertas.size(); }

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