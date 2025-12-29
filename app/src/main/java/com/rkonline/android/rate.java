package com.rkonline.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class rate extends AppCompatActivity {

    ViewDialog progressDialog;
    FirebaseFirestore db;
    RecyclerView recyclerview;
    protected TextView headerTitle;
    String market,openTime,closeTime;

    boolean closeNextDay;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_game);
        initView();
        headerTitle.setText(getIntent().getStringExtra("header"));
        market = getIntent().getStringExtra("market");
        openTime = getIntent().getStringExtra("openTime");
        closeTime = getIntent().getStringExtra("closeTime");
        closeNextDay = getIntent().getBooleanExtra("closeNextDay",false);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        apicall();
    }


    private void apicall() {

        progressDialog = new ViewDialog(rate.this);
        progressDialog.showDialog();

        db.collection("games").orderBy("order").get().addOnCompleteListener(task -> {
            progressDialog.hideDialog();
            if (task.isSuccessful()) {
                ArrayList<String> name = new ArrayList<>();
                ArrayList<String> rate = new ArrayList<>();


                for (QueryDocumentSnapshot document : task.getResult()) {

                    Map<String, Object> game = document.getData();

                    String gameName = (String) game.get("name");
                    String gameRate = (String) game.get("rate");
                    Boolean isActive = (Boolean) game.get("isActive");

                    if (isActive == null || !isActive) continue;
                    if (headerTitle.getText().toString().equalsIgnoreCase("Game Rate")) {
                        if (gameRate != null && !gameRate.trim().isEmpty()) {
                            name.add(gameName);
                            rate.add(gameRate);
                        }
                    } else {
                        name.add(gameName);
                    }
                }
                adapter_game rc;
                Log.e("header",headerTitle.getText().toString());
                if(headerTitle.getText().toString().equalsIgnoreCase("Game Rate")){
                   rc = new adapter_game(rate.this,name,rate,market, false, openTime, closeTime, closeNextDay);
                    recyclerview.setLayoutManager(new LinearLayoutManager(rate.this));

                }else{
                    boolean isMarketOpen = getIntent().getBooleanExtra("isMarketOpen",false);
                    if(isMarketOpen){
                        name.remove("Jodi");
                        name.remove("Crossing");
                        name.remove("Half Sangam");
                        name.remove("Full Sangam");
                    }
                    rc = new adapter_game(rate.this,name,new ArrayList<>(), market, isMarketOpen, openTime, closeTime, closeNextDay);
                    recyclerview.setLayoutManager(new GridLayoutManager(rate.this,2));

                }
                recyclerview.setAdapter(rc);
                rc.notifyDataSetChanged();
            } else {
                Toast.makeText(rate.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initView() {
        db = FirebaseFirestore.getInstance();
        recyclerview = findViewById(R.id.gameRecyclerView);
        headerTitle = findViewById(R.id.header_title);

    }
}