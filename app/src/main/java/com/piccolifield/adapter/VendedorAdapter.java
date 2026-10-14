package com.piccolifield.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.DetalleVendedorActivity;
import com.piccolifield.R;
import com.piccolifield.model.Vendedor;

import java.util.List;

public class VendedorAdapter extends RecyclerView.Adapter<VendedorAdapter.ViewHolder> {

    private List<Vendedor> listaVendedores;
    private Context context;

    public VendedorAdapter(List<Vendedor> listaVendedores, Context context) {
        this.listaVendedores = listaVendedores;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Aquí "inflamos" (cargamos) el diseño XML del item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vendedor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vendedor vendedor = listaVendedores.get(position);
        String textoCombinado = vendedor.getNombre() + " - " + vendedor.getLocal(); // o vendedor.getLocal_pertenece()

        holder.tvNombreLocal.setText(textoCombinado);
        holder.tvVolumenVentas.setText(vendedor.getCantVentas() + " productos");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleVendedorActivity.class);
            intent.putExtra("ID_VENDEDOR", vendedor.getIdUsuario());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaVendedores.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosicion, tvNombreLocal, tvVolumenVentas;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosicion = itemView.findViewById(R.id.tvPosicion);
            tvNombreLocal = itemView.findViewById(R.id.tvNombreLocal);
            tvVolumenVentas = itemView.findViewById(R.id.tvVolumenVentas);
        }
    }
}
