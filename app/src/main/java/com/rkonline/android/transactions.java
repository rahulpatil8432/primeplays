package com.rkonline.android;

import android.os.Bundle;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class transactions extends AppCompatActivity {

    RecyclerView recyclerview;
    ViewDialog progressDialog;

    FirebaseFirestore db;

    ArrayList<String> date = new ArrayList<>();
    ArrayList<String> amount = new ArrayList<>();
    ArrayList<String> remark = new ArrayList<>();

    String mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        initView();

        db = FirebaseFirestore.getInstance();

        mobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("mobile", null);

        findViewById(R.id.back).setOnClickListener(v -> finish());

        loadTransactions();
    }


    private void loadTransactions() {

        progressDialog = new ViewDialog(transactions.this);
        progressDialog.showDialog();

        db.collection("transactions")
                .whereEqualTo("mobile", mobile)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {

                    progressDialog.hideDialog();

                    date.clear();
                    remark.clear();
                    amount.clear();

                    for (DocumentSnapshot doc : snapshot) {

                        String a = doc.getString("amount");
                        String r = doc.getString("remark");

                        Long time = doc.getLong("timestamp");
                        String formattedDate = formatDate(time);

                        if (a == null) a = "0";
                        if (r == null) r = "";
                        if (formattedDate == null) formattedDate = "";

                        date.add(formattedDate);
                        amount.add(a);
                        remark.add(r);
                    }

                    adaptertransaction rc =
                            new adaptertransaction(transactions.this, date, remark, amount);

                    recyclerview.setLayoutManager(new GridLayoutManager(transactions.this, 1));
                    recyclerview.setAdapter(rc);
                })
                .addOnFailureListener(e -> {

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
    }
}
