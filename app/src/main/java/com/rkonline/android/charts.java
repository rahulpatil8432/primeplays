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

import java.util.ArrayList;
import java.util.List;

public class charts extends AppCompatActivity {

    RecyclerView chartRecycler;
    ChartsAdapter adapter;
    List<ChartModel> chartList = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String marketId;
    TextView emptyView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        emptyView = findViewById(R.id.emptyView);
        chartRecycler = findViewById(R.id.chartRecycler);
        chartRecycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChartsAdapter(this, chartList);
        chartRecycler.setAdapter(adapter);

        findViewById(R.id.back).setOnClickListener(v -> finish());

        marketId = getIntent().getStringExtra("href")
                .replace(".php", "")
                .trim();

        loadChartData();
    }

    private void loadChartData() {
        db.collection("markets")
                .document(marketId)
                .collection("winning_charts")
                .orderBy("date",    Query.Direction.DESCENDING)
                .limit(7)
                .get()
                .addOnSuccessListener(snap -> {
                    chartList.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {

                        ChartModel m = new ChartModel();
                        m.date = doc.getString("date");
                        m.aankdoOpen = doc.getString("aankdo_open");
                        m.aankdoClose = doc.getString("aankdo_close");
                        m.jodi = doc.getString("jodi");

                        chartList.add(m);
                    }
                    adapter.notifyDataSetChanged();
                    emptyView.setVisibility(chartList.isEmpty() ? View.VISIBLE : View.GONE);
                    chartRecycler.setVisibility(chartList.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }
}