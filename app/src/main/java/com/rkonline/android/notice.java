package com.rkonline.android;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class notice extends AppCompatActivity {

    protected latonormal text;

    ViewDialog progressDialog;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_notice);
        initView();
        findViewById(R.id.back).setOnClickListener(v -> finish());
        db = FirebaseFirestore.getInstance();
        apicall();
    }


    private void apicall() {

        progressDialog = new ViewDialog(notice.this);
        progressDialog.showDialog();

        db.collection("app_config").document("notice")
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    progressDialog.hideDialog();

                    if (documentSnapshot.exists()) {
                        String html = documentSnapshot.getString("content");
                        if (html == null) html = "<p>Content not available</p>";

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            text.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            text.setText(Html.fromHtml(html));
                        }

                    } else {
                        text.setText("Something went wrong.");
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.hideDialog();
                    text.setText("Something went wrong.");
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void initView() {
        text = findViewById(R.id.text);
    }
}