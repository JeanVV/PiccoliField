package com.piccolifield.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.DetalleVendedorActivity;
import com.piccolifield.R;
import com.piccolifield.model.Vendedor;
import com.piccolifield.util.UserPhotoLoader;

import java.util.ArrayList;
import java.util.List;

public class VendedorAdapter extends RecyclerView.Adapter<VendedorAdapter.ViewHolder> {

    private List<Vendedor> listaOriginal;
    private List<Vendedor> listaFiltrada;
    private Context context;

    public VendedorAdapter(List<Vendedor> listaVendedores, Context context) {
        this.listaOriginal = new ArrayList<>(listaVendedores);
        this.listaFiltrada = new ArrayList<>(listaVendedores);
        this.context = context;
    }

    public void filtrar(String query) {
        listaFiltrada.clear();
        if (query == null || query.trim().isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            String q = query.toLowerCase().trim();
            for (Vendedor v : listaOriginal) {
                if ((v.getNombre() != null && v.getNombre().toLowerCase().contains(q)) ||
                        (v.getLocal() != null && v.getLocal().toLowerCase().contains(q))) {
                    listaFiltrada.add(v);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vendedor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vendedor vendedor = listaFiltrada.get(position);
        String textoCombinado = vendedor.getNombre() + " - " + vendedor.getLocal();

        holder.tvPosicion.setText(String.valueOf(vendedor.getPosicion()));
        holder.tvNombreLocal.setText(textoCombinado);
        holder.tvVolumenVentas.setText(vendedor.getCantVentas() + " productos");

        UserPhotoLoader.loadUserPhoto(
                context,
                vendedor.getIdUsuario(),
                vendedor.getUrlFoto(),
                vendedor.getNombre(),
                holder.ivAvatar,
                R.drawable.ic_avatar_placeholder
        );

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleVendedorActivity.class);
            intent.putExtra("ID_VENDEDOR", vendedor.getIdUsuario());
            intent.putExtra("CANTIDAD_VENDIDA", vendedor.getCantVentas());
            intent.putExtra("META_ASIGNADA", vendedor.getMeta_asignada());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaFiltrada != null ? listaFiltrada.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosicion, tvNombreLocal, tvVolumenVentas;
        ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosicion = itemView.findViewById(R.id.tvPosicion);
            tvNombreLocal = itemView.findViewById(R.id.tvNombreLocal);
            tvVolumenVentas = itemView.findViewById(R.id.tvVolumenVentas);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}
