package com.piccolifield.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

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
    public static void registrarSesionActiva(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Usamos una LLAVE NUEVA exclusiva para el login
        editor.putBoolean("sesion_iniciada", true);
        editor.apply();
    }

    public static void limpiarSesionActiva(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("sesion_iniciada", false);
        editor.apply();
    }

    public static boolean consultarSesionActiva(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("sesion_iniciada", false);
    }
}
