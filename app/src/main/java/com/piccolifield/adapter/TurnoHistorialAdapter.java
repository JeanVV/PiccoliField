package com.piccolifield.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.data.model.TurnoHistorial;
import com.piccolifield.databinding.ItemShiftHistoryBinding;

import java.util.List;

public class TurnoHistorialAdapter extends RecyclerView.Adapter<TurnoHistorialAdapter.ViewHolder> {

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

            String fh = item.getFechaHora();
            if (fh != null && fh.length() >= 16) {
                binding.tvShiftTime.setText(fh.substring(11, 16));
            } else if (fh != null) {
                binding.tvShiftTime.setText(fh);
            } else {
                binding.tvShiftTime.setText("--:--");
            }
        }
    }
}
