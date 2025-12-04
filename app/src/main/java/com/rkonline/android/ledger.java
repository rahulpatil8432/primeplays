package com.rkonline.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

/**
 * Firestore-backed ledger showing each played record and computing totals:
 *  - totalPlayed (sum of amount)
 *  - totalWon (sum of win)
 *  - net = totalWon - totalPlayed
 *
 * It expects each document in "played" to contain fields:
 *  - date (String)
 *  - bazar (String)      // market name
 *  - bet (String)
 *  - amount (String)     // numeric in string form
 *  - win (String)        // numeric in string form (optional; default "0")
 *  - mobile (String)
 */
public class ledger extends AppCompatActivity {

    protected RecyclerView recyclerview;
    ViewDialog progressDialog;
    FirebaseFirestore db;

    TextView tvTotalPlayed, tvTotalWon, tvTotalNet;

    ArrayList<String> date = new ArrayList<>();
    ArrayList<String> remark = new ArrayList<>();
    ArrayList<String> amount = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger); // keep your existing layout file name

        initView();
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.back).setOnClickListener(v -> finish());

        loadLedgerFromFirestore();
    }

    private void loadLedgerFromFirestore() {
        progressDialog = new ViewDialog(ledger.this);
        progressDialog.showDialog();

        String mobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("mobile", null);

        if (mobile == null) {
            progressDialog.hideDialog();
            Toast.makeText(this, "No mobile number found in prefs", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("played")
                .whereEqualTo("mobile", mobile)
                .orderBy("date", Query.Direction.DESCENDING)
                .orderBy("__name__", Query.Direction.DESCENDING) // stable ordering
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressDialog.hideDialog();

                    date.clear();
                    remark.clear();
                    amount.clear();

                    double totalPlayed = 0.0;
                    double totalWon = 0.0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // Safe parsing: use getString and defaults
                        String sDate = defaultString(doc.getString("date"), "");
                        String bazar = defaultString(doc.getString("bazar"), "");
                        String bet = defaultString(doc.getString("bet"), "");
                        String sAmount = defaultString(doc.getString("amount"), "0");
                        String sWin = defaultString(doc.getString("win"), "0");

                        // narration (what appears in "Narration" column)
                        String narration = bazar;
                        if (!bet.isEmpty()) {
                            narration += " | " + bet;
                        }
                        // optionally append win if present
                        if (!sWin.equals("") && !sWin.equals("0")) {
                            narration += " | Win: " + sWin;
                        }

                        // parse numbers defensively
                        double dAmount = parseDoubleSafe(sAmount);
                        double dWin = parseDoubleSafe(sWin);

                        // keep totals
                        totalPlayed += dAmount;
                        totalWon += dWin;

                        // add to lists (keep same format as original)
                        date.add(sDate);
                        amount.add(formatAmountForDisplay(dAmount));
                        remark.add(narration);
                    }

                    // update totals UI
                    tvTotalPlayed.setText(formatAmountForDisplay(totalPlayed));
                    tvTotalWon.setText(formatAmountForDisplay(totalWon));
                    double net = totalWon - totalPlayed;
                    tvTotalNet.setText(formatAmountForDisplay(net));

                    // set adapter
                    adaptertransaction rc =
                            new adaptertransaction(ledger.this, date, remark, amount);

                    recyclerview.setLayoutManager(new GridLayoutManager(ledger.this, 1));
                    recyclerview.setAdapter(rc);
                    rc.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    progressDialog.hideDialog();
                    Log.e("ledger", "Failed to load ledger", e);
                    Toast.makeText(ledger.this,
                            "Failed to load data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String defaultString(String s, String def) {
        return s == null ? def : s;
    }

    private double parseDoubleSafe(String s) {
        if (s == null) return 0.0;
        s = s.trim();
        if (s.isEmpty()) return 0.0;
        try {
            // allow comma separators if any
            s = s.replace(",", "");
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private String formatAmountForDisplay(double v) {
        // keep integer display if it is an integer (like your older UI)
        if (Math.abs(v - Math.round(v)) < 0.0001) {
            return String.valueOf((long) Math.round(v));
        } else {
            return String.format("%.2f", v);
        }
    }

    private void initView() {
        recyclerview = findViewById(R.id.recyclerview);

        // new TextViews added to layout to show totals (see layout below)
        tvTotalPlayed = findViewById(R.id.total_played);
        tvTotalWon = findViewById(R.id.total_won);
        tvTotalNet = findViewById(R.id.total_net);
    }
}
