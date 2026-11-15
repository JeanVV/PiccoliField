package com.piccolifield.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.data.model.NotificacionItem;

import java.util.List;

public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.ViewHolder> {

    public interface OnNotificacionClickListener {
        void onNotificacionClick(NotificacionItem item);
    }

    private List<NotificacionItem> lista;
    private Context context;
    private OnNotificacionClickListener listener;

    public NotificacionAdapter(List<NotificacionItem> lista, Context context, OnNotificacionClickListener listener) {
        this.lista = lista;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notificacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificacionItem item = lista.get(position);
        if (item == null) return;

        holder.tvTituloNotif.setText(item.getTitulo());
        holder.tvMensajeNotif.setText(item.getMensaje());
        holder.tvFechaNotif.setText(item.getFecha());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotificacionClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloNotif, tvMensajeNotif, tvFechaNotif;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloNotif = itemView.findViewById(R.id.tvTituloNotif);
            tvMensajeNotif = itemView.findViewById(R.id.tvMensajeNotif);
            tvFechaNotif = itemView.findViewById(R.id.tvFechaNotif);
        }
    }
}