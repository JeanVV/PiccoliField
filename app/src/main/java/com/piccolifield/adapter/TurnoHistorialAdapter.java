package com.piccolifield.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.data.model.TurnoHistorial;
import com.piccolifield.databinding.ItemShiftHistoryBinding;

import java.util.List;

public class TurnoHistorialAdapter extends RecyclerView.Adapter<TurnoHistorialAdapter.ViewHolder> {

    private static final java.util.Locale LOCALE_PERU = new java.util.Locale("es", "PE");
    private List<TurnoHistorial> lista;

    public TurnoHistorialAdapter(List<TurnoHistorial> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShiftHistoryBinding binding = ItemShiftHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TurnoHistorial item = lista.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return lista == null ? 0 : lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemShiftHistoryBinding binding;

        ViewHolder(ItemShiftHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TurnoHistorial item) {
            String tipoText = item.getTipoEvento();
            binding.tvShiftType.setText(tipoText != null ? tipoText : "Marcado");

            if ("Entrada".equalsIgnoreCase(tipoText)) {
                binding.ivShiftBullet.setImageResource(R.drawable.ic_check_circle_green);
            } else {
                binding.ivShiftBullet.setImageResource(R.drawable.ic_warning_alert);
            }

            String fh = item.getFechaHora();
            binding.tvShiftDate.setText(formatearFecha(fh));
            binding.tvShiftTime.setText(formatearHora(fh));
        }

        private String formatearFecha(String str) {
            if (str == null || str.trim().isEmpty()) return "Fecha: --";
            try {
                String s = str.trim().replace('T', ' ');
                if (s.contains(" ")) {
                    s = s.split(" ")[0];
                }
                if (s.length() >= 10) {
                    java.text.SimpleDateFormat inFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", LOCALE_PERU);
                    java.text.SimpleDateFormat outFormat = new java.text.SimpleDateFormat("d 'de' MMM. yyyy", LOCALE_PERU);
                    java.util.Date date = inFormat.parse(s.substring(0, 10));
                    if (date != null) {
                        return outFormat.format(date);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return str;
        }

        private String formatearHora(String str) {
            if (str == null || str.trim().isEmpty()) return "--:--";
            try {
                String s = str.trim().replace('T', ' ');
                if (s.contains(" ")) {
                    String[] parts = s.split(" ");
                    for (String part : parts) {
                        if (part.contains(":")) {
                            s = part;
                            break;
                        }
                    }
                }
                if (s.contains(":")) {
                    String[] timeParts = s.split(":");
                    if (timeParts.length >= 2) {
                        int hh = Integer.parseInt(timeParts[0].trim());
                        int mm = Integer.parseInt(timeParts[1].trim());
                        String amPm = (hh >= 12) ? "p. m." : "a. m.";
                        int hh12 = hh % 12;
                        if (hh12 == 0) hh12 = 12;
                        return String.format(java.util.Locale.US, "%02d:%02d %s", hh12, mm, amPm);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return str;
        }
    }
}
