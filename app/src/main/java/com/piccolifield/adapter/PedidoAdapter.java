package com.piccolifield.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.model.Pedido;

import java.util.List;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.ViewHolder> {

    private final List<Pedido> pedidos;
    private final OnPedidoClickListener listener;

    public interface OnPedidoClickListener {
        void onPedidoClick(Pedido pedido);
    }

    public PedidoAdapter(List<Pedido> pedidos, OnPedidoClickListener listener) {
        this.pedidos = pedidos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);
        holder.bind(pedido, listener);
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPedidoId;
        private final TextView tvPedidoFecha;
        private final TextView tvPedidoEstado;
        private final TextView tvPedidoCantidad;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPedidoId = itemView.findViewById(R.id.tvPedidoId);
            tvPedidoFecha = itemView.findViewById(R.id.tvPedidoFecha);
            tvPedidoEstado = itemView.findViewById(R.id.tvPedidoEstado);
            tvPedidoCantidad = itemView.findViewById(R.id.tvPedidoCantidad);
        }

        public void bind(Pedido pedido, OnPedidoClickListener listener) {
            tvPedidoId.setText("Pedido #" + pedido.getIdPedido());
            tvPedidoFecha.setText(pedido.getFecha() != null ? pedido.getFecha() : "Sin fecha");
            
            // Si el backend aún no manda estado, simulamos uno
            tvPedidoEstado.setText("Registrado"); 
            tvPedidoCantidad.setText(pedido.getCantSolicitada() + " und.");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPedidoClick(pedido);
                }
            });
        }
    }
}