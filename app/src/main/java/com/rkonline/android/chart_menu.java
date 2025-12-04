package com.rkonline.android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class chart_menu extends AppCompatActivity {

    RecyclerView recyclerview;
    EditText search;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<MarketModel> marketList = new ArrayList<>();
    ArrayList<MarketModel> filteredList = new ArrayList<>();

    ChartMenuAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_menu);

        recyclerview = findViewById(R.id.recyclerview);
        search = findViewById(R.id.search);

        recyclerview.setLayoutManager(new GridLayoutManager(this, 1));

        adapter = new ChartMenuAdapter(this, filteredList, market -> {
            startActivity(new Intent(chart_menu.this, charts.class)
                    .putExtra("href", market.marketId));
        });

        recyclerview.setAdapter(adapter);

        findViewById(R.id.back).setOnClickListener(v -> finish());

        loadMarkets();
        setupSearch();
    }

    private void loadMarkets() {
        System.out.println("Inside loadMarkets");
        db.collection("markets")
                .get()
                .addOnSuccessListener(snap -> {
                    System.out.println("loadMarkets success");
                    marketList.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        MarketModel m = new MarketModel();

                        m.marketId    = doc.getId();
                        m.name        = doc.getString("name");
                        m.openResult  = doc.getString("openResult");
                        m.closeResult = doc.getString("closeResult");

                        if (m.openResult == null)  m.openResult = "-";
                        if (m.closeResult == null) m.closeResult = "-";
                        if (m.name == null)        m.name = "";

                        marketList.add(m);
                    }


                    filteredList.clear();
                    filteredList.addAll(marketList);

                    System.out.println("filteredList "+filteredList);

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->{
                        System.out.println("loadMarkets failed");
                        Toast.makeText(chart_menu.this, "Failed to load charts", Toast.LENGTH_SHORT).show();
    });
    }

    private void setupSearch() {
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String q = s.toString().toLowerCase();
                filteredList.clear();

                if (q.isEmpty()) {
                    filteredList.addAll(marketList);
                } else {
                    for (MarketModel m : marketList) {
                        if (m.name.toLowerCase().contains(q)) {
                            filteredList.add(m);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}