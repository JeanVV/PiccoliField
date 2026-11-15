package com.piccolifield.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.piccolifield.model.Pedido;
import com.piccolifield.model.PuntoDistribucion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PuntosDistribucionStorage {

    private static final String PREF_NAME = "puntos_distribucion_prefs";
    private static final String KEY_PUNTOS_LOCALES = "puntos_locales";
    private static final String KEY_PEDIDOS_LOCALES_PREFIX = "pedidos_pdv_";

    public static void guardarPuntoLocal(Context context, PuntoDistribucion nuevoPunto) {
        if (context == null || nuevoPunto == null) return;
        List<PuntoDistribucion> lista = obtenerPuntosLocales(context);
        boolean actualizado = false;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId() == nuevoPunto.getId() && nuevoPunto.getId() > 0) {
                lista.set(i, nuevoPunto);
                actualizado = true;
                break;
            }
        }
        if (!actualizado) {
            lista.add(0, nuevoPunto);
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PUNTOS_LOCALES, new Gson().toJson(lista)).apply();
    }

    public static List<PuntoDistribucion> obtenerPuntosLocales(Context context) {
        if (context == null) return new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PUNTOS_LOCALES, null);
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            Type type = new TypeToken<List<PuntoDistribucion>>() {}.getType();
            List<PuntoDistribucion> list = new Gson().fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static PuntoDistribucion buscarPuntoLocalPorId(Context context, int idPdv) {
        for (PuntoDistribucion p : obtenerPuntosLocales(context)) {
            if (p.getId() == idPdv) {
                return p;
            }
        }
        return null;
    }

    public static void guardarPedidoLocal(Context context, int idPdv, Pedido nuevoPedido) {
        if (context == null || nuevoPedido == null) return;
        List<Pedido> lista = obtenerPedidosLocales(context, idPdv);
        lista.add(0, nuevoPedido);
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PEDIDOS_LOCALES_PREFIX + idPdv, new Gson().toJson(lista)).apply();
    }

    public static List<Pedido> obtenerPedidosLocales(Context context, int idPdv) {
        if (context == null) return new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PEDIDOS_LOCALES_PREFIX + idPdv, null);
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            Type type = new TypeToken<List<Pedido>>() {}.getType();
            List<Pedido> list = new Gson().fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
