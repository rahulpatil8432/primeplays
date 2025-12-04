package com.rkonline.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class signup extends AppCompatActivity {

    EditText name, email, mobile, edtOtp;
    latobold submit, verifyOtp;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    String storedVerificationId;
    PhoneAuthProvider.ForceResendingToken resendToken;

    private static final String ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();

        submit.setOnClickListener(v -> {
            if (validate()) {
                checkUserExistsThenSendOtp();
            }
        });

        verifyOtp.setOnClickListener(v -> {
            String code = edtOtp.getText().toString().trim();
            if (code.isEmpty()) {
                edtOtp.setError("Enter OTP");
            } else {
                verifyCode(code);
            }
        });
    }

    private void checkUserExistsThenSendOtp() {
        String mob = mobile.getText().toString();

        db.collection("users")
                .document(mob)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Toast.makeText(signup.this, "Mobile already registered", Toast.LENGTH_SHORT).show();
                    } else {
                        sendVerificationCode(mob);
                    }
                });
    }

    private void sendVerificationCode(String phoneNumber) {

        phoneNumber = formatPhone(phoneNumber);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
        edtOtp.setVisibility(View.VISIBLE);
        verifyOtp.setVisibility(View.VISIBLE);
        Toast.makeText(this, "OTP sent", Toast.LENGTH_SHORT).show();
    }

    private String formatPhone(String mob) {
        if (!mob.startsWith("+")) {
            return "+91" + mob;
        }
        return mob;
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Auto verification or instant verification
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(signup.this,
                            "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {

                    storedVerificationId = verificationId;
                    resendToken = token;

                    // Show OTP field & verify button
                    edtOtp.setVisibility(View.VISIBLE);
                    verifyOtp.setVisibility(View.VISIBLE);

                    // Optional: hide submit button to prevent duplicate OTP sends
                    submit.setVisibility(View.GONE);

                    Toast.makeText(signup.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifyCode(String code) {
        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(storedVerificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        createFirestoreUser();
                    } else {
                        Toast.makeText(this,
                                "OTP Verification Failed: " +
                                        task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createFirestoreUser() {

        String mob = mobile.getText().toString();
        String nm = name.getText().toString();
        String mail = email.getText().toString();

        Map<String, Object> user = new HashMap<>();
        user.put("mobile", mob);
        user.put("name", nm);
        user.put("email", mail);
        user.put("wallet", 0);
        user.put("session", getRandomString(30));
        user.put("active", "1");

        db.collection("users")
                .document(mob)
                .set(user)
                .addOnSuccessListener(aVoid -> {

                    SharedPreferences.Editor editor =
                            getSharedPreferences(constant.prefs, MODE_PRIVATE).edit();

                    editor.putString("mobile", mob);
                    editor.putString("login", "true");
                    editor.putString("name", nm);
                    editor.putString("email", mail);
                    editor.putString("session", user.get("session").toString());
                    editor.apply();

                    Toast.makeText(signup.this, "Signup Successful!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(signup.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(signup.this,
                                "Signup Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private boolean validate() {
        if (name.getText().toString().isEmpty()) {
            name.setError("Enter name");
            return false;
        }
        if (email.getText().toString().isEmpty()) {
            email.setError("Enter email");
            return false;
        }
        if (mobile.getText().toString().isEmpty()) {
            mobile.setError("Enter mobile number");
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
        edtOtp.setVisibility(View.GONE);
        verifyOtp.setVisibility(View.GONE);
    }

    private static String getRandomString(final int size) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}
