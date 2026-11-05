package com.piccolifield.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

public class FirebaseTokenManager {

    private static final String PREF_NAME = "firebase_preferences";
    private static final String KEY_TOKEN = "fcm_token";
    private static final String KEY_DEVICE_NAME = "device_name";

    public static void setFirebaseToken(Context context, String token, String deviceName) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_DEVICE_NAME, deviceName)
                .apply();
    }

    public static String getFirebaseToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_TOKEN, null);
    }

    public static String getDeviceName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_DEVICE_NAME, null);
    }
}