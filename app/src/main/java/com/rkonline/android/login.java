package com.rkonline.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class login extends AppCompatActivity {

    private ImageView draw;
    private EditText mobile, edtOtp;
    private latobold submit, verifyOtp;
    private TextView create;
    private boolean isSigningIn = false;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    private String storedVerificationId = "";
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private static final String ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initView();

        // Create account
        create.setOnClickListener(v ->
                startActivity(new Intent(login.this, signup.class)));

        // Send OTP
        submit.setOnClickListener(v -> {
            String mob = mobile.getText().toString().trim();

            if (mob.isEmpty()) {
                mobile.setError("Enter mobile number");
                return;
            }

            checkUserExistsThenSendOtp(mob);
        });

        // Verify OTP
        verifyOtp.setOnClickListener(v -> {
            String code = edtOtp.getText().toString().trim();
            if (code.isEmpty()) {
                edtOtp.setError("Enter OTP");
            } else {
                verifyCode(code);
            }
        });
    }

    // Step 1 → Check if user exists before sending OTP
    private void checkUserExistsThenSendOtp(String mob) {

        db.collection("users")
                .document(mob)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(login.this, "User not registered", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sendVerificationCode(formatPhone(mob));
                });
    }

    // Step 2 → Send OTP
    private void sendVerificationCode(String phoneNumber) {

        // Emulator test number auto-fill
        if (phoneNumber.equals("+919999999999")) {
            edtOtp.setText("123456");
        }

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        // Show OTP UI
        edtOtp.setVisibility(View.VISIBLE);
        verifyOtp.setVisibility(View.VISIBLE);
        submit.setVisibility(View.GONE);

        Toast.makeText(this, "OTP Sent", Toast.LENGTH_SHORT).show();
    }

    // Phone number formatter
    private String formatPhone(String mob) {
        if (!mob.startsWith("+")) {
            return "+91" + mob;
        }
        return mob;
    }

    // Firebase OTP Callbacks
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

                    edtOtp.setVisibility(View.GONE);
                    verifyOtp.setVisibility(View.GONE);

                    signInWithPhoneAuthCredential(credential);
                }


                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(login.this,
                            "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {

                    storedVerificationId = verificationId;
                    resendToken = token;

                    edtOtp.setVisibility(View.VISIBLE);
                    verifyOtp.setVisibility(View.VISIBLE);
                    submit.setVisibility(View.GONE);

                    Toast.makeText(login.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                }
            };

    // Step 3 → Enter OTP
    private void verifyCode(String code) {

        if (storedVerificationId == null || storedVerificationId.isEmpty()) {
            Toast.makeText(this,
                    "OTP not ready yet. Please wait.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(storedVerificationId, code);

        signInWithPhoneAuthCredential(credential);
    }


    // Step 4 → Firebase signs in user
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        if (isSigningIn) return;
        isSigningIn = true;

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        fetchUserDataAndLogin();
                    } else {
                        isSigningIn = false;
                        Toast.makeText(login.this,
                                "Invalid OTP: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Step 5 → Fetch user details from Firestore and save session
    private void fetchUserDataAndLogin() {

        String mob = mobile.getText().toString().trim();

        db.collection("users")
                .document(mob)
                .get()
                .addOnSuccessListener(this::handleUserLogin)
                .addOnFailureListener(e ->
                        Toast.makeText(login.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // Step 6 → Save session + prefs + go to MainActivity
    private void handleUserLogin(DocumentSnapshot document) {

        if (!document.exists()) {
            Toast.makeText(login.this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String mob = mobile.getText().toString().trim();
        String newSession = getRandomString(30);


        // Save prefs
        final String[] token = {""};

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) return;
                    token[0] = task.getResult();
                    // Update session
                    db.collection("users")
                            .document(mob)
                            .update("session", newSession,"fcmToken", token[0]);

                    Log.d("FCM token", token[0]);
                });

        SharedPreferences.Editor editor =
                getSharedPreferences(constant.prefs, MODE_PRIVATE).edit();

        editor.putString("mobile", mob);
        editor.putString("login", "true");
        editor.putString("name", document.getString("name"));
        editor.putString("email", document.getString("email"));
        editor.putString("session", newSession);
        editor.putString("active", document.getString("active"));
        editor.putString("homeline", document.getString("homeline"));
        editor.putString("fcmToken",token[0]);
        editor.apply();
        Toast.makeText(login.this, "Login Successful!", Toast.LENGTH_SHORT).show();

        Intent in = new Intent(getApplicationContext(), MainActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
        finish();
    }

    private void initView() {
        draw = findViewById(R.id.draw);
        mobile = findViewById(R.id.mobile);
        edtOtp = findViewById(R.id.otp);
        submit = findViewById(R.id.submit);
        verifyOtp = findViewById(R.id.verifyOtp);
        create = findViewById(R.id.create);

        // Hide OTP UI initially
        edtOtp.setVisibility(View.GONE);
        verifyOtp.setVisibility(View.GONE);
    }

    private static String getRandomString(final int size) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}