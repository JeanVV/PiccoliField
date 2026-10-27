package com.piccolifield.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.piccolifield.DetalleVendedorActivity;
import com.piccolifield.R;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.model.Vendedor;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        holder.tvPosicion.setText(String.valueOf(vendedor.getPosicion()));
        holder.tvNombreLocal.setText(textoCombinado);
        holder.tvVolumenVentas.setText(vendedor.getCantVentas() + " productos");

        if (vendedor.getIdUsuario() > 0) {
            ApiService apiService = RetrofitClient.createService();
            apiService.obtenerFotoImpulsador(vendedor.getIdUsuario()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        holder.ivAvatar.post(() -> holder.ivAvatar.setImageBitmap(bmp));
                    } else {
                        holder.ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    holder.ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            });
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }

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
        return listaVendedores.size();
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
