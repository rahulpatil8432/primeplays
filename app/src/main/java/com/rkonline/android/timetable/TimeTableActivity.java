package com.rkonline.android.timetable;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rkonline.android.R;
import com.rkonline.android.model.MarketModel;

import java.util.ArrayList;

public class TimeTableActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);
        findViewById(R.id.back).setOnClickListener(view -> finish());
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        loadTable();
    }

    private void loadTable() {
        db.collection("markets")
                .orderBy("open_time")
                .get()
                .addOnSuccessListener(query -> {

                    ArrayList<MarketModel> list = new ArrayList<>();

                    for (DocumentSnapshot doc : query) {
                        String name = doc.getString("market_name");
                        String open = doc.getString("open_time_formatted");
                        String close = doc.getString("close_time_formatted");

                        list.add(new MarketModel(name, open, close));
                    }
                    TimeTableAdapter adapter = new TimeTableAdapter(list);

                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }).addOnFailureListener(query -> {
                    Log.d("Failed..",query.getMessage());
                });
    }
}
