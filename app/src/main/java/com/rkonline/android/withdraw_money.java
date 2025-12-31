package com.rkonline.android;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rkonline.android.adapter.WithdrawAdapter;
import com.rkonline.android.model.WithdrawRequest;
import com.rkonline.android.utils.AlertHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class withdraw_money extends AppCompatActivity {

    EditText amountInput, upiInput, accNoInput, IFSCcodeInput;
    FirebaseFirestore db;
    Button submitBtn;
    ProgressBar loader;
    SwipeRefreshLayout swipeRefresh;
    String userMobile, wallet, terms;

    RecyclerView recyclerView;
    LinearLayout previousContainer;
    TextWatcher watcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_money);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        amountInput = findViewById(R.id.amount);
        upiInput = findViewById(R.id.upi);
        accNoInput = findViewById(R.id.accNo);
        IFSCcodeInput = findViewById(R.id.IFSCcode);
        submitBtn = findViewById(R.id.submit);
        loader = findViewById(R.id.loader);
        recyclerView = findViewById(R.id.previous_requests_list);
        previousContainer = findViewById(R.id.previous_requests_container);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        upiInput.setVisibility(View.GONE);
        accNoInput.setVisibility(View.GONE);
        IFSCcodeInput.setVisibility(View.GONE);
        submitBtn.setEnabled(false);
        submitBtn.setAlpha(0.5f);

        userMobile = getSharedPreferences(constant.prefs, MODE_PRIVATE).getString("mobile", null);
        wallet = getSharedPreferences(constant.prefs, MODE_PRIVATE).getString("wallet", null);

        upiInput.setText(getSharedPreferences(constant.prefs,MODE_PRIVATE).getString("upi",""));
        accNoInput.setText(getSharedPreferences(constant.prefs,MODE_PRIVATE).getString("accountNo",""));
        IFSCcodeInput.setText(getSharedPreferences(constant.prefs,MODE_PRIVATE).getString("IFSCCode",""));

        db = FirebaseFirestore.getInstance();
        terms = getSharedPreferences(constant.prefs,MODE_PRIVATE).getString("withdrawTerms", "");
        AlertHelper.showCustomAlert(withdraw_money.this,"Terms & Conditions",terms,R.drawable.info_icon,0);

         watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean checkButton = validateInputs();
                submitBtn.setAlpha(checkButton ? 1.0f : 0.5f);
                submitBtn.setEnabled(checkButton);
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        amountInput.addTextChangedListener(watcher);
        upiInput.addTextChangedListener(watcher);
        accNoInput.addTextChangedListener(watcher);
        IFSCcodeInput.addTextChangedListener(watcher);

        findViewById(R.id.back).setOnClickListener(v -> finish());

        submitBtn.setOnClickListener(v -> {
            if (validateInputs()) submitWithdrawal();
        });
        swipeRefresh.setOnRefreshListener(() -> {
            fetchPreviousRequests();
        });
        fetchPreviousRequests();
    }

    private void fetchPreviousRequests() {
        loader.setVisibility(View.VISIBLE);
        db.collection("withdraw_requests")
                .whereEqualTo("mobile", userMobile)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loader.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<WithdrawRequest> requestList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            requestList.add(doc.toObject(WithdrawRequest.class));
                        }
                        Log.d("RequestList",requestList.size() + "");
                        WithdrawAdapter adapter = new WithdrawAdapter(requestList);
                        recyclerView.setAdapter(adapter);
                        swipeRefresh.setVisibility(View.VISIBLE);
                    } else {
                        swipeRefresh.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    loader.setVisibility(View.GONE);
                    Log.d("RequestList",e.getMessage());
                    swipeRefresh.setVisibility(View.GONE);
                });
    }

    private void submitWithdrawal() {
        submitBtn.setEnabled(false);
        submitBtn.setAlpha(0.5f);
        loader.setVisibility(View.VISIBLE);

        String amount = amountInput.getText().toString().trim();
        String upi = upiInput.getText().toString().trim();
        String accNum = accNoInput.getText().toString().trim();
        String ifscCode = IFSCcodeInput.getText().toString().trim();

        Map<String, Object> withdrawData = new HashMap<>();
        withdrawData.put("mobile", userMobile);
        withdrawData.put("amount", Integer.parseInt(amount));

        if(Integer.parseInt(amount)>50000){
            withdrawData.put("accountNo", accNum);
            withdrawData.put("IFSCCode", ifscCode);
        }
        else{
            withdrawData.put("upi", upi);
        }
        withdrawData.put("status", "pending");
        withdrawData.put("timestamp", System.currentTimeMillis());

        db.collection("withdraw_requests")
                .add(withdrawData)
                .addOnSuccessListener(doc -> db.collection("users")
                        .document(userMobile)
                        .update("upi", upi, "accountNo", accNum, "IFSCCode", ifscCode)
                        .addOnSuccessListener(aVoid -> {
                            AlertHelper.showCustomAlert(withdraw_money.this, "Success!" , "Withdrawal Request Submitted!\nAdmin will process shortly.", R.drawable.check,R.color.md_green_900);

                            fetchPreviousRequests(); // Refresh list
                            amountInput.removeTextChangedListener(watcher);
                            upiInput.removeTextChangedListener(watcher);
                            accNoInput.removeTextChangedListener(watcher);
                            IFSCcodeInput.removeTextChangedListener(watcher);
                            amountInput.setText("");
                            amountInput.clearFocus();
                            upiInput.setVisibility(View.GONE);
                            accNoInput.setVisibility(View.GONE);
                            IFSCcodeInput.setVisibility(View.GONE);
                            amountInput.addTextChangedListener(watcher);
                            upiInput.addTextChangedListener(watcher);
                            accNoInput.addTextChangedListener(watcher);
                            IFSCcodeInput.addTextChangedListener(watcher);
                            submitBtn.setEnabled(false);
                            submitBtn.setAlpha(0.5f);
                        })
                        .addOnFailureListener(e -> {
                            AlertHelper.showCustomAlert(this, "Sorry!" , "Something went wrong", 0,0);

                            submitBtn.setEnabled(true);
                            submitBtn.setAlpha(1f);
                            loader.setVisibility(View.GONE);
                        }))
                .addOnFailureListener(e -> {
                    AlertHelper.showCustomAlert(this, "Sorry!" , "Something went wrong", 0,0);
                    submitBtn.setEnabled(true);
                    submitBtn.setAlpha(1f);
                    loader.setVisibility(View.GONE);
                });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String amountStr = amountInput.getText().toString().trim();
        int minAmount = getSharedPreferences(constant.prefs,MODE_PRIVATE).getInt("minAmount",1000);
        if (TextUtils.isEmpty(amountStr)) {
            amountInput.setError("Enter amount");
            isValid = false;
        } else {
            int amount = Integer.parseInt(amountStr);

            if (amount <= 0) {
                amountInput.setError("Enter valid amount");
                isValid = false;
            } else if (amount > Double.parseDouble(wallet)) {
                amountInput.setError("Insufficient balance");
                isValid = false;
            } else if (amount < minAmount) {
                amountInput.setError("Minimum Withdraw Amount must be "+minAmount);
                isValid = false;
            } else {
                amountInput.setError(null);
            }

            if (amount > 50000) {
                upiInput.setVisibility(View.GONE);
                accNoInput.setVisibility(View.VISIBLE);
                IFSCcodeInput.setVisibility(View.VISIBLE);

                if (TextUtils.isEmpty(accNoInput.getText().toString().trim())) {
                    accNoInput.setError("Enter account number");
                    isValid = false;
                } else accNoInput.setError(null);

                if (TextUtils.isEmpty(IFSCcodeInput.getText().toString().trim())) {
                    IFSCcodeInput.setError("Enter IFSC code");
                    isValid = false;
                } else IFSCcodeInput.setError(null);

            } else {
                upiInput.setVisibility(View.VISIBLE);
                accNoInput.setVisibility(View.GONE);
                IFSCcodeInput.setVisibility(View.GONE);

                String upi = upiInput.getText().toString().trim();
                if (TextUtils.isEmpty(upi)) {
                    upiInput.setError("Enter UPI ID");
                    isValid = false;
                } else if (!isValidUpi(upi)) {
                    upiInput.setError("Invalid UPI ID");
                    isValid = false;
                } else upiInput.setError(null);
            }
        }
        return isValid;
    }

    private boolean isValidUpi(String upi) {
        String UPI_REGEX = "^[a-zA-Z0-9._-]{2,256}@[a-zA-Z]{2,64}$";
        return !TextUtils.isEmpty(upi) && upi.matches(UPI_REGEX);
    }

}
