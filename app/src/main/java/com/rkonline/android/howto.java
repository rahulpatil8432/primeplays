package com.rkonline.android;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class howto extends AppCompatActivity {

    protected latonormal text;
    FirebaseFirestore db;
    ViewDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_howto);

        initView();
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.back).setOnClickListener(v -> finish());

        loadHowToPlay();
    }

    private void loadHowToPlay() {

        progressDialog = new ViewDialog(howto.this);
        progressDialog.showDialog();

        db.collection("app_config").document("how_to_play")
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    progressDialog.hideDialog();   // 🔥 IMPORTANT

                    if (documentSnapshot.exists()) {
                        String html = documentSnapshot.getString("content");
                        if (html == null) html = "<p>Content not available</p>";

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            text.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            text.setText(Html.fromHtml(html));
                        }

                    } else {
                        text.setText("How to play content not found.");
                    }
                })
                .addOnFailureListener(e -> {

                    progressDialog.hideDialog();   // 🔥 ALSO IMPORTANT

                    text.setText("Unable to load content.");
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void initView() {
        text = findViewById(R.id.text);
    }
}
