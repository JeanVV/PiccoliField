package com.piccolifield.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.piccolifield.R;
import com.piccolifield.model.ItemNuevoPedido;

import java.util.List;
import java.util.Locale;

public class ItemPedidoSeleccionadoAdapter extends RecyclerView.Adapter<ItemPedidoSeleccionadoAdapter.ViewHolder> {

    public interface OnItemPedidoChangeListener {
        void onCantidadCambiada();
        void onItemEliminado(int position);
    }

    private final List<ItemNuevoPedido> items;
    private final OnItemPedidoChangeListener listener;

    public ItemPedidoSeleccionadoAdapter(List<ItemNuevoPedido> items, OnItemPedidoChangeListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_seleccionado_pedido, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemNuevoPedido item = items.get(position);
        if (item == null || item.getProducto() == null) return;

        holder.tvNombreItemSeleccionado.setText(item.getProducto().getProducto());
        holder.tvPrecioUnitarioItem.setText(String.format(Locale.US, "S/ %.2f c/u", item.getPrecioUnitario()));
        holder.tvCantidadItemLista.setText(String.valueOf(item.getCantidad()));
        holder.tvSubtotalItem.setText(String.format(Locale.US, "S/ %.2f", item.getSubtotal()));

        Bitmap cached = ProductoCatalogoPedidoAdapter.obtenerFotoCache(item.getProducto().getIdProductos());
        if (cached != null) {
            holder.ivFotoItemSeleccionado.setImageBitmap(cached);
        } else {
            holder.ivFotoItemSeleccionado.setImageResource(R.drawable.botellasinicio);
        }

        holder.btnMenosItemLista.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ItemNuevoPedido actual = items.get(pos);
            if (actual.getCantidad() > 1) {
                actual.setCantidad(actual.getCantidad() - 1);
                notifyItemChanged(pos);
                if (listener != null) listener.onCantidadCambiada();
            } else {
                if (listener != null) listener.onItemEliminado(pos);
            }
        });

        holder.btnMasItemLista.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ItemNuevoPedido actual = items.get(pos);
            actual.setCantidad(actual.getCantidad() + 1);
            notifyItemChanged(pos);
            if (listener != null) listener.onCantidadCambiada();
        });

        holder.tvQuitarItem.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (listener != null) listener.onItemEliminado(pos);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFotoItemSeleccionado;
        TextView tvNombreItemSeleccionado, tvPrecioUnitarioItem, tvCantidadItemLista, tvSubtotalItem, tvQuitarItem;
        MaterialButton btnMenosItemLista, btnMasItemLista;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFotoItemSeleccionado = itemView.findViewById(R.id.ivFotoItemSeleccionado);
            tvNombreItemSeleccionado = itemView.findViewById(R.id.tvNombreItemSeleccionado);
            tvPrecioUnitarioItem = itemView.findViewById(R.id.tvPrecioUnitarioItem);
            tvCantidadItemLista = itemView.findViewById(R.id.tvCantidadItemLista);
            tvSubtotalItem = itemView.findViewById(R.id.tvSubtotalItem);
            tvQuitarItem = itemView.findViewById(R.id.tvQuitarItem);
            btnMenosItemLista = itemView.findViewById(R.id.btnMenosItemLista);
            btnMasItemLista = itemView.findViewById(R.id.btnMasItemLista);
        }
    }
}
