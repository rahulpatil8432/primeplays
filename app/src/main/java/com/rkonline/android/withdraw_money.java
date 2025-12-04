package com.rkonline.android;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class withdraw_money extends AppCompatActivity {

    EditText amountInput, upiInput;
    FirebaseFirestore db;

    String userMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_money);

        amountInput = findViewById(R.id.amount);
        upiInput = findViewById(R.id.upi);

        db = FirebaseFirestore.getInstance();

        userMobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("mobile", null);

        findViewById(R.id.back).setOnClickListener(v -> finish());

        findViewById(R.id.submit).setOnClickListener(v -> submitWithdrawal());
    }


    // 🔵 SUBMIT WITHDRAWAL REQUEST
    private void submitWithdrawal() {

        String amount = amountInput.getText().toString().trim();
        String upi = upiInput.getText().toString().trim();

        if (TextUtils.isEmpty(amount) || amount.equals("0")) {
            amountInput.setError("Enter valid amount");
            return;
        }

        if (TextUtils.isEmpty(upi) || !upi.contains("@")) {
            upiInput.setError("Enter valid UPI ID");
            return;
        }

        Map<String, Object> withdrawData = new HashMap<>();
        withdrawData.put("mobile", userMobile);
        withdrawData.put("amount", Integer.parseInt(amount));
        withdrawData.put("upi", upi);
        withdrawData.put("status", "pending");       // admin will approve
        withdrawData.put("timestamp", System.currentTimeMillis());

        db.collection("withdraw_requests")
                .add(withdrawData)
                .addOnSuccessListener(doc -> {

                    Toast.makeText(this,
                            "Withdrawal Request Submitted!\nAdmin will process shortly.",
                            Toast.LENGTH_LONG).show();

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed! Try again.",
                                Toast.LENGTH_SHORT).show()
                );
    }
}
