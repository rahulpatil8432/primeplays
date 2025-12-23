package com.rkonline.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rkonline.android.utils.OtpHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class signup extends AppCompatActivity {

    private EditText name, email, mobile, edtOtp;
    private latobold submit, verifyOtp, resendOtp;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private OtpHelper otpHelper;

    private static final String ALLOWED_CHARACTERS =
            "0123456789abcdefghijklmnopqrstuvwxyz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        initOtpHelper();

        submit.setOnClickListener(v -> {
            if (validate()) {
                checkUserExistsThenSendOtp();
            }
        });

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

    // 🔹 Init OTP helper
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
                Toast.makeText(signup.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // 🔹 Check if user already exists
    private void checkUserExistsThenSendOtp() {

        String mob = mobile.getText().toString().trim();

        db.collection("users")
                .document(mob)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Toast.makeText(this,
                                "Mobile already registered",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    otpHelper.sendOtp(formatPhone(mob));
                });
    }

    private String formatPhone(String mob) {
        return mob.startsWith("+") ? mob : "+91" + mob;
    }

    // 🔹 Firebase auth success
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        createFirestoreUser();
                    } else {
                        Toast.makeText(this,
                                "OTP Verification Failed",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // 🔹 Create Firestore user
    private void createFirestoreUser() {

        String mob = mobile.getText().toString().trim();
        String nm = name.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String session = getRandomString(30);

        Map<String, Object> user = new HashMap<>();
        user.put("mobile", mob);
        user.put("name", nm);
        user.put("email", mail);
        user.put("wallet", 0);
        user.put("session", session);
        user.put("active", "1");

        db.collection("users")
                .document(mob)
                .set(user)
                .addOnSuccessListener(aVoid -> {

                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task -> {

                                String fcmToken = "";

                                if (task.isSuccessful()) {
                                    fcmToken = task.getResult();
                                    db.collection("users")
                                            .document(mob)
                                            .update("fcmToken", fcmToken);
                                }

                                SharedPreferences.Editor editor =
                                        getSharedPreferences(constant.prefs,
                                                MODE_PRIVATE).edit();

                                editor.putString("mobile", mob);
                                editor.putString("login", "true");
                                editor.putString("name", nm);
                                editor.putString("email", mail);
                                editor.putString("session", session);
                                editor.putString("fcmToken", fcmToken);
                                editor.apply();

                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            });
                });
    }

    private boolean validate() {

        if (name.getText().toString().trim().isEmpty()) {
            name.setError("Enter name");
            return false;
        }

        if (mobile.getText().toString().trim().isEmpty()) {
            mobile.setError("Enter mobile");
            return false;
        }

        return true;
    }

    private void initViews() {

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        mobile = findViewById(R.id.mobile);
        edtOtp = findViewById(R.id.otp);
        submit = findViewById(R.id.submit);
        verifyOtp = findViewById(R.id.verifyOtp);
        resendOtp = findViewById(R.id.resendOtp);

        edtOtp.setVisibility(View.GONE);
        verifyOtp.setVisibility(View.GONE);
        resendOtp.setVisibility(View.GONE);
    }

    private static String getRandomString(int size) {

        Random random = new Random();
        StringBuilder sb = new StringBuilder(size);

        for (int i = 0; i < size; i++) {
            sb.append(ALLOWED_CHARACTERS.charAt(
                    random.nextInt(ALLOWED_CHARACTERS.length())));
        }
        return sb.toString();
    }
}