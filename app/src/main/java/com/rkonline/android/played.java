package com.rkonline.android;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;

public class played extends AppCompatActivity {

    RecyclerView recyclerview;
    FirebaseFirestore db;
    ViewDialog progressDialog;
    adapterplayed rc;
    SwipeRefreshLayout swipeRefresh;
    TextView dateFilter;

    String filterDate = null;
    TextView noData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_played);

        recyclerview = findViewById(R.id.recyclerview);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        dateFilter = findViewById(R.id.dateFilter);
        noData = findViewById(R.id.noData);
        db = FirebaseFirestore.getInstance();

        rc = new adapterplayed(
                this,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        recyclerview.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerview.setAdapter(rc);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(this::loadPlayedMatches);
        dateFilter.setOnClickListener(v -> showSingleDatePicker());
        setDefaultTodayDate();
    }

    private void setDefaultTodayDate() {
        Calendar today = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        int day = today.get(Calendar.DAY_OF_MONTH);
        int month = today.get(Calendar.MONTH) + 1;
        int year = today.get(Calendar.YEAR);

        filterDate = String.format("%04d-%02d-%02d", year, month, day);
        dateFilter.setText(String.format("%02d/%02d/%04d", day, month, year));

        loadPlayedMatches();
    }

    private void showSingleDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    Calendar start = Calendar.getInstance();
                    start.set(year, month, day, 0, 0, 0);

                    Calendar end = Calendar.getInstance();
                    end.set(year, month, day, 23, 59, 59);

                    int realMonth = month + 1;
                    filterDate = String.format("%04d-%02d-%02d", year, realMonth, day);
                    dateFilter.setText(String.format("%02d/%02d/%04d", day, realMonth, year));

                    loadPlayedMatches();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void loadPlayedMatches() {
        progressDialog = new ViewDialog(played.this);
        progressDialog.showDialog();

        String mobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("mobile", null);

        Query query = db.collection("played")
                .whereEqualTo("mobile", mobile);

        if (filterDate != null) {
            query = query
                    .whereEqualTo("date", filterDate);
        }

        query = query.orderBy("timestamp", Query.Direction.DESCENDING);

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    progressDialog.hideDialog();
                    swipeRefresh.setRefreshing(false);

                    ArrayList<String> date = new ArrayList<>();
                    ArrayList<String> bazar = new ArrayList<>();
                    ArrayList<String> amount = new ArrayList<>();
                    ArrayList<String> bet = new ArrayList<>();
                    ArrayList<String> gameName = new ArrayList<>();
                    ArrayList<String> gameType = new ArrayList<>();
                    ArrayList<String> result = new ArrayList<>();
                    ArrayList<String> playedTime = new ArrayList<>();
                    ArrayList<String> winAmount = new ArrayList<>();
                    ArrayList<String> market_result = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        date.add(doc.getString("date") != null ? doc.getString("date") : "-");
                        bazar.add(doc.getString("market") != null ? doc.getString("market") : "-");
                        amount.add(doc.getString("amount") != null ? doc.getString("amount") : "0");
                        bet.add(doc.getString("bet") != null ? doc.getString("bet") : "-");
                        gameName.add(doc.getString("game") != null ? doc.getString("game") : "-");
                        gameType.add(doc.getString("gameType") != null ? doc.getString("gameType") : "-");
                        result.add(doc.getString("result") != null ? doc.getString("result") : "-");
                        playedTime.add(doc.getString("time") != null ? doc.getString("time") : "-");
                        winAmount.add(doc.getString("win_amount") != null ? doc.getString("win_amount") : "-");
                        market_result.add(doc.getString("market_result") != null ? doc.getString("market_result") : "***-**-***");
                    }

                    if (date.isEmpty()) {
                        noData.setVisibility(View.VISIBLE);
                        recyclerview.setVisibility(View.GONE);
                    } else {
                        noData.setVisibility(View.GONE);
                        recyclerview.setVisibility(View.VISIBLE);
                        rc = new adapterplayed(played.this, date, bazar, amount, bet, gameName, gameType, result, playedTime, winAmount, market_result);
                        recyclerview.setAdapter(rc);
                    }
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    progressDialog.hideDialog();
                    Log.d("On Fail Played", e.getMessage());
                    Toast.makeText(played.this,
                            "Failed to load data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
