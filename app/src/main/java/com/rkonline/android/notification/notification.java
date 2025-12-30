package com.rkonline.android.notification;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rkonline.android.R;
import com.rkonline.android.adapter.NotificationAdapter;
import com.rkonline.android.constant;
import com.rkonline.android.model.NotificationModel;
import com.rkonline.android.utils.AlertHelper;

import java.util.ArrayList;

public class notification extends AppCompatActivity {

    RecyclerView recyclerView;
    NotificationAdapter adapter;
    ArrayList<NotificationModel> notificationList;
    FirebaseFirestore db;
    ProgressBar progressBar; // add this


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.notificationRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        loadNotificationData();
        enableSwipeToDelete();
    }

    private void loadNotificationData() {
        notificationList = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        String mobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("mobile", "");
        db.collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {

                            String title = doc.getString("title");
                            String message = doc.getString("message");
                            boolean addToList = false;
                            boolean hasUserIds = doc.contains("userIds");

                            if (doc.contains("userIds")) {
                                java.util.List<String> userIds = (java.util.List<String>) doc.get("userIds");
                                if (userIds != null && userIds.contains(mobile)) {
                                    addToList = true;
                                }
                            } else {
                                addToList = true;
                            }

                            if (!addToList) continue;

                            com.google.firebase.Timestamp timestamp = doc.getTimestamp("expireAt");
                            String formattedDate = "";
                            if (timestamp != null) {
                                java.util.Calendar cal = java.util.Calendar.getInstance();
                                cal.setTime(timestamp.toDate());
                                cal.add(java.util.Calendar.DAY_OF_YEAR, -7); // subtract 7 days

                                java.text.SimpleDateFormat sdf =
                                        new java.text.SimpleDateFormat("dd MMM yyyy • hh:mm a");
                                formattedDate = sdf.format(cal.getTime());
                            }

                            notificationList.add(new NotificationModel(title, message, formattedDate,doc.getId(),hasUserIds));
                        }

                        adapter = new NotificationAdapter(notificationList);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    e.printStackTrace();
                });
    }


    private void enableSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        NotificationModel notification = notificationList.get(position);

                        String docId = notification.getDocId();

                        db.collection("notifications").document(docId)
                                .get().addOnSuccessListener(doc -> {
                                    if (doc.contains("userIds")) {
                                        String mobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                                                .getString("mobile", "");
                                        db.collection("notifications").document(docId)
                                                .update("userIds", com.google.firebase.firestore.FieldValue.arrayRemove(mobile))
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(notification.this, "Notification Deleted!", Toast.LENGTH_SHORT).show();
                                                    adapter.removeItem(position);
                                                });
                                    } else {
                                        adapter.notifyItemChanged(position);
                                    }
                                });
                    }

                    @Override
                    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
                        NotificationModel notification = notificationList.get(viewHolder.getAdapterPosition());
                        return notification.isHasUserIds() ? super.getSwipeThreshold(viewHolder) : 1.5f;
                    }
                };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

}
