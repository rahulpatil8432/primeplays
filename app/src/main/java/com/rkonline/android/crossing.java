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
import com.rkonline.android.utils.AlertHelper;
import com.rkonline.android.utils.BetEngine;

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

            if (!submit.isEnabled()) return;
            submit.setEnabled(false);

            if (number.getText().toString().isEmpty()) {
                number.setError("Enter numbers");
                submit.setEnabled(true);
                return;
            }

            if (amount.getText().toString().isEmpty() || amount.getText().toString().equals("0")) {
                amount.setError("Enter amount");
                submit.setEnabled(true);
                return;
            }

            String totalStr = totalamount.getText().toString();
            if (totalStr.isEmpty()) {
                showAlert("Enter amount");
                submit.setEnabled(true);
                return;
            }

            int totalVal;
            try {
                totalVal = Integer.parseInt(totalStr);
            } catch (NumberFormatException e) {
                showAlert("Invalid total amount");
                submit.setEnabled(true);
                return;
            }

            if (totalVal < 10 || totalVal > 10000) {
                showAlert("You can only bet between 10 INR to 10000 INR");
                submit.setEnabled(true);
                return;
            }

            prepareCrossingBets();
        });

    }

    private void prepareCrossingBets() {

        fillnumber.clear();
        fillamount.clear();

        for (String n : numbers) {
            fillnumber.add(n);
            fillamount.add(amount.getText().toString());
        }

        if (fillnumber.isEmpty()) {
            showAlert("No numbers to place bet");
            return;
        }

        placeCrossingWithEngine();
    }

    private void placeCrossingWithEngine() {

        progressDialog = new ViewDialog(crossing.this);
        progressDialog.showDialog();
        submit.setEnabled(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String mobile = prefs.getString("mobile", "");

        ArrayList<BetEngine.BetItem> bets = new ArrayList<>();

        for (int i = 0; i < fillnumber.size(); i++) {
            try {
                int amt = Integer.parseInt(fillamount.get(i));
                bets.add(new BetEngine.BetItem(fillnumber.get(i), amt));
            } catch (NumberFormatException e) {
                submit.setEnabled(true);
                progressDialog.hideDialog();
                AlertHelper.showCustomAlert(this, "Sorry!", "Invalid bet amount", 0, 0);
                return;
            }
        }

        BetEngine.placeMultipleBets(
                db,
                mobile,
                market,
                game,
                null, // crossing doesn't use Open/Close
                bets,
                new BetEngine.BetCallback() {
                    @Override
                    public void onSuccess(int newWallet) {
                        prefs.edit().putString("wallet", String.valueOf(newWallet)).apply();
                        progressDialog.hideDialog();
                        onAllCrossingComplete();
                    }

                    @Override
                    public void onFailure(String error) {
                        submit.setEnabled(true);
                        progressDialog.hideDialog();
                        AlertHelper.showCustomAlert(crossing.this, "Sorry!", error, 0, 0);
                    }
                }
        );
    }


    private void onAllCrossingComplete() {
        progressDialog.hideDialog();
        goThankYou();
    }


    private void showAlert(String msg) {
        new AlertDialog.Builder(crossing.this)
                .setMessage(msg)
                .setCancelable(true)
                .setNegativeButton("Okay", (dialog, id) -> dialog.dismiss())
                .show();
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