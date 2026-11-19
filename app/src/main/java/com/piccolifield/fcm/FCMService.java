package com.piccolifield.fcm;

import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.piccolifield.MenuActivity;
import com.piccolifield.sharedpreferences.FirebaseTokenManager;
import com.piccolifield.util.NotificationUtils;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.e("FCM TOKEN", token);

        String deviceName = Settings.Global.getString(getContentResolver(), "device_name");
        if (deviceName == null) {
            deviceName = Settings.Global.getString(getContentResolver(), "bluetooth_name");
        }

        Log.e("FCM Device name", deviceName);

        FirebaseTokenManager.setFirebaseToken(getApplicationContext(), token, deviceName);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = null;
        String body = null;
        String tipoNotificacion = null;

        // 1. Extraer título y cuerpo desde la notificación de Firebase
        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        }

        // 2. Extraer datos personalizados en data si existen
        if (!message.getData().isEmpty()) {
            if (title == null || title.trim().isEmpty()) {
                title = message.getData().get("title");
            }
            if (body == null || body.trim().isEmpty()) {
                body = message.getData().get("body");
            }
            tipoNotificacion = message.getData().get("tipo");
            if (tipoNotificacion == null) {
                tipoNotificacion = message.getData().get("type");
            }
        }

        // Título genérico neutro por defecto sólo si viene completamente vacío
        if (title == null) title = "PiccoliField";
        if (body == null) body = "Tienes una nueva notificación";

        String timestamp = message.getData().get("timestamp");

        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);

        // SOLO si el tipo es explícitamente de Asistencias o Justificaciones, agregamos el extra de navegación
        if ("RESUMEN_ASISTENCIA".equalsIgnoreCase(tipoNotificacion) || "RESUMEN_DIARIO".equalsIgnoreCase(tipoNotificacion)) {
            intent.putExtra("PANTALLA", "RESUMEN_ASISTENCIA");
        } else if ("JUSTIFICACION_RECIBIDA".equalsIgnoreCase(tipoNotificacion) || "JUSTIFICACION".equalsIgnoreCase(tipoNotificacion)) {
            intent.putExtra("PANTALLA", "JUSTIFICACION");
        }

        new NotificationUtils(getApplicationContext()).showNotificationMessage(title, body, timestamp, intent);
    }
}
