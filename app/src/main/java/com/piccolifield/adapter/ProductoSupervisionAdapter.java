package com.piccolifield.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.piccolifield.R;
import com.piccolifield.data.model.PuntoVentaProducto;
import java.util.List;

public class ProductoSupervisionAdapter extends RecyclerView.Adapter<ProductoSupervisionAdapter.ViewHolder> {

    private List<PuntoVentaProducto> listaProductos;
    private Context context;

    public ProductoSupervisionAdapter(List<PuntoVentaProducto> listaProductos, Context context) {
        this.listaProductos = listaProductos;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto_supervision, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PuntoVentaProducto producto = listaProductos.get(position);
        holder.tvNombreProducto.setText(producto.getProducto());
        holder.tvSkuProducto.setText("SKU: " + producto.getSku());
        holder.tvStockMinimo.setText("Stock Mínimo: " + producto.getStockMinimo());

        if (producto.getIdProductos() > 0) {
            com.piccolifield.data.remote.api.ApiService apiService = com.piccolifield.data.remote.dto.RetrofitClient.createService();
            apiService.obtenerFotoProducto(producto.getIdProductos()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                        holder.ivProducto.post(() -> holder.ivProducto.setImageBitmap(bitmap));
                    } else {
                        holder.ivProducto.setImageResource(R.drawable.ic_avatar_placeholder);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    holder.ivProducto.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            });
        } else {
            holder.ivProducto.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return listaProductos != null ? listaProductos.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProducto;
        TextView tvNombreProducto, tvSkuProducto, tvStockMinimo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProducto = itemView.findViewById(R.id.ivProducto);
            tvNombreProducto = itemView.findViewById(R.id.tvNombreProducto);
            tvSkuProducto = itemView.findViewById(R.id.tvSkuProducto);
            tvStockMinimo = itemView.findViewById(R.id.tvStockMinimo);
        }
    }
}