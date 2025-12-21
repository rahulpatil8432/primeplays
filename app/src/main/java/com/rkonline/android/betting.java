package com.rkonline.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class betting extends AppCompatActivity {

    protected RecyclerView recyclerview;
    protected latobold submit;
    protected ScrollView scrollView;
    protected EditText totalamount;
    SharedPreferences prefs;

    ArrayList<String> list;
    ArrayList<String> number = new ArrayList<>();
    adapterbetting adapterbetting;

    String market, game;

    ViewDialog progressDialog;

    int total = 0;
    ArrayList<String> fillnumber = new ArrayList<>();
    ArrayList<String> fillamount = new ArrayList<>();

    String numb, amou;

    protected Spinner type;
    ArrayList<String> typeof = new ArrayList<>();
    String selectedGameType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_betting);
        initView();


        findViewById(R.id.back).setOnClickListener(v -> finish());

        game = getIntent().getStringExtra("game");
        market = getIntent().getStringExtra("market");
        number = getIntent().getStringArrayListExtra("list");

        if(getIntent().getBooleanExtra("isMarketOpen", false)){
            typeof.add("Close");
        }else{
            typeof.add("Open");
            typeof.add("Close");
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(betting.this, R.layout.simple_list_item_1, typeof);
        type.setAdapter(arrayAdapter);
        prefs = getSharedPreferences(constant.prefs, MODE_PRIVATE);
        adapterbetting = new adapterbetting(betting.this, number,
                updatedList -> {
                    list = adapterbetting.getNumber();
                    total = 0;

                    for (int a = 0; a < list.size(); a++) {
                        total = total + Integer.parseInt(list.get(a));
                    }

                    totalamount.setText(String.valueOf(total));
                });

        recyclerview.setLayoutManager(new GridLayoutManager(betting.this, 4));
        recyclerview.setAdapter(adapterbetting);

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){
                    selectedGameType = "Open";
                }else{
                    selectedGameType = "Close";
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        submit.setOnClickListener(v -> handleBetSubmit());
    }

    private void handleBetSubmit() {

        Log.e("list", list != null ? list.toString() : "null");

        if (total < constant.min_total || total > constant.max_total) {
            showAlert("You can only bet between 10 INR to 10000 INR");
            return;
        }

        // wallet check
        if (total > Integer.parseInt(prefs.getString("wallet", "0"))) {
            new AlertDialog.Builder(betting.this)
                    .setMessage("You don't have enough wallet balance. Recharge your wallet to play.")
                    .setPositiveButton("Recharge", (dialog, id) -> {
                        Intent intent = new Intent(betting.this, deposit_money.class);
                        startActivity(intent);
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                    .show();
            return;
        }


        // validate each bet
        fillnumber.clear();
        fillamount.clear();

        for (int a = 0; a < list.size(); a++) {

            if (!list.get(a).equals("0")) {

                int amt = Integer.parseInt(list.get(a));

                if (amt < constant.min_single || amt > constant.max_single) {
                    showAlert("You can only bet between 10 INR to 10000 INR");
                    return;
                }

                fillnumber.add(number.get(a));
                fillamount.add(list.get(a));
            }
        }

        numb = TextUtils.join(",", fillnumber);
        amou = TextUtils.join(",", fillamount);

        saveToFirestore();
    }

    private void saveToFirestore() {

        progressDialog = new ViewDialog(betting.this);
        progressDialog.showDialog();
        submit.setEnabled(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String mobile = prefs.getString("mobile", null);
        int wallet = Integer.parseInt(prefs.getString("wallet", "0"));
        int newWallet = wallet - total;

        long timestamp = System.currentTimeMillis();
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

        db.runBatch(batch -> {

            // 1️⃣ Save each bet
            for (int i = 0; i < fillnumber.size(); i++) {

                Map<String, Object> betData = new HashMap<>();
                betData.put("mobile", mobile);
                betData.put("market", market);
                betData.put("game", game);
                betData.put("bet", fillnumber.get(i));
                betData.put("amount", fillamount.get(i));
                betData.put("date", date);
                betData.put("time", time);
                betData.put("timestamp", timestamp);
                betData.put("gameType", selectedGameType);

                batch.set(
                        db.collection("played").document(),
                        betData
                );
            }

            // 2️⃣ Wallet transaction
            Map<String, Object> txn = new HashMap<>();
            txn.put("mobile", mobile);
            txn.put("amount",  String.valueOf(total));
            txn.put("type", "DEBIT");
            txn.put("remark", "Bet placed - " + market);
            txn.put("timestamp", timestamp);
            txn.put("date", date);

            batch.set(
                    db.collection("transactions").document(),
                    txn
            );

            // 3️⃣ Update user wallet
            batch.update(
                    db.collection("users").document(mobile),
                    "wallet", newWallet
            );

        }).addOnSuccessListener(unused -> {

            // Update local wallet
            prefs.edit().putString("wallet", String.valueOf(newWallet)).apply();

            progressDialog.hideDialog();
            Toast.makeText(betting.this, "Bet placed successfully 🎉", Toast.LENGTH_SHORT).show();
            goThankYou();

        }).addOnFailureListener(e -> {

            submit.setEnabled(true);
            progressDialog.hideDialog();
            Toast.makeText(betting.this, "Bet failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void goThankYou() {
        Intent in = new Intent(getApplicationContext(), thankyou.class);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
        finish();
    }

    private void showAlert(String msg) {
        new AlertDialog.Builder(betting.this)
                .setMessage(msg)
                .setCancelable(true)
                .setNegativeButton("Okay", (dialog, id) -> dialog.cancel())
                .show();
    }

    private void initView() {
        recyclerview = findViewById(R.id.recyclerview);
        submit = findViewById(R.id.submit);
        totalamount = findViewById(R.id.totalamount);
        type = findViewById(R.id.type);
    }
}