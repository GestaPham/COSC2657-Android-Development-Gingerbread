package com.gingerbread.asm3.Services;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationServiceHttp {
    private static final String TAG = "NotificationServiceHttp";
    private static final String FUNCTION_URL = "https://sendpushnotification-esvhjsnkha-uc.a.run.app";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface NotificationCallback {
        void onSuccess(String messageId);

        void onFailure(String errorMessage);
    }

    public void sendPushNotification(String fcmToken, String title, String body, NotificationCallback callback) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            callback.onFailure("Invalid FCM token.");
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("fcmToken", fcmToken);
            json.put("title", title);
            json.put("body", body);
        } catch (Exception e) {
            callback.onFailure("Failed to create JSON payload: " + e.getMessage());
            return;
        }

        RequestBody requestBody = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .url(FUNCTION_URL)
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to send notification: " + e.getMessage());
                callback.onFailure("Error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("HTTP Error: " + response.code());
                    return;
                }

                String responseBody = response.body().string();
                Log.d(TAG, "Notification sent successfully: " + responseBody);
                callback.onSuccess(responseBody);
            }
        });
    }

    public void requestNotificationPermission(Context context, ActivityResultLauncher<String> permissionLauncher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission already granted.");
                return;
            }
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        } else {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            if (!notificationManagerCompat.areNotificationsEnabled()) {
                Log.e(TAG, "Notifications are disabled. Ask the user to enable them in settings.");
            } else {
                Log.d(TAG, "Notifications are already enabled.");
            }
        }
    }
}
