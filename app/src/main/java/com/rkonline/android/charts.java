package com.rkonline.android;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class charts extends AppCompatActivity {

    RecyclerView chartRecycler;
    ChartsAdapter adapter;
    List<ChartModel> chartList = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String marketId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

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
                .collection("winning_chart")
                .orderBy("date")
                .get()
                .addOnSuccessListener(snap -> {
                    chartList.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        ChartModel m = new ChartModel();
                        m.openResult = doc.getString("openResult");
                        m.closeResult = doc.getString("closeResult");
                        m.date = doc.getString("date");
                        chartList.add(m);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load chart", Toast.LENGTH_SHORT).show()
                );
    }
}