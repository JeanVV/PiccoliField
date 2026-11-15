package com.piccolifield.adapter;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.piccolifield.R;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.model.Producto;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductoCatalogoPedidoAdapter extends RecyclerView.Adapter<ProductoCatalogoPedidoAdapter.ViewHolder> {

    public interface OnAgregarProductoListener {
        void onAgregarProducto(Producto producto, int cantidad, double precioUnitario);
    }

    private final List<Producto> listaProductos;
    private boolean esMayorista;
    private final OnAgregarProductoListener listener;
    private static final Map<Integer, Bitmap> cacheFotos = new HashMap<>();

    public ProductoCatalogoPedidoAdapter(List<Producto> listaProductos, boolean esMayorista, OnAgregarProductoListener listener) {
        this.listaProductos = listaProductos;
        this.esMayorista = esMayorista;
        this.listener = listener;
    }

    public void setEsMayorista(boolean esMayorista) {
        this.esMayorista = esMayorista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_catalogo_pedido, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        if (producto == null) return;

        holder.tvNombreProductoCatalogo.setText(producto.getProducto());
        String detalle = producto.getSku() + " · " + producto.getCategoria() + " · " + producto.getPresentacion();
        holder.tvDetalleProductoCatalogo.setText(detalle);

        double precio = producto.getPrecioSegunCriterio(esMayorista);
        holder.tvPrecioProductoCatalogo.setText(String.format(Locale.US, "S/ %.2f", precio));

        if (esMayorista) {
            holder.tvBadgeCriterioPrecio.setText("Precio Mayorista");
            holder.tvBadgeCriterioPrecio.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            holder.tvBadgeCriterioPrecio.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvBadgeCriterioPrecio.setText("Precio Minorista");
            holder.tvBadgeCriterioPrecio.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F3E4C7")));
            holder.tvBadgeCriterioPrecio.setTextColor(Color.parseColor("#4A2B1C"));
        }

        // Imagen del producto con caché
        holder.ivFotoProductoCatalogo.setImageResource(R.drawable.botellasinicio);
        int idProd = producto.getIdProductos();
        if (idProd > 0) {
            if (cacheFotos.containsKey(idProd)) {
                holder.ivFotoProductoCatalogo.setImageBitmap(cacheFotos.get(idProd));
            } else {
                ApiService apiService = RetrofitClient.createService();
                apiService.obtenerFotoProducto(idProd).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                            if (bmp != null) {
                                cacheFotos.put(idProd, bmp);
                                holder.ivFotoProductoCatalogo.post(() -> holder.ivFotoProductoCatalogo.setImageBitmap(bmp));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        // Mantiene botellasinicio como foto por defecto
                    }
                });
            }
        }

        final int[] cantidadActual = {1};
        holder.tvCantidadCatalogo.setText("1");

        holder.btnRestarCantCatalogo.setOnClickListener(v -> {
            if (cantidadActual[0] > 1) {
                cantidadActual[0]--;
                holder.tvCantidadCatalogo.setText(String.valueOf(cantidadActual[0]));
            }
        });

        holder.btnSumarCantCatalogo.setOnClickListener(v -> {
            cantidadActual[0]++;
            holder.tvCantidadCatalogo.setText(String.valueOf(cantidadActual[0]));
        });

        holder.btnAgregarAlPedido.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAgregarProducto(producto, cantidadActual[0], precio);
                cantidadActual[0] = 1;
                holder.tvCantidadCatalogo.setText("1");
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos != null ? listaProductos.size() : 0;
    }

    public static Bitmap obtenerFotoCache(int idProd) {
        return cacheFotos.get(idProd);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFotoProductoCatalogo;
        TextView tvNombreProductoCatalogo, tvDetalleProductoCatalogo, tvBadgeCriterioPrecio, tvPrecioProductoCatalogo, tvCantidadCatalogo;
        MaterialButton btnRestarCantCatalogo, btnSumarCantCatalogo, btnAgregarAlPedido;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFotoProductoCatalogo = itemView.findViewById(R.id.ivFotoProductoCatalogo);
            tvNombreProductoCatalogo = itemView.findViewById(R.id.tvNombreProductoCatalogo);
            tvDetalleProductoCatalogo = itemView.findViewById(R.id.tvDetalleProductoCatalogo);
            tvBadgeCriterioPrecio = itemView.findViewById(R.id.tvBadgeCriterioPrecio);
            tvPrecioProductoCatalogo = itemView.findViewById(R.id.tvPrecioProductoCatalogo);
            tvCantidadCatalogo = itemView.findViewById(R.id.tvCantidadCatalogo);
            btnRestarCantCatalogo = itemView.findViewById(R.id.btnRestarCantCatalogo);
            btnSumarCantCatalogo = itemView.findViewById(R.id.btnSumarCantCatalogo);
            btnAgregarAlPedido = itemView.findViewById(R.id.btnAgregarAlPedido);
        }
    }
}
