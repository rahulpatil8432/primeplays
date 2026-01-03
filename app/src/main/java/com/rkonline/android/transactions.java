package com.rkonline.android;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.rkonline.android.adapter.adaptertransaction;
import com.rkonline.android.model.TransactionModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class transactions extends AppCompatActivity {

    RecyclerView recyclerview;
    ViewDialog progressDialog;
    ArrayList<TransactionModel> list = new ArrayList<>();

    FirebaseFirestore db;
    SwipeRefreshLayout swipeRefresh;
    TextView dateFilter, noData;

    String mobile, wallet;
    String filterDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        initView();

        db = FirebaseFirestore.getInstance();
        mobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("mobile", null);

        wallet = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("wallet", null);

        findViewById(R.id.back).setOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(this::loadTransactions);
        dateFilter.setOnClickListener(v -> showSingleDatePicker());
        setDefaultTodayDate();
    }

    private void initView() {
        recyclerview = findViewById(R.id.recyclerview);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        dateFilter = findViewById(R.id.dateFilter);
        noData = findViewById(R.id.noData);
    }

    private void setDefaultTodayDate() {
        Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_MONTH);
        int month = today.get(Calendar.MONTH) + 1;
        int year = today.get(Calendar.YEAR);

        filterDate = String.format("%04d-%02d-%02d", year, month, day);
        dateFilter.setText(String.format("Today: %02d/%02d/%04d", day, month, year));

        loadTransactions();
    }

    private void showSingleDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    int realMonth = month + 1;

                    filterDate = String.format("%04d-%02d-%02d", year, realMonth, day);
                    dateFilter.setText(String.format("%02d/%02d/%04d", day, realMonth, year));

                    loadTransactions();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void loadTransactions() {
        progressDialog = new ViewDialog(transactions.this);
        progressDialog.showDialog();

        Query query = db.collection("transactions")
                .whereEqualTo("mobile", mobile);

        if (filterDate != null) {
            query = query.whereEqualTo("date", filterDate); // filter by date
        }

        query = query.orderBy("timestamp", Query.Direction.DESCENDING);

        query.get()
                .addOnSuccessListener(snapshot -> {
                    swipeRefresh.setRefreshing(false);
                    progressDialog.hideDialog();

                    list.clear();


                for (DocumentSnapshot doc : snapshot) {

                    String amount = doc.getString("amount");
                    String remark = doc.getString("remark");
                    String type = doc.getString("type");
                    Long time = doc.getLong("timestamp");
                    String balance = doc.getString("balance");

                    if (amount == null) amount = "0";
                    if (remark == null) remark = "";
                    if (type == null) type = "CREDIT";
                    if (time == null) time = 0L;
                    if (balance == null) balance = "0";

                    TransactionModel model = new TransactionModel(
                            formatDate(time),
                            amount,
                            remark,
                            type,
                            time,balance
                    );

                    list.add(model);
                }


                    if (list.isEmpty()) {
                        noData.setVisibility(View.VISIBLE);
                        recyclerview.setVisibility(View.GONE);
                    } else {
                        noData.setVisibility(View.GONE);
                        recyclerview.setVisibility(View.VISIBLE);

                        adaptertransaction adapter =
                                new adaptertransaction(transactions.this, list);
                        recyclerview.setLayoutManager(new GridLayoutManager(this, 1));
                        recyclerview.setAdapter(adapter);
                    }

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
}
