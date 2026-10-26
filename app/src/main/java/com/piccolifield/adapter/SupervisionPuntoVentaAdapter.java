package com.piccolifield.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.model.SupervisionPuntoVenta;

import java.util.List;

public class SupervisionPuntoVentaAdapter extends RecyclerView.Adapter<SupervisionPuntoVentaAdapter.ViewHolder> {

    private List<SupervisionPuntoVenta> listaPdv;
    private Context context;

    public SupervisionPuntoVentaAdapter(List<SupervisionPuntoVenta> listaPdv, Context context) {
        this.listaPdv = listaPdv;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_supervision_puntoventa, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SupervisionPuntoVenta pdv = listaPdv.get(position);

        holder.tvNombrePdv.setText(pdv.getNombre());
        holder.tvDireccionPdv.setText(pdv.getDireccion());
        holder.tvEstadoPdv.setText(pdv.getEstado());
        holder.tvEstadoPdv.setTextColor(Color.WHITE);
    }

    @Override
    public int getItemCount() { return listaPdv.size(); }

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