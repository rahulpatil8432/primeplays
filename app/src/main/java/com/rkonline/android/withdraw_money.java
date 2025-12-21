package com.rkonline.android;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class withdraw_money extends AppCompatActivity {

    EditText amountInput, upiInput,accNoInput,IFSCcodeInput;
    FirebaseFirestore db;

    String userMobile,wallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_money);

        amountInput = findViewById(R.id.amount);
        upiInput = findViewById(R.id.upi);
        accNoInput = findViewById(R.id.accNo);
        IFSCcodeInput = findViewById(R.id.IFSCcode);
        upiInput.setVisibility(View.GONE);
        accNoInput.setVisibility(View.GONE);
        IFSCcodeInput.setVisibility(View.GONE);
        userMobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("mobile", null);
        wallet =  getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("wallet", null);

        findViewById(R.id.submit).setEnabled(false);
        findViewById(R.id.submit).setAlpha(0.5f);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean checkButton = validateInputs();
                findViewById(R.id.submit).setAlpha(checkButton ? 1.0f : 0.5f);
                findViewById(R.id.submit).setEnabled(checkButton);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        amountInput.addTextChangedListener(watcher);
        upiInput.addTextChangedListener(watcher);
        accNoInput.addTextChangedListener(watcher);
        IFSCcodeInput.addTextChangedListener(watcher);
        db = FirebaseFirestore.getInstance();


        findViewById(R.id.back).setOnClickListener(v -> finish());

        findViewById(R.id.submit).setOnClickListener(v -> {
            if (validateInputs()) {
                submitWithdrawal();
            }
        });
    }


    // 🔵 SUBMIT WITHDRAWAL REQUEST
    private void submitWithdrawal() {

        String amount = amountInput.getText().toString().trim();
        String upi = upiInput.getText().toString().trim();
        String accNum = accNoInput.getText().toString().trim();
        String ifscCode = IFSCcodeInput.getText().toString().trim();

        Map<String, Object> withdrawData = new HashMap<>();
        withdrawData.put("mobile", userMobile);
        withdrawData.put("amount", Integer.parseInt(amount));
        withdrawData.put("upi", upi);
        withdrawData.put("accountNo", accNum);
        withdrawData.put("IFSCCode", ifscCode);
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

    private boolean validateInputs() {

        boolean isValid = true;

        String amountStr = amountInput.getText().toString().trim();

        // 🔹 AMOUNT VALIDATION
        if (TextUtils.isEmpty(amountStr)) {
            amountInput.setError("Enter amount");
            isValid = false;
        } else {
            int amount = Integer.parseInt(amountStr);

            if (amount <= 0) {
                amountInput.setError("Enter valid amount");
                isValid = false;
            } else if (amount > Integer.parseInt(wallet)) {
                amountInput.setError("Insufficient balance");
                isValid = false;
            } else {
                amountInput.setError(null);
            }

            // 🔹 Decide UPI or BANK
            if (amount > 50000) {

                upiInput.setVisibility(View.GONE);
                accNoInput.setVisibility(View.VISIBLE);
                IFSCcodeInput.setVisibility(View.VISIBLE);

                // Account Number
                if (TextUtils.isEmpty(accNoInput.getText().toString().trim())) {
                    accNoInput.setError("Enter account number");
                    isValid = false;
                } else {
                    accNoInput.setError(null);
                }

                // IFSC
                if (TextUtils.isEmpty(IFSCcodeInput.getText().toString().trim())) {
                    IFSCcodeInput.setError("Enter IFSC code");
                    isValid = false;
                } else {
                    IFSCcodeInput.setError(null);
                }

            } else {

                upiInput.setVisibility(View.VISIBLE);
                accNoInput.setVisibility(View.GONE);
                IFSCcodeInput.setVisibility(View.GONE);

                // UPI
                String upi = upiInput.getText().toString().trim();
//                if (TextUtils.isEmpty(upi)) {
//                    upiInput.setError("Enter UPI ID");
//                    isValid = false;
//                } else if (!upi.contains("@")) {
//                    upiInput.setError("Invalid UPI ID");
//                    isValid = false;
//                } else {
//                    upiInput.setError(null);
//                }
                if (TextUtils.isEmpty(upi)) {
                    upiInput.setError("Enter UPI ID");
                    isValid = false;

                } else if (!isValidUpi(upi)) {
                    upiInput.setError("Invalid UPI ID");
                    isValid = false;

                } else {
                    upiInput.setError(null);
                }
            }
        }

        return isValid;
    }
    private boolean isValidUpi(String upi) {
        String UPI_REGEX = "^[a-zA-Z0-9._-]{2,256}@[a-zA-Z]{2,64}$";
        return !TextUtils.isEmpty(upi) && upi.matches(UPI_REGEX);
    }



}
