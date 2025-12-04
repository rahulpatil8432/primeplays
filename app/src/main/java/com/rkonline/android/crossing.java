package com.rkonline.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class crossing extends AppCompatActivity {

    protected EditText number;
    protected EditText amount;
    protected EditText totalamount;
    protected latobold submit;
    protected NestedScrollView scrollView;
    protected RecyclerView recyclerview;
    String value = "";
    SharedPreferences prefs;
    Boolean ischange = false;

    String market, game;
    ViewDialog progressDialog;

    ArrayList<String> fillnumber = new ArrayList<>();
    ArrayList<String> fillamount = new ArrayList<>();
    String numb, amou;

    ArrayList<String> numbers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crossing);
        initView();

        findViewById(R.id.back).setOnClickListener(v -> finish());

        prefs = getSharedPreferences(constant.prefs, MODE_PRIVATE);
        game = getIntent().getStringExtra("game");
        market = getIntent().getStringExtra("market");

        submit.setOnClickListener(v -> {

            if (number.getText().toString().isEmpty()) {
                number.setError("Enter numbers");
                return;
            }
            if (amount.getText().toString().isEmpty() || amount.getText().toString().equals("0")) {
                amount.setError("Enter amount");
                return;
            }

            String totalStr = totalamount.getText().toString();
            if (totalStr.isEmpty()) {
                showAlert("Enter amount");
                return;
            }

            int totalVal;
            try {
                totalVal = Integer.parseInt(totalStr);
            } catch (NumberFormatException e) {
                showAlert("Invalid total amount");
                return;
            }

            if (totalVal < 10 || totalVal > 10000) {
                showAlert("You can only bet between 10 INR to 10000 INR");
                return;
            }

            int wallet = Integer.parseInt(prefs.getString("wallet", "0"));

            if (totalVal <= wallet) {
                // prepare fill lists
                fillnumber.clear();
                fillamount.clear();
                for (int a = 0; a < numbers.size(); a++) {
                    fillnumber.add(numbers.get(a));
                    fillamount.add(amount.getText().toString());
                }

                numb = TextUtils.join(",", fillnumber);
                amou = TextUtils.join(",", fillamount);

                saveCrossingToFirestore();
            } else {
                new AlertDialog.Builder(crossing.this)
                        .setMessage("You don't have enough wallet balance to place this bet, Recharge your wallet to play")
                        .setPositiveButton("Recharge", (dialog, id) -> {
                            Intent intent = new Intent(crossing.this, deposit_money.class);
                            startActivity(intent);
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                        .show();
            }
        });
    }

    private void showAlert(String msg) {
        new AlertDialog.Builder(crossing.this)
                .setMessage(msg)
                .setCancelable(true)
                .setNegativeButton("Okay", (dialog, id) -> dialog.dismiss())
                .show();
    }

    private void saveCrossingToFirestore() {

        progressDialog = new ViewDialog(crossing.this);
        progressDialog.showDialog();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String mobile = prefs.getString("mobile", null);
        String bazarName = market;
        String gameType = "jodi"; // crossing uses jodi game as earlier code indicated

        long timestamp = System.currentTimeMillis();
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

        if (fillnumber.isEmpty()) {
            progressDialog.hideDialog();
            Toast.makeText(crossing.this, "No numbers to place bet", Toast.LENGTH_SHORT).show();
            return;
        }

        // track failures to show a single error if needed
        final int totalBets = fillnumber.size();
        final int[] successCount = {0};
        final int[] failureCount = {0};

        for (int i = 0; i < fillnumber.size(); i++) {
            final int idx = i;
            String betNum = fillnumber.get(idx);
            String betAmount = fillamount.get(idx);

            Map<String, Object> betData = new HashMap<>();
            betData.put("mobile", mobile);
            betData.put("bazar", bazarName);
            betData.put("game", gameType);
            betData.put("bet", betNum);
            betData.put("amount", betAmount);
            betData.put("date", date);
            betData.put("time", time);
            betData.put("timestamp", timestamp);

            db.collection("played")
                    .add(betData)
                    .addOnSuccessListener(docRef -> {
                        successCount[0]++;

                        // when last one succeeds (or all finished), go to thankyou
                        if (successCount[0] + failureCount[0] == totalBets) {
                            progressDialog.hideDialog();
                            if (failureCount[0] == 0) {
                                goThankYou();
                            } else {
                                Toast.makeText(crossing.this, "Some bets failed. Please check your played history.", Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        failureCount[0]++;
                        if (successCount[0] + failureCount[0] == totalBets) {
                            progressDialog.hideDialog();
                            Toast.makeText(crossing.this, "Failed placing some bets: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void goThankYou() {
        Intent in = new Intent(getApplicationContext(), thankyou.class);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
        finish();
    }

    public void characterCount(String inputString) {
        StringBuilder data = new StringBuilder();
        HashMap<Character, Integer> charCountMap = new HashMap<>();
        char[] strArray = inputString.toCharArray();
        for (char c : strArray) {
            if (charCountMap.containsKey(c)) {
                charCountMap.put(c, charCountMap.get(c) + 1);
            } else {
                charCountMap.put(c, 1);
            }
        }

        numbers.clear();

        for (Map.Entry entry : charCountMap.entrySet()) {
            data.append(entry.getKey().toString());
        }

        value = data.toString();

        for (int a = 0; a < value.length(); a++) {
            Log.e("fr", value.charAt(a) + "");
            for (int i = 0; i < value.length(); i++) {
                String nd = value.charAt(a) + "" + value.charAt(i) + "";
                numbers.add(nd);
            }
        }

        adapter_crossing adapterbetting = new adapter_crossing(crossing.this, numbers);
        recyclerview.setLayoutManager(new GridLayoutManager(crossing.this, 4));
        recyclerview.setAdapter(adapterbetting);
        adapterbetting.notifyDataSetChanged();

        number.setText(value);

        if (!amount.getText().toString().isEmpty()) {
            totalamount.setText("" + (Integer.parseInt(amount.getText().toString().toString()) * (value.length() * value.length())));
        }
    }

    private void initView() {
        number = findViewById(R.id.number);
        amount = findViewById(R.id.amount);
        totalamount = findViewById(R.id.totalamount);
        submit = findViewById(R.id.submit);
        scrollView = findViewById(R.id.scrollView);
        recyclerview = findViewById(R.id.recyclerview);

        number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // no-op
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0 && !value.equals(s.toString())) {
                    ischange = true;
                    characterCount(s.toString());
                } else if (s.toString().equals("")) {
                    numbers.clear();
                    adapter_crossing adapterbetting = new adapter_crossing(crossing.this, numbers);
                    recyclerview.setLayoutManager(new GridLayoutManager(crossing.this, 4));
                    recyclerview.setAdapter(adapterbetting);
                    adapterbetting.notifyDataSetChanged();
                }
            }
        });

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // no-op
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0 && value != null && value.length() > 0) {
                    totalamount.setText("" + (Integer.parseInt(s.toString()) * (value.length() * value.length())));
                } else {
                    totalamount.setText("");
                }
            }
        });
    }
}