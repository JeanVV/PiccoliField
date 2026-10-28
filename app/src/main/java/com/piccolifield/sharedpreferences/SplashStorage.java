package com.piccolifield.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.piccolifield.data.model.RegistroAsistencia;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SplashStorage {
    private static final String PREFS_NAME = "campusgo_ingreso_app";

    public static void registrarIngreso(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("ingreso", true);
        editor.apply();
    }

    public static void limpiarIngreso(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static boolean consultarIngreso(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("ingreso", false);
    }

    public static void registrarSesionActiva(Context context, String rol) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("sesion_iniciada", true);
        editor.putString("user_rol", rol != null ? rol : "");
        editor.putString("user_role", rol != null ? rol : "");
        editor.apply();
    }

    public static void registrarSesionActiva(Context context) {
        registrarSesionActiva(context, "SUPERVISOR");
    }

    public static boolean consultarSesionActiva(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("sesion_iniciada", false);
    }

    public static void limpiarSesionActiva(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("sesion_iniciada", false);
        editor.remove("auth_token");
        editor.remove("user_role");
        editor.remove("user_rol");
        editor.remove("user_id");
        editor.remove("user_name");
        editor.remove("user_email");
        editor.apply();
    }

    public static void guardarToken(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("auth_token", token);
        editor.apply();
    }

    public static String obtenerToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("auth_token", "");
    }

    public static void guardarRol(Context context, String rol) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_role", rol != null ? rol : "");
        editor.putString("user_rol", rol != null ? rol : "");
        editor.apply();
    }

    public static String obtenerRol(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String rol = sharedPreferences.getString("user_role", "");
        if (rol.isEmpty()) {
            rol = sharedPreferences.getString("user_rol", "");
        }
        return rol;
    }

    public static void guardarUsuarioId(Context context, int idUsuario) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("user_id", idUsuario);
        editor.apply();
    }

    public static int obtenerUsuarioId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", 0);
    }

    public static void guardarNombreUsuario(Context context, String nombre) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", nombre != null ? nombre : "");
        editor.apply();
    }

    public static String obtenerNombreUsuario(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("user_name", "");
    }

    public static void guardarCorreoUsuario(Context context, String correo) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_email", correo != null ? correo : "");
        editor.apply();
    }

    public static String obtenerCorreoUsuario(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("user_email", "");
    }

    public static void guardarRegistroAsistenciaLocal(Context context, RegistroAsistencia registro) {
        if (context == null || registro == null) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        List<RegistroAsistencia> lista = obtenerRegistrosAsistenciaLocales(context);

        boolean actualizado = false;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getIdUsuario() == registro.getIdUsuario()) {
                if (registro.getJustificacion() == null && lista.get(i).getJustificacion() != null) {
                    registro.setJustificacion(lista.get(i).getJustificacion());
                    registro.setMotivoJustificacion(lista.get(i).getMotivoJustificacion());
                }
                lista.set(i, registro);
                actualizado = true;
                break;
            }
        }
        if (!actualizado) {
            lista.add(0, registro);
        }

        String json = gson.toJson(lista);
        sharedPreferences.edit().putString("registros_asistencia_locales", json).apply();
    }

    public static List<RegistroAsistencia> obtenerRegistrosAsistenciaLocales(Context context) {
        if (context == null) return new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("registros_asistencia_locales", "");
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<RegistroAsistencia>>(){}.getType();
            List<RegistroAsistencia> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void guardarJustificacionLocal(Context context, int idUsuario, String motivo, String descripcion, String rutaSustento) {
        if (context == null || idUsuario == 0) return;
        List<RegistroAsistencia> lista = obtenerRegistrosAsistenciaLocales(context);
        for (RegistroAsistencia reg : lista) {
            if (reg.getIdUsuario() == idUsuario) {
                reg.setMotivoJustificacion(motivo);
                reg.setJustificacion(descripcion);
                reg.setRutaSustento(rutaSustento);
                reg.setEstadoJustificacion("ENVIADA");
                guardarRegistroAsistenciaLocal(context, reg);
                return;
            }
        }
        RegistroAsistencia reg = new RegistroAsistencia();
        reg.setJustificacion(descripcion);
        reg.setMotivoJustificacion(motivo);
        reg.setRutaSustento(rutaSustento);
        reg.setEstadoJustificacion("ENVIADA");
        guardarRegistroAsistenciaLocal(context, reg);
    }
}
