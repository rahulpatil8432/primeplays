package com.rkonline.android;

import static com.rkonline.android.utils.CommonUtils.canPlaceBet;
import static com.rkonline.android.utils.CommonUtils.soundPlayAndVibrate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.rkonline.android.adapter.SelectedNumberAdapter;
import com.rkonline.android.utils.AlertHelper;
import com.rkonline.android.utils.BetEngine;

import java.util.ArrayList;

public class betting extends AppCompatActivity {

    private Spinner type;
    private Button submit;
    private TextView totalamount;
    private RecyclerView selectedNumberRecycler;
    private SharedPreferences prefs;

    String market, game, openTime, closeTime;
    boolean closeNextDay;

    private ArrayList<String> numberList = new ArrayList<>();
    private ArrayList<String> selectedNumbers = new ArrayList<>();
    private ArrayList<String> amounts = new ArrayList<>();

    private String selectedGameType;
    private SelectedNumberAdapter adapter;
    ViewDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_betting);

        initView();

        findViewById(R.id.back).setOnClickListener(v -> finish());

        game = getIntent().getStringExtra("game");
        market = getIntent().getStringExtra("market");
        openTime = getIntent().getStringExtra("openTime");
        closeTime = getIntent().getStringExtra("closeTime");
        closeNextDay = getIntent().getBooleanExtra("closeNextDay", false);
        boolean isMarketOpen = getIntent().getBooleanExtra("isMarketOpen", false);

        setupTypeSpinner(isMarketOpen);
        setupRecyclerView();
        loadNumbersDirectly();

        submit.setOnClickListener(v -> {
            Log.e("time", openTime + " " + closeTime + " " + selectedGameType);
            if (!canPlaceBet(this, selectedGameType, openTime, closeTime, closeNextDay)) {
                return;
            }
            handleBetSubmit();
        });
    }

    private void initView() {
        type = findViewById(R.id.type);
        submit = findViewById(R.id.submit);
        totalamount = findViewById(R.id.totalamount);
        selectedNumberRecycler = findViewById(R.id.selectedNumberRecycler);
        prefs = getSharedPreferences(constant.prefs, MODE_PRIVATE);
    }

    private void setupTypeSpinner(boolean isMarketOpen) {
        ArrayList<String> types = new ArrayList<>();

        if (isMarketOpen) {
            types.add("Close");
        } else if (game.equals("Jodi") || game.equals("Red Jodi")) {
            types.add("Open");
        } else {
            types.add("Open");
            types.add("Close");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                types
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGameType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new SelectedNumberAdapter(
                selectedNumbers,
                amounts,
                new SelectedNumberAdapter.OnAmountChangedListener() {
                    @Override
                    public void onAmountChanged(int total) {
                        totalamount.setText("Total: " + total);
                    }

                    @Override
                    public void onDelete(int position) {
                    }
                }
        );

        selectedNumberRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        selectedNumberRecycler.setAdapter(adapter);
    }

    private void loadNumbersDirectly() {
        numberList = getIntent().getStringArrayListExtra("list");

        selectedNumbers.clear();
        amounts.clear();

        for (String num : numberList) {
            selectedNumbers.add(num);
            amounts.add("");
        }

        adapter.notifyDataSetChanged();
        updateTotal();
    }

    private void updateTotal() {
        int total = 0;
        for (String amt : amounts) {
            try {
                total += Integer.parseInt(amt);
            } catch (Exception ignored) {}
        }
        totalamount.setText("Total: " + total);
    }

    private void handleBetSubmit() {

        int total = 0;
        boolean hasAtLeastOneBet = false;

        for (int i = 0; i < amounts.size(); i++) {
            String amtStr = amounts.get(i);

            if (TextUtils.isEmpty(amtStr)) continue;

            hasAtLeastOneBet = true;

            int amt;
            try {
                amt = Integer.parseInt(amtStr);
            } catch (NumberFormatException e) {
                AlertHelper.showCustomAlert(
                        this,
                        "Info!",
                        "Invalid amount for number " + selectedNumbers.get(i),
                        R.drawable.info_icon,
                        0
                );
                return;
            }

            if (amt < 10 || amt > 10000) {
                AlertHelper.showCustomAlert(
                        this,
                        "Info!",
                        "Amount for number " + selectedNumbers.get(i)
                                + " must be between 10 and 10000",
                        R.drawable.info_icon,
                        0
                );
                return;
            }

            total += amt;
        }

        if (!hasAtLeastOneBet) {
            AlertHelper.showCustomAlert(
                    this,
                    "Info!",
                    "Please enter at least one amount",
                    R.drawable.info_icon,
                    0
            );
            return;
        }

        if (total < constant.min_total || total > constant.max_total) {
            AlertHelper.showCustomAlert(
                    this,
                    "Info!",
                    "Total bet amount must be between "
                            + constant.min_total + " and " + constant.max_total,
                    R.drawable.info_icon,
                    0
            );
            return;
        }

        placeBetsWithEngine();
    }


    private void placeBetsWithEngine() {

        progressDialog = new ViewDialog(this);
        progressDialog.showDialog();
        submit.setEnabled(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String mobile = prefs.getString("mobile", "");

        ArrayList<BetEngine.BetItem> betItems = new ArrayList<>();

        for (int i = 0; i < selectedNumbers.size(); i++) {
            if (!TextUtils.isEmpty(amounts.get(i))) {
                betItems.add(
                        new BetEngine.BetItem(
                                selectedNumbers.get(i),
                                Integer.parseInt(amounts.get(i))
                        )
                );
            }
        }

        BetEngine.placeMultipleBets(
                db,
                mobile,
                market,
                game,
                selectedGameType,
                betItems,
                new BetEngine.BetCallback() {
                    @Override
                    public void onSuccess(int newWallet) {
                        prefs.edit().putString("wallet", String.valueOf(newWallet)).apply();
                        onAllBetsComplete();
                    }

                    @Override
                    public void onFailure(String error) {
                        submit.setEnabled(true);
                        progressDialog.hideDialog();
                        AlertHelper.showCustomAlert(
                                betting.this,
                                "Sorry!",
                                "Something went wrong",
                                0,
                                0
                        );
                    }
                }
        );
    }

    private void onAllBetsComplete() {
        progressDialog.hideDialog();
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        soundPlayAndVibrate(this, vibrator);
        goThankYou();
    }

    private void goThankYou() {
        Intent in = new Intent(this, thankyou.class);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
        finish();
    }
}
