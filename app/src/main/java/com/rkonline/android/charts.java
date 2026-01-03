package com.rkonline.android;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.rkonline.android.adapter.ChartsAdapter;

import java.util.ArrayList;
import java.util.List;

public class charts extends AppCompatActivity {

    private RecyclerView chartRecycler;
    private ChartsAdapter adapter;
    private TextView emptyView;

    private final List<DocumentSnapshot> chartList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String marketId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        chartRecycler = findViewById(R.id.chartRecycler);
        emptyView = findViewById(R.id.emptyView);

        chartRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChartsAdapter(chartList);
        chartRecycler.setAdapter(adapter);

        findViewById(R.id.back).setOnClickListener(v -> finish());

        // marketId passed like: "MILAN NIGHT.php"
        marketId = getIntent().getStringExtra("href");
        if (marketId != null) {
            marketId = marketId.replace(".php", "").trim();
        }

        loadChartData();
    }

    private void loadChartData() {

        if (marketId == null || marketId.isEmpty()) {
            showEmpty();
            return;
        }

        db.collection("markets")
                .document(marketId)
                .collection("winning_charts")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(7)
                .get()
                .addOnSuccessListener(snapshot -> {

                    chartList.clear();

                    if (!snapshot.isEmpty()) {
                        chartList.addAll(snapshot.getDocuments());
                    }

                    adapter.notifyDataSetChanged();

                    if (chartList.isEmpty()) {
                        showEmpty();
                    } else {
                        showList();
                    }
                })
                .addOnFailureListener(e -> showEmpty());
    }

    private void showEmpty() {
        emptyView.setVisibility(View.VISIBLE);
        chartRecycler.setVisibility(View.GONE);
    }

    private void showList() {
        emptyView.setVisibility(View.GONE);
        chartRecycler.setVisibility(View.VISIBLE);
    }
}
