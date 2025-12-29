package com.rkonline.android;

import static com.rkonline.android.utils.CommonUtils.canPlaceBet;
import static com.rkonline.android.utils.CommonUtils.soundPlayAndVibrate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.rkonline.android.adapter.SelectedNumberAdapter;
import com.rkonline.android.utils.BetEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class betting extends AppCompatActivity {

    private Spinner type, numbersList;
    private Button submit;
    private TextView totalamount;
    private RecyclerView selectedNumberRecycler;
    private SharedPreferences prefs;
    String market, game, openTime, closeTime;
    private ArrayList<String> numberList = new ArrayList<>();
    private ArrayList<String> selectedNumbers = new ArrayList<>();
    private ArrayList<String> amounts = new ArrayList<>();
    private boolean[] selectedFlags;
    private boolean isDialogOpen = false;
    private String selectedGameType;
    ViewDialog progressDialog;
    private SelectedNumberAdapter adapter;
    boolean closeNextDay;

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
        closeNextDay = getIntent().getBooleanExtra("closeNextDay",false);
        boolean isMarketOpen = getIntent().getBooleanExtra("isMarketOpen", false);
        // Setup type spinner (Open/Close)
        ArrayList<String> types = new ArrayList<>();
        if (isMarketOpen) {
            types.add("Close");
        } else if(!isMarketOpen && game.equals("Jodi")) {
            types.add("Open");
        }else{
            types.add("Open");
            types.add("Close");
        }
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(typeAdapter);
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedGameType = adapterView.getItemAtPosition(i).toString();
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // Setup numbers spinner
        numberList = getIntent().getStringArrayListExtra("list");
        selectedFlags = new boolean[numberList.size()];
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Select Numbers"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numbersList.setAdapter(spinnerAdapter);

        numbersList.setOnTouchListener((v, event) -> {
            if (!isDialogOpen && event.getAction() == MotionEvent.ACTION_UP) {
                syncSelectedFlags();
                showMultiSelectDialog();
            }
            return true;
        });

        // RecyclerView
        adapter = new SelectedNumberAdapter(selectedNumbers, amounts, new SelectedNumberAdapter.OnAmountChangedListener() {
            @Override
            public void onAmountChanged(int total) {
                totalamount.setText("Total: " + total);
                totalamount.startAnimation(
                        android.view.animation.AnimationUtils.loadAnimation(
                                betting.this, R.anim.pulse
                        )
                );
            }

            @Override
            public void onDelete(int position) {

                String deletedNumber = selectedNumbers.get(position);

                // 1️⃣ Remove from lists
                selectedNumbers.remove(position);
                amounts.remove(position);
                adapter.notifyItemRemoved(position);

                // 2️⃣ Uncheck from selectedFlags
                int indexInMaster = numberList.indexOf(deletedNumber);
                if (indexInMaster != -1) {
                    selectedFlags[indexInMaster] = false;
                }

                // 3️⃣ Update spinner text & total
                updateSpinnerText();
                updateTotal();
            }

        });

        selectedNumberRecycler.setLayoutManager(new GridLayoutManager(this,2));
        selectedNumberRecycler.setItemAnimator(
                new androidx.recyclerview.widget.DefaultItemAnimator()
        );

        selectedNumberRecycler.setAdapter(adapter);

        submit.setOnClickListener(v ->{
            Log.e("time",openTime + "  "+ closeTime + " "+ selectedGameType);
            if (!canPlaceBet(betting.this, selectedGameType, openTime, closeTime, closeNextDay)) {
                return;
            }
            handleBetSubmit();
        });
    }

    private void syncSelectedFlags() {
        for (int i = 0; i < selectedFlags.length; i++) {
            selectedFlags[i] = selectedNumbers.contains(numberList.get(i));
        }
    }

    private void initView() {
        type = findViewById(R.id.type);
        numbersList = findViewById(R.id.numbersList);
        submit = findViewById(R.id.submit);
        totalamount = findViewById(R.id.totalamount);
        selectedNumberRecycler = findViewById(R.id.selectedNumberRecycler);
        prefs = getSharedPreferences(constant.prefs, MODE_PRIVATE);
    }

    private void showMultiSelectDialog() {

        isDialogOpen = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Numbers");

        View view = getLayoutInflater().inflate(R.layout.dialog_search_multiselect, null);
        builder.setView(view);

        EditText searchEdit = view.findViewById(R.id.searchEdit);
        ListView listView = view.findViewById(R.id.listView);

        ArrayList<String> filteredList = new ArrayList<>(numberList);

        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                filteredList
        );
        listView.setAdapter(listAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // ✅ Method to sync checked state
        Runnable syncChecks = () -> {
            for (int i = 0; i < filteredList.size(); i++) {
                int originalIndex = numberList.indexOf(filteredList.get(i));
                listView.setItemChecked(i,
                        originalIndex != -1 && selectedFlags[originalIndex]);
            }
        };

        syncChecks.run();

        // ✅ Correct click handling
        listView.setOnItemClickListener((parent, v, position, id) -> {
            String value = filteredList.get(position);
            int originalIndex = numberList.indexOf(value);
            if (originalIndex != -1) {
                selectedFlags[originalIndex] = !selectedFlags[originalIndex];
            }
            syncChecks.run(); // 🔥 keep list stable
        });

        // 🔍 Search filter (SAFE)
        searchEdit.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String query = s.toString().toLowerCase();
                filteredList.clear();

                for (String num : numberList) {
                    if (num.toLowerCase().contains(query)) {
                        filteredList.add(num);
                    }
                }

                listAdapter.notifyDataSetChanged();
                syncChecks.run(); // 🔥 CRITICAL
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {

            selectedNumbers.clear();
            amounts.clear();

            for (int i = 0; i < numberList.size(); i++) {
                if (selectedFlags[i]) {
                    selectedNumbers.add(numberList.get(i));
                    amounts.add("");
                }
            }

            adapter.notifyDataSetChanged();
            updateSpinnerText();
            updateTotal();
            isDialogOpen = false;
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> isDialogOpen = false);
        builder.setOnCancelListener(dialog -> isDialogOpen = false);

        builder.show();

    }


    private void updateSpinnerText() {
        StringBuilder display = new StringBuilder();
        for (String num : selectedNumbers) display.append(num).append(", ");
        String text = display.length() > 0 ? "Selected "+selectedNumbers.size() + " Numbers": "Select Numbers";

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{text});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numbersList.setAdapter(adapter);
    }

    private void updateTotal() {
        int total = 0;
        for (String amt : amounts) {
            try {
                total += Integer.parseInt(amt);
            } catch (NumberFormatException e) { total += 0; }
        }
        totalamount.setText("Total: " + total);
    }

    private void handleBetSubmit() {

        int total = 0;

        for (int i = 0; i < amounts.size(); i++) {
            String amtStr = amounts.get(i);

            if (TextUtils.isEmpty(amtStr)) {
                showAlert("Please enter amount for number " + selectedNumbers.get(i));
                return;
            }

            int amt;
            try {
                amt = Integer.parseInt(amtStr);
            } catch (NumberFormatException e) {
                showAlert("Invalid amount for number " + selectedNumbers.get(i));
                return;
            }

            if (amt < 10 || amt > 10000) {
                showAlert("Amount for number " + selectedNumbers.get(i) +
                        " must be between 10 and 10000");
                return;
            }

            total += amt;
        }

        if (total < constant.min_total || total > constant.max_total) {
            showAlert("Total bet amount must be between " +
                    constant.min_total + " and " + constant.max_total);
            return;
        }
        placeBetsWithEngine();
    }


    private void placeBetsWithEngine() {

        progressDialog = new ViewDialog(betting.this);
        progressDialog.showDialog();
        submit.setEnabled(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String mobile = prefs.getString("mobile", "");

        for (int i = 0; i < selectedNumbers.size(); i++) {

            String betNum = selectedNumbers.get(i);
            int amount = Integer.parseInt(amounts.get(i));

            Map<String, Object> extras = new HashMap<>();
            extras.put("gameType", selectedGameType);

            BetEngine.placeBet(
                    db,
                    mobile,
                    market,
                    game,
                    betNum,
                    amount,
                    "Bet placed - " + market,
                    extras,
                    new BetEngine.BetCallback() {

                        int completed = 0;
                        boolean failed = false;

                        @Override
                        public synchronized void onSuccess(int newWallet) {
                            if (failed) return;

                            completed++;

                            if (completed == selectedNumbers.size()) {
                                prefs.edit().putString("wallet", String.valueOf(newWallet)).apply();
                                onAllBetsComplete();
                            }
                        }

                        @Override
                        public synchronized void onFailure(String error) {
                            if (failed) return;

                            failed = true;
                            submit.setEnabled(true);
                            progressDialog.hideDialog();
                            Toast.makeText(betting.this, error, Toast.LENGTH_LONG).show();
                        }
                    }
            );
        }
    }


    private void onAllBetsComplete() {
        progressDialog.hideDialog();

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        soundPlayAndVibrate(betting.this, vibrator);

        Toast.makeText(betting.this, "Bet placed successfully 🎉", Toast.LENGTH_SHORT).show();
        goThankYou();
    }

    private void goThankYou() {
        Intent in = new Intent(betting.this, thankyou.class);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
        finish();
    }

    private void showAlert(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }
}
