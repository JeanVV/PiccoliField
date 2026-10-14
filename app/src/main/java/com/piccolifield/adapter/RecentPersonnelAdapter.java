package com.piccolifield.adapter; // Asegúrate de que el paquete sea el correcto

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.databinding.ItemRecentPersonnelBinding;

import java.util.List;

public class RecentPersonnelAdapter extends RecyclerView.Adapter<RecentPersonnelAdapter.ViewHolder> {

    private List<RegistroAsistencia> lista;

    public RecentPersonnelAdapter(List<RegistroAsistencia> lista) {
        this.lista = lista;
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
        holder.bind(item);
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

        void bind(RegistroAsistencia item) {
            binding.tvPersonnelName.setText(item.getNombreCompleto());
            binding.tvPersonnelLocation.setText(item.getSede());

            // Manejo seguro de la hora
            String hora = item.getHoraMarcado();
            if (hora != null && hora.length() >= 16) {
                binding.tvPersonnelTime.setText(hora.substring(11, 16));
            } else {
                binding.tvPersonnelTime.setText("--:--");
            }

            // Ahora sí puede llamar al método static
            configurarBadge(binding.tvPersonnelStatusBadge, item.getEstado());
        }
    }

    // 🔑 AGREGA "static" AQUÍ
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
            default: // PERMISO u otro
                badge.setBackgroundResource(R.drawable.bg_badge_permission);
                badge.setTextColor(badge.getContext().getColor(R.color.status_permission_text));
                badge.setText(estado);
                break;
        }
    }
}