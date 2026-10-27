package com.piccolifield.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.R;
import com.piccolifield.data.model.PuntoVentaHistorial;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PuntoVentaHistorialAdapter extends RecyclerView.Adapter<PuntoVentaHistorialAdapter.ViewHolder> {

    private List<PuntoVentaHistorial> listaHistorial;
    private Context context;

    public PuntoVentaHistorialAdapter(List<PuntoVentaHistorial> listaHistorial, Context context) {
        this.listaHistorial = listaHistorial;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_historial_asignacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PuntoVentaHistorial item = listaHistorial.get(position);

        holder.tvImpulsadorNombre.setText(item.getImpulsadorNombre());
        
        String fechaFinText = item.getFechaFin() != null ? item.getFechaFin() : "Presente";
        holder.tvFechas.setText("Desde: " + item.getFechaInicio() + " - " + fechaFinText);
        
        if (item.getMotivo() != null && !item.getMotivo().isEmpty()) {
            holder.tvMotivo.setText("Motivo: " + item.getMotivo());
            holder.tvMotivo.setVisibility(View.VISIBLE);
        } else {
            holder.tvMotivo.setVisibility(View.GONE);
        }

        if (item.getRegistradoPor() != null && !item.getRegistradoPor().isEmpty()) {
            holder.tvRegistradoPor.setText("Registrado por: " + item.getRegistradoPor());
            holder.tvRegistradoPor.setVisibility(View.VISIBLE);
        } else {
            holder.tvRegistradoPor.setVisibility(View.GONE);
        }

        if (item.getEsActual() == 1) {
            holder.tvBadgeActual.setVisibility(View.VISIBLE);
        } else {
            holder.tvBadgeActual.setVisibility(View.GONE);
        }

        if (item.getIdUsuario() > 0) {
            ApiService apiService = RetrofitClient.createService();
            apiService.obtenerFotoImpulsador(item.getIdUsuario()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        holder.ivImpulsadorFoto.post(() -> holder.ivImpulsadorFoto.setImageBitmap(bmp));
                    } else {
                        holder.ivImpulsadorFoto.setImageResource(R.drawable.ic_avatar_placeholder);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    holder.ivImpulsadorFoto.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            });
        } else {
            holder.ivImpulsadorFoto.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return listaHistorial != null ? listaHistorial.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImpulsadorFoto;
        TextView tvImpulsadorNombre, tvFechas, tvMotivo, tvRegistradoPor, tvBadgeActual;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImpulsadorFoto = itemView.findViewById(R.id.ivImpulsadorFoto);
            tvImpulsadorNombre = itemView.findViewById(R.id.tvImpulsadorNombre);
            tvFechas = itemView.findViewById(R.id.tvFechas);
            tvMotivo = itemView.findViewById(R.id.tvMotivo);
            tvRegistradoPor = itemView.findViewById(R.id.tvRegistradoPor);
            tvBadgeActual = itemView.findViewById(R.id.tvBadgeActual);
        }
    }
}