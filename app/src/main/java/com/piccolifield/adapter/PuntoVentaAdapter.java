package com.piccolifield.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.DetallePuntoVentaActivity;
import com.piccolifield.R;
import com.piccolifield.model.PuntoVenta;

import java.util.List;

public class PuntoVentaAdapter extends RecyclerView.Adapter<PuntoVentaAdapter.ViewHolder> {

    private List<PuntoVenta> listaPuntos;
    private Context context;

    public PuntoVentaAdapter(List<PuntoVenta> listaPuntos, Context context) {
        this.listaPuntos = listaPuntos;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_punto_venta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PuntoVenta pdv = listaPuntos.get(position);

        holder.tvNombrePdv.setText(pdv.getNombre());
        holder.tvDireccionPdv.setText("📍 " + pdv.getDireccion());
        holder.tvEstadoPdv.setText(pdv.getEstado());

        // Al hacer clic, vamos al detalle de la tienda
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetallePuntoVentaActivity.class);
            intent.putExtra("ID_PDV", pdv.getId());
            intent.putExtra("NOMBRE_PDV", pdv.getNombre());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return listaPuntos.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombrePdv, tvDireccionPdv, tvEstadoPdv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombrePdv = itemView.findViewById(R.id.tvNombrePdv);
            tvDireccionPdv = itemView.findViewById(R.id.tvDireccionPdv);
            tvEstadoPdv = itemView.findViewById(R.id.tvEstadoPdv);
        }
    }
}
