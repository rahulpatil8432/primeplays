package com.rkonline.android;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ScrollView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_betting);
        initView();

        prefs = getSharedPreferences(constant.prefs, MODE_PRIVATE);

        findViewById(R.id.back).setOnClickListener(v -> finish());

        game = getIntent().getStringExtra("game");
        market = getIntent().getStringExtra("market");
        number = getIntent().getStringArrayListExtra("list");

        adapterbetting = new adapterbetting(betting.this, number,
                new adapterbetting.AmountChangeListener() {
                    @Override
                    public void onAmountChanged(ArrayList<String> updatedList) {
                        list = adapterbetting.getNumber();
                        total = 0;

                        for (int a = 0; a < list.size(); a++) {
                            total = total + Integer.parseInt(list.get(a));
                        }

                        totalamount.setText(String.valueOf(total));
                    }
                });

//        BroadcastReceiver mReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//
//
//            }
//        };

//        IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
//        registerReceiver(mReceiver, intentFilter);

        recyclerview.setLayoutManager(new GridLayoutManager(betting.this, 4));
        recyclerview.setAdapter(adapterbetting);

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

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String mobile = prefs.getString("mobile", null);

        long timestamp = System.currentTimeMillis();
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

        for (int i = 0; i < fillnumber.size(); i++) {

            String betNum = fillnumber.get(i);
            String betAmount = fillamount.get(i);

            Map<String, Object> betData = new HashMap<>();
            betData.put("mobile", mobile);
            betData.put("bazar", market);
            betData.put("game", game);
            betData.put("bet", betNum);
            betData.put("amount", betAmount);
            betData.put("date", date);
            betData.put("time", time);
            betData.put("timestamp", timestamp);

            int finalI = i;
            db.collection("played")
                    .add(betData)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(betting.this, "Success: " +"Bet Placed", Toast.LENGTH_SHORT).show();
                        if (finalI == fillnumber.size() - 1) {
                            progressDialog.hideDialog();
                            goThankYou();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.hideDialog();
                        Toast.makeText(betting.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
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
    }
}