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

        // Asignamos los datos a los textos
        holder.tvPosicion.setText(String.valueOf(vendedor.getPosicion()));
        holder.tvNombreLocal.setText(vendedor.getNombre() + " - " + vendedor.getLocal());

        holder.tvVolumenVentas.setText(vendedor.getCantVentas() + " ventas");

        // Configuramos el clic en el ítem completo para ir a la interfaz de Rendimiento
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleVendedorActivity.class);
            // Opcional: puedes enviar el nombre del vendedor a la otra pantalla
            intent.putExtra("NOMBRE_VENDEDOR", vendedor.getNombre());
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
