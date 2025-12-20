package com.rkonline.android.notification;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rkonline.android.constant;


public class NFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("onMessageReceived","mesasge");
        Log.d("onMessageReceived",remoteMessage.getData().toString());

        if(!remoteMessage.getData().isEmpty()){
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            NotificationHelper.showNotification(
                    getApplicationContext(),
                    title,
                    message
            );
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM_TOKEN", token);
        // Example: save under logged-in user
        String userId = getSharedPreferences(constant.prefs, MODE_PRIVATE).getString("mobile", null);
        if (userId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", token);
            SharedPreferences.Editor editor =
                    getSharedPreferences(constant.prefs, MODE_PRIVATE).edit();

            editor.putString("fcmToken",token);
            editor.apply();
        }
        // Send this token to server / Firebase DB
        // saveTokenToDatabase(token);
    }
}
