package com.rkonline.android;

import static com.rkonline.android.utils.CommonUtils.canPlaceBet;
import static com.rkonline.android.utils.CommonUtils.soundPlayAndVibrate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.util.HashMap;
import java.util.Map;

public class betting extends AppCompatActivity {

    private Spinner type;
    private Button submit;
    private TextView totalamount;
    private RecyclerView selectedNumberRecycler;
    private LinearLayout filterRow;
    private SharedPreferences prefs;

    String market, game, openTime, closeTime;
    boolean closeNextDay;

    private ArrayList<String> masterNumbers = new ArrayList<>();
    private ArrayList<String> filteredNumbers = new ArrayList<>();

    private final Map<String, String> amountMap = new HashMap<>();

    private String selectedGameType;
    private SelectedNumberAdapter adapter;
    ViewDialog progressDialog;

    private LinearLayout allAmountsContainer;
    private LinearLayout amountHeaderRow;
    ScrollView scrollForPlayed;

    private int selectedFilter = -1;
    private final ArrayList<Button> filterButtons = new ArrayList<>();

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
            if (!canPlaceBet(this, selectedGameType, openTime, closeTime, closeNextDay)) return;
            handleBetSubmit();
        });
    }

    private void initView() {
        type = findViewById(R.id.type);
        submit = findViewById(R.id.submit);
        totalamount = findViewById(R.id.totalamount);
        allAmountsContainer = findViewById(R.id.allAmountsContainer);
        selectedNumberRecycler = findViewById(R.id.selectedNumberRecycler);
        filterRow = findViewById(R.id.filterRow);
        amountHeaderRow = findViewById(R.id.amountHeaderRow);
        scrollForPlayed = findViewById(R.id.scrollForPlayed);
        amountHeaderRow.setVisibility(View.GONE);

        prefs = getSharedPreferences(constant.prefs, MODE_PRIVATE);
    }

    private void setupTypeSpinner(boolean isMarketOpen) {
        ArrayList<String> types = new ArrayList<>();

        if (isMarketOpen) types.add("Close");
        else if (game.equals("Jodi") || game.equals("Red Jodi")) types.add("Open");
        else {
            types.add("Open");
            types.add("Close");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedGameType = parent.getItemAtPosition(position).toString();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new SelectedNumberAdapter(
                filteredNumbers,
                amountMap,
                this::updateTotal
        );

        selectedNumberRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        selectedNumberRecycler.setAdapter(adapter);
    }

    private void loadNumbersDirectly() {
        masterNumbers = getIntent().getStringArrayListExtra("list");

        filteredNumbers.clear();
        amountMap.clear();

        for (String num : masterNumbers) {
            filteredNumbers.add(num);
            amountMap.put(num, "");
        }

        if (!game.equals("Single Ank") && !game.equals("Triple Pana")) {
            setupFilterRow();
        }

        adapter.notifyDataSetChanged();
        updateTotal();
    }

    private void setupFilterRow() {
        filterRow.removeAllViews();
        filterButtons.clear();

        for (int i = 0; i <= 9; i++) {
            int digit = i;
            Button btn = new Button(this);
            btn.setText(String.valueOf(i));
            btn.setAllCaps(false);
            btn.setBackgroundResource(R.drawable.filter_button_bg);
            btn.setTextColor(getResources().getColor(R.color.md_black_1000));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );
            params.setMargins(4, 4, 4, 4);
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> {
                selectedFilter = digit;
                if(game.equals("Jodi") || game.equals("Red Jodi")){
                    applyJodiFilter();
                }else{
                    applyFilter();
                }
                updateFilterHighlight();
            });
            filterRow.addView(btn);
            filterButtons.add(btn);
        }
    }

    private void updateFilterHighlight() {
        for (int i = 0; i < filterButtons.size(); i++) {
            Button btn = filterButtons.get(i);
            if (i == selectedFilter) {
                btn.setBackgroundResource(R.drawable.filter_button_bg_selected);
                btn.setTextColor(getResources().getColor(R.color.md_white_1000));
            } else {
                btn.setBackgroundResource(R.drawable.filter_button_bg);
                btn.setTextColor(getResources().getColor(R.color.md_black_1000));
            }
        }
    }

    private void applyFilter() {
        filteredNumbers.clear();

        for (String num : masterNumbers) {
            int sum = 0;
            for (char c : num.toCharArray()) sum += c - '0';

            if (sum % 10 == selectedFilter) filteredNumbers.add(num);
        }

        adapter.notifyDataSetChanged();
        updateTotal();
    }

    private void applyJodiFilter() {
        filteredNumbers.clear();

        for (String num : masterNumbers) {
            if (num.charAt(0) - '0' == selectedFilter) filteredNumbers.add(num);
        }

        adapter.notifyDataSetChanged();
        updateTotal();
    }

    private void updateTotal() {
        int total = 0;
        for (String amt : amountMap.values()) {
            if (!TextUtils.isEmpty(amt)) {
                try { total += Integer.parseInt(amt); }
                catch (Exception ignored) {}
            }
        }
        totalamount.setText("Total: " + total);
        if (total > 0) {
            showALLPlayedBet();
        } else {
            amountHeaderRow.setVisibility(View.GONE);
            allAmountsContainer.setVisibility(View.GONE);
            scrollForPlayed.setVisibility(View.GONE);
        }
    }

    private void showALLPlayedBet() {
        allAmountsContainer.removeAllViews();
        amountHeaderRow.setVisibility(View.VISIBLE);
        allAmountsContainer.setVisibility(View.VISIBLE);
        scrollForPlayed.setVisibility(View.VISIBLE);
        boolean alternate = false;
        for (String num : masterNumbers) {
            String amt = amountMap.get(num);
            if (!TextUtils.isEmpty(amt)) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(4, 4, 4, 4);
                if (alternate) {
                    row.setBackgroundColor(getResources().getColor(R.color.md_grey_100));

                } else {
                    row.setBackgroundColor(getResources().getColor(R.color.md_white_1000));
                }
                alternate = !alternate;

                TextView numTv = new TextView(this);
                numTv.setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                numTv.setGravity(android.view.Gravity.CENTER);
                numTv.setText(num);

                TextView amtTv = new TextView(this);
                amtTv.setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                amtTv.setGravity(android.view.Gravity.CENTER);
                amtTv.setText(amt);

                row.addView(numTv);
                row.addView(amtTv);
                allAmountsContainer.addView(row);
            }
        }
    }


    private void handleBetSubmit() {

        int total = 0;
        boolean hasBet = false;

        for (String num : amountMap.keySet()) {
            String amtStr = amountMap.get(num);
            if (TextUtils.isEmpty(amtStr)) continue;

            hasBet = true;
            int amt = Integer.parseInt(amtStr);

            if (amt < 10 || amt > 10000) {
                AlertHelper.showCustomAlert(this, "Info!",
                        "Amount for number " + num + " must be between 10 and 10000",
                        R.drawable.info_icon, 0);
                return;
            }
            total += amt;
        }

        if (!hasBet) {
            AlertHelper.showCustomAlert(this, "Info!",
                    "Please enter at least one amount",
                    R.drawable.info_icon, 0);
            return;
        }

        if (total < constant.min_total || total > constant.max_total) {
            AlertHelper.showCustomAlert(this, "Info!",
                    "Total bet must be between " + constant.min_total + " and " + constant.max_total,
                    R.drawable.info_icon, 0);
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

        for (String num : amountMap.keySet()) {
            String amt = amountMap.get(num);
            if (!TextUtils.isEmpty(amt)) {
                betItems.add(new BetEngine.BetItem(num, Integer.parseInt(amt)));
            }
        }

        BetEngine.placeMultipleBets(db, mobile, market, game, selectedGameType, betItems,
                new BetEngine.BetCallback() {
                    @Override public void onSuccess(int newWallet) {
                        prefs.edit().putString("wallet", String.valueOf(newWallet)).apply();
                        onAllBetsComplete();
                    }

                    @Override public void onFailure(String error) {
                        submit.setEnabled(true);
                        progressDialog.hideDialog();
                    }
                });
    }

    private void onAllBetsComplete() {
        progressDialog.hideDialog();
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        soundPlayAndVibrate(this, vibrator);
        startActivity(new Intent(this, thankyou.class));
        finish();
    }
}
