package com.rkonline.android.utils;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpHelper {

    public interface OtpListener {
        void onOtpSent(String verificationId, PhoneAuthProvider.ForceResendingToken token);
        void onVerificationSuccess(PhoneAuthCredential credential);
        void onVerificationFailed(String error);
    }

    private final FirebaseAuth mAuth;
    private final Activity activity;
    private final OtpListener listener;

    private String storedVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    public OtpHelper(Activity activity, OtpListener listener) {
        this.activity = activity;
        this.listener = listener;
        this.mAuth = FirebaseAuth.getInstance();
    }

    // 🔹 Send OTP
    public void sendOtp(String phoneNumber) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // 🔹 Resend OTP
    public void resendOtp(String phoneNumber) {

        if (resendToken == null) {
            Toast.makeText(activity,
                    "Please wait before resending OTP",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(callbacks)
                        .setForceResendingToken(resendToken)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // 🔹 Verify OTP manually
    public void verifyOtp(String code) {

        if (storedVerificationId == null || storedVerificationId.isEmpty()) {
            listener.onVerificationFailed("OTP not ready yet");
            return;
        }

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(storedVerificationId, code);

        listener.onVerificationSuccess(credential);
    }

    // 🔹 Firebase callbacks
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    listener.onVerificationSuccess(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    listener.onVerificationFailed(e.getMessage());
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {

                    storedVerificationId = verificationId;
                    resendToken = token;
                    listener.onOtpSent(verificationId, token);
                }
            };
}
