package com.rkonline.android.notification;


import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationListenerService {

    private DatabaseReference reference;
    private Context context;

    public NotificationListenerService(Context context,String mobile) {
        this.context = context;
        reference = FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child("user_1"); // Change user ID
        Log.d("MobileNumber", "mobile"+mobile);
    }

    public void startListening() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String message = snapshot.child("message").getValue(String.class);

                    if (title != null && message != null) {
                        NotificationHelper.showNotification(context, title, message);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
}

