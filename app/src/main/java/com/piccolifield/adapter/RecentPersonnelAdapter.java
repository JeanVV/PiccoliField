package com.piccolifield.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.databinding.ItemRecentPersonnelBinding;

import java.util.ArrayList;
import java.util.List;

public class RecentPersonnelAdapter extends RecyclerView.Adapter<RecentPersonnelAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(RegistroAsistencia item);
    }

    private List<RegistroAsistencia> listaOriginal;
    private List<RegistroAsistencia> lista;
    private OnItemClickListener listener;

    public RecentPersonnelAdapter(List<RegistroAsistencia> lista) {
        this.listaOriginal = new ArrayList<>(lista != null ? lista : new ArrayList<>());
        this.lista = new ArrayList<>(listaOriginal);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void filtrar(String query, String filtroEstado) {
        lista.clear();
        String q = query != null ? query.toLowerCase().trim() : "";
        String estado = filtroEstado != null ? filtroEstado.toUpperCase().trim() : "";

        for (RegistroAsistencia item : listaOriginal) {
            boolean coincideNombre = q.isEmpty() ||
                    (item.getNombreCompleto() != null && item.getNombreCompleto().toLowerCase().contains(q)) ||
                    (item.getSede() != null && item.getSede().toLowerCase().contains(q));

            boolean coincideEstado = estado.isEmpty() || "TODOS".equals(estado) ||
                    (item.getEstado() != null && item.getEstado().equalsIgnoreCase(estado));

            if (coincideNombre && coincideEstado) {
                lista.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecentPersonnelBinding binding = ItemRecentPersonnelBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RegistroAsistencia item = lista.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return lista == null ? 0 : lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemRecentPersonnelBinding binding;

        ViewHolder(ItemRecentPersonnelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RegistroAsistencia item, OnItemClickListener listener) {
            binding.tvPersonnelName.setText(item.getNombreCompleto());
            binding.tvPersonnelLocation.setText(item.getSede());

            // Manejo seguro de la hora
            String hora = item.getHoraMarcado();
            if (hora != null && hora.length() >= 16) {
                binding.tvPersonnelTime.setText(hora.substring(11, 16));
            } else if (hora != null) {
                binding.tvPersonnelTime.setText(hora);
            } else {
                binding.tvPersonnelTime.setText("--:--");
            }

            configurarBadge(binding.tvPersonnelStatusBadge, item.getEstado());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    private static void configurarBadge(TextView badge, String estado) {
        if (estado == null) {
            badge.setText("Desconocido");
            return;
        }

        switch (estado.toUpperCase()) {
            case "A_TIEMPO":
                badge.setBackgroundResource(R.drawable.bg_badge_on_time);
                badge.setTextColor(badge.getContext().getColor(R.color.status_on_time_text));
                badge.setText("A tiempo");
                break;
            case "TARDANZA":
                badge.setBackgroundResource(R.drawable.bg_badge_late);
                badge.setTextColor(badge.getContext().getColor(R.color.status_late_text));
                badge.setText("Tardanza");
                break;
            case "FALTA":
                badge.setBackgroundResource(R.drawable.bg_badge_absent);
                badge.setTextColor(badge.getContext().getColor(R.color.status_absent_text));
                badge.setText("Falta");
                break;
            default:
                badge.setBackgroundResource(R.drawable.bg_badge_permission);
                badge.setTextColor(badge.getContext().getColor(R.color.status_permission_text));
                badge.setText(estado);
                break;
        }
    }
}
