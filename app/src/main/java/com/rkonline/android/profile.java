package com.rkonline.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class profile extends AppCompatActivity {

    protected EditText name;
    protected EditText email;
    protected EditText mobile;
    protected latobold submit;

    SharedPreferences prefs;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initView();
        db = FirebaseFirestore.getInstance();

        prefs = getSharedPreferences(constant.prefs, MODE_PRIVATE);

        // Pre-fill UI
        name.setText(prefs.getString("name", ""));
        email.setText(prefs.getString("email", ""));
        mobile.setText(prefs.getString("mobile", "")); // read-only field

        // Back button
        findViewById(R.id.back).setOnClickListener(v -> finish());

        submit.setOnClickListener(v -> {
            if (name.getText().toString().isEmpty()) {
                name.setError("Enter name");
                return;
            }
            if (email.getText().toString().isEmpty()) {
                email.setError("Enter email");
                return;
            }
            updateProfile();
        });
    }

    private void updateProfile() {
        String nm = name.getText().toString();
        String mail = email.getText().toString();
        String mob = mobile.getText().toString(); // Document ID in Firestore

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", nm);
        updates.put("email", mail);

        db.collection("users")
                .document(mob)
                .update(updates)
                .addOnSuccessListener(aVoid -> {

                    // Save locally
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("name", nm);
                    editor.putString("email", mail);
                    editor.apply();

                    Toast.makeText(profile.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(profile.this,
                                "Update failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void initView() {
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        mobile = findViewById(R.id.mobile);
        submit = findViewById(R.id.submit);
    }
}
