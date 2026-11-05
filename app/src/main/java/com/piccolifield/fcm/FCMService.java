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

        if (message.getNotification() != null) {
            Log.e("FCM message title", message.getNotification().getTitle());
            Log.e("FCM message body", message.getNotification().getBody());

            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();
            String timestamp = message.getData().get("timestamp");

            Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
            new NotificationUtils(getApplicationContext()).showNotificationMessage(title, body, timestamp, intent);
        }
    }
}