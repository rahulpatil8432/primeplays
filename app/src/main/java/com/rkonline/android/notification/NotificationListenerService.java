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
    private String mobile;
    public NotificationListenerService(Context context,String mobile) {
        this.context = context;
        reference = FirebaseDatabase.getInstance()
                .getReference("notifications");
        this.mobile = mobile;
//                .child(mobile); // Change user ID
        Log.d("MobileNumber", "mobile"+mobile);
    }

    public void startListening() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean admin = snapshot.child("admin").child("title").getValue(String.class) != null;
                String refRemove = admin ? "admin" : mobile;
                if (snapshot.exists()) {
                    String title = snapshot.child(refRemove).child("title").getValue(String.class);
                    String message = snapshot.child(refRemove).child("message").getValue(String.class);

                    if (title != null && message != null) {
                        NotificationHelper.showNotification(context, title, message);
                        DatabaseReference dbRef =
                                FirebaseDatabase.getInstance().getReference("notifications").child(refRemove);
                        dbRef.removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
}

