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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rkonline.android.utils.AlertHelper;
import com.rkonline.android.utils.OtpHelper;

import java.util.Random;

public class login extends AppCompatActivity {

    private ImageView draw;
    private EditText mobile, edtOtp;
    private latobold submit, verifyOtp, resendOtp;
    private TextView create;
    private boolean isSigningIn = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private OtpHelper otpHelper;

    private static final String ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initView();
        initOtpHelper();

        create.setOnClickListener(v ->
                startActivity(new Intent(this, signup.class)));

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
                return;
            }
            otpHelper.verifyOtp(code);
        });

        // RESEND OTP
        resendOtp.setOnClickListener(v -> {
            String mob = mobile.getText().toString().trim();
            otpHelper.resendOtp(formatPhone(mob));
        });
    }

    // 🔹 OTP helper initialization
    private void initOtpHelper() {

        otpHelper = new OtpHelper(this, new OtpHelper.OtpListener() {

            @Override
            public void onOtpSent(String verificationId,
                                  com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken token) {

                edtOtp.setVisibility(View.VISIBLE);
                verifyOtp.setVisibility(View.VISIBLE);
                resendOtp.setVisibility(View.VISIBLE);
                submit.setVisibility(View.GONE);
            }

            @Override
            public void onVerificationSuccess(PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(String error) {
                AlertHelper.showCustomAlert(login.this, "Sorry!" , "Something went wrong\n Please try again", 0,0);

            }
        });
    }

    // 🔹 Check user exists before OTP
    private void checkUserExistsThenSendOtp(String mob) {

        db.collection("users")
                .document(mob)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        AlertHelper.showCustomAlert(this, "Sorry!" , "User not registered", 0,0);
                        return;
                    }
                    otpHelper.sendOtp(formatPhone(mob));
                });
    }

    private String formatPhone(String mob) {
        return mob.startsWith("+") ? mob : "+91" + mob;
    }

    // 🔹 Firebase sign-in
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        if (isSigningIn) return;
        isSigningIn = true;

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fetchUserDataAndLogin();
                    } else {
                        isSigningIn = false;
                        AlertHelper.showCustomAlert(this, "Sorry!" , "Invalid OTP", 0,0);
                    }
                });
    }

    // 🔹 Fetch user & save session
    private void fetchUserDataAndLogin() {

        String mob = mobile.getText().toString().trim();

        db.collection("users")
                .document(mob)
                .get()
                .addOnSuccessListener(this::handleUserLogin);
    }

    private void handleUserLogin(DocumentSnapshot document) {

        if (!document.exists()) {
            AlertHelper.showCustomAlert(this, "Sorry!" , "User not found", 0,0);
            return;
        }

        String mob = mobile.getText().toString().trim();
        String session = getRandomString(30);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {

                    String fcmToken = "";

                    if (task.isSuccessful()) {
                        fcmToken = task.getResult();
                        db.collection("users")
                                .document(mob)
                                .update("session", session, "fcmToken", fcmToken);

                        Log.d("FCM token", fcmToken);
                    }

                    SharedPreferences.Editor editor =
                            getSharedPreferences(constant.prefs, MODE_PRIVATE).edit();

                    editor.putString("mobile", mob);
                    editor.putString("login", "true");
                    editor.putString("name", document.getString("name"));
                    editor.putString("email", document.getString("email"));
                    editor.putString("session", session);
                    editor.putString("active", document.getString("active"));
                    editor.putString("homeline", document.getString("homeline"));
                    editor.putString("fcmToken", fcmToken);
                    editor.apply();

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    private void initView() {
        draw = findViewById(R.id.draw);
        mobile = findViewById(R.id.mobile);
        edtOtp = findViewById(R.id.otp);
        submit = findViewById(R.id.submit);
        verifyOtp = findViewById(R.id.verifyOtp);
        resendOtp = findViewById(R.id.resendOtp);
        create = findViewById(R.id.create);

        edtOtp.setVisibility(View.GONE);
        verifyOtp.setVisibility(View.GONE);
        resendOtp.setVisibility(View.GONE);
    }

    private static String getRandomString(int size) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++)
            sb.append(ALLOWED_CHARACTERS.charAt(
                    random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}