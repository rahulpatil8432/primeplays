package com.rkonline.android;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.rkonline.android.model.TransactionModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class transactions extends AppCompatActivity {

    RecyclerView recyclerview;
    ViewDialog progressDialog;
    ArrayList<TransactionModel> list = new ArrayList<>();

    FirebaseFirestore db;

    ArrayList<String> date = new ArrayList<>();
    ArrayList<String> amount = new ArrayList<>();
    ArrayList<String> remark = new ArrayList<>();

    String mobile;
    String wallet;
    SwipeRefreshLayout swipeRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        initView();

        db = FirebaseFirestore.getInstance();

        mobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("mobile", null);

        wallet = getSharedPreferences(constant.prefs,MODE_PRIVATE).getString("wallet","0");
        findViewById(R.id.back).setOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(() -> {
            loadTransactions();
        });
        loadTransactions();
    }


//    private void loadTransactions() {
//
//        progressDialog = new ViewDialog(transactions.this);
//        progressDialog.showDialog();
//
//        db.collection("transactions")
//                .whereEqualTo("mobile", mobile)
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .get()
//                .addOnSuccessListener(snapshot -> {
//
//                    progressDialog.hideDialog();
//
//                    date.clear();
//                    remark.clear();
//                    amount.clear();
//
//                    for (DocumentSnapshot doc : snapshot) {
//
//                        String a = doc.getString("amount");
//                        String r = doc.getString("remark");
//
//                        Long time = doc.getLong("timestamp");
//                        String formattedDate = formatDate(time);
//
//                        if (a == null) a = "0";
//                        if (r == null) r = "";
//                        if (formattedDate == null) formattedDate = "";
//
//                        date.add(formattedDate);
//                        amount.add(a);
//                        remark.add(r);
//                    }
//
//                    adaptertransaction rc =
//                            new adaptertransaction(transactions.this, date, remark, amount);
//
//                    recyclerview.setLayoutManager(new GridLayoutManager(transactions.this, 1));
//                    recyclerview.setAdapter(rc);
//                })
//                .addOnFailureListener(e -> {
//
//                    progressDialog.hideDialog();
//                    Toast.makeText(this,
//                            "Failed to load: " + e.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                });
//    }


//    private void loadTransactions() {
//
//        progressDialog = new ViewDialog(transactions.this);
//        progressDialog.showDialog();
//
//        db.collection("transactions")
//                .whereEqualTo("mobile", mobile)
//                .orderBy("timestamp", Query.Direction.DESCENDING) // oldest first
//                .get()
//                .addOnSuccessListener(snapshot -> {
//
//                    progressDialog.hideDialog();
//                    list.clear();
//
//                    double runningBalance = 0;
//                    ArrayList<TransactionModel> asc = new ArrayList<>();
//
//                    for (DocumentSnapshot doc : snapshot) {
//
//                        String amount = doc.getString("amount");
//                        String remark = doc.getString("remark");
//                        String type = doc.getString("type");
//                        Long time = doc.getLong("timestamp");
//
//                        if (amount == null) amount = "0";
//                        if (remark == null) remark = "";
//                        if (type == null) type = "CREDIT";
//                        if (time == null) time = 0L;
//
//                        double amt = Double.parseDouble(amount);
//
//                        if (type.equalsIgnoreCase("CREDIT")) {
//                            runningBalance += amt;
//                        } else {
//                            runningBalance -= amt;
//                        }
//
//                        TransactionModel m = new TransactionModel(
//                                formatDate(time),
//                                amount,
//                                remark,
//                                type,
//                                time
//                        );
//                        m.balanceAfter = runningBalance;
//                        list.add(m);
//                    }
//
//                    // reverse to show latest first
////                    java.util.Collections.reverse(list);
//
//                    adaptertransaction adapter =
//                            new adaptertransaction(transactions.this, list);
//
//                    recyclerview.setLayoutManager(new GridLayoutManager(this, 1));
//                    recyclerview.setAdapter(adapter);
//                })
//                .addOnFailureListener(e -> {
//                    Log.d("transactions", e.getMessage());
//                    progressDialog.hideDialog();
//                    Toast.makeText(this,
//                            "Failed to load: " + e.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                });
//    }

private void loadTransactions() {

    progressDialog = new ViewDialog(transactions.this);
    progressDialog.showDialog();

    db.collection("transactions")
            .whereEqualTo("mobile", mobile)
            .orderBy("timestamp", Query.Direction.DESCENDING) // ANY order is OK now
            .get()
            .addOnSuccessListener(snapshot -> {
                swipeRefresh.setRefreshing(false);
                progressDialog.hideDialog();

                list.clear();

                ArrayList<TransactionModel> tempAscList = new ArrayList<>();

                // 1️⃣ Build TEMP list (raw data)
                for (DocumentSnapshot doc : snapshot) {

                    String amount = doc.getString("amount");
                    String remark = doc.getString("remark");
                    String type = doc.getString("type");
                    Long time = doc.getLong("timestamp");

                    if (amount == null) amount = "0";
                    if (remark == null) remark = "";
                    if (type == null) type = "CREDIT";
                    if (time == null) time = 0L;

                    TransactionModel model = new TransactionModel(
                            formatDate(time),
                            amount,
                            remark,
                            type,
                            time
                    );

                    tempAscList.add(model);
                }

                // 2️⃣ SORT TEMP LIST ASCENDING (oldest → newest)
                tempAscList.sort((a, b) ->
                        Long.compare(a.timestamp, b.timestamp)
                );

                // 3️⃣ CALCULATE RUNNING BALANCE
                double runningBalance = 0;

                for (TransactionModel m : tempAscList) {

                    double amt = Double.parseDouble(m.amount);

                    if (m.type.equalsIgnoreCase("CREDIT")) {
                        runningBalance += amt;
                    } else {
                        runningBalance -= amt;
                    }

                    m.balanceAfter = runningBalance;
                }

                // 4️⃣ REVERSE FOR UI (latest first)
                java.util.Collections.reverse(tempAscList);

                // 5️⃣ ASSIGN TO MAIN LIST
                list.addAll(tempAscList);

                adaptertransaction adapter =
                        new adaptertransaction(transactions.this, list);

                recyclerview.setLayoutManager(new GridLayoutManager(this, 1));
                recyclerview.setAdapter(adapter);
            })
            .addOnFailureListener(e -> {
                swipeRefresh.setRefreshing(false);
                progressDialog.hideDialog();
                Toast.makeText(this,
                        "Failed to load: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
}

    private String formatDate(Long timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }


    private void initView() {
        recyclerview = findViewById(R.id.recyclerview);
        swipeRefresh = findViewById(R.id.swipeRefresh);
    }
}
