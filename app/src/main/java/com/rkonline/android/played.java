package com.rkonline.android;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class played extends AppCompatActivity {

    RecyclerView recyclerview;

    FirebaseFirestore db;
    ViewDialog progressDialog;

    adapterplayed rc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_played);

        recyclerview = findViewById(R.id.recyclerview);

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
                new ArrayList<>()
        );

        recyclerview.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerview.setAdapter(rc);

        findViewById(R.id.back).setOnClickListener(v -> finish());

        loadPlayedMatches();
    }

    private void loadPlayedMatches() {

        progressDialog = new ViewDialog(played.this);
        progressDialog.showDialog();

        String mobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("mobile", null);

        db.collection("played")
                .whereEqualTo("mobile", mobile)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressDialog.hideDialog();

                    ArrayList<String> date = new ArrayList<>();
                    ArrayList<String> bazar = new ArrayList<>();
                    ArrayList<String> amount = new ArrayList<>();
                    ArrayList<String> bet = new ArrayList<>();
                    ArrayList<String> gameName = new ArrayList<>();
                    ArrayList<String> gameType = new ArrayList<>();
                    ArrayList<String> result = new ArrayList<>();
                    ArrayList<String> playedTime = new ArrayList<>();
                    ArrayList<String> winAmount = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {

                        String d = doc.getString("date");
                        String b = doc.getString("market");
                        String a = doc.getString("amount");
                        String bt = doc.getString("bet");
                        String gameN = doc.getString("game");
                        String gameT = doc.getString("gameType");
                        String resultT = doc.getString("result");
                        String playT = doc.getString("time");
                        String winA = doc.getString("win_amount");


                        date.add(d != null ? d : "-");
                        bazar.add(b != null ? b : "-");
                        amount.add(a != null ? a : "0");
                        bet.add(bt != null ? bt : "-");
                        gameName.add(gameN != null ? gameN : "-");
                        gameType.add(gameT != null ? gameT : "-");
                        result.add(resultT != null ? resultT : "-");
                        playedTime.add(playT != null ? playT : "-");
                        winAmount.add(winA != null ? winA : "-");

                    }

                    // 🔥 Update the existing adapter (no warning)
                    rc = new adapterplayed(played.this, date, bazar, amount, bet, gameName,gameType,result,playedTime,winAmount);
                    recyclerview.setAdapter(rc);
                })
                .addOnFailureListener(e -> {
                    progressDialog.hideDialog();
                    Toast.makeText(played.this,
                            "Failed to load data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}