package com.rkonline.android;

import static com.rkonline.android.utils.CommonUtils.canPlaceBet;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.rkonline.android.utils.AlertHelper;
import com.rkonline.android.utils.BetEngine;
import com.rkonline.android.utils.CommonUtils;
import com.rkonline.android.utils.GameData;
import com.rkonline.android.utils.PlayedBetRenderer;

import java.util.ArrayList;
import java.util.Objects;

public class crossing extends AppCompatActivity {

    private EditText number, amount;
    private Spinner type;

    private TextView totalamount,Heading;
    private Button submit;

    private LinearLayout allAmountsContainer, amountHeaderRow;
    private ScrollView scrollForPlayed;

    private SharedPreferences prefs;

    private ArrayList<String> numbers = new ArrayList<>();
    private String market, game, openTime, closeTime;
    boolean closeNextDay;
    private ViewDialog progressDialog;
    private String selectedGameType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crossing);
        initView();

        findViewById(R.id.back).setOnClickListener(v -> finish());

        prefs = getSharedPreferences(constant.prefs, MODE_PRIVATE);
        game = getIntent().getStringExtra("game");
        market = getIntent().getStringExtra("market");
        boolean isMarketOpen = getIntent().getBooleanExtra("isMarketOpen", false);
        setupTypeSpinner(isMarketOpen);
        openTime = getIntent().getStringExtra("openTime");
        closeTime = getIntent().getStringExtra("closeTime");
        closeNextDay = getIntent().getBooleanExtra("closeNextDay", false);
        Heading.setText(game);
        if(Objects.equals(game, "Crossing")){
            type.setVisibility(View.GONE);
            selectedGameType = null;
        }
//        submit.
        submit.setOnClickListener(v -> {
            if (!canPlaceBet(this, selectedGameType, openTime, closeTime, closeNextDay)) return;
            handleSubmit();
        });
    }


    private void initView() {
        number = findViewById(R.id.number);
        amount = findViewById(R.id.amount);
        Heading = findViewById(R.id.heading);
        totalamount = findViewById(R.id.totalamount);
        submit = findViewById(R.id.submit);
        type = findViewById(R.id.type);
        amountHeaderRow = findViewById(R.id.amountHeaderRow);
        allAmountsContainer = findViewById(R.id.allAmountsContainer);
        scrollForPlayed = findViewById(R.id.scrollForPlayed);
        hidePlayedSection();

        number.addTextChangedListener(simpleWatcher(this::onNumberChanged));
        amount.addTextChangedListener(simpleWatcher(this::onAmountChanged));
    }
    private void setupTypeSpinner(boolean isMarketOpen) {
        ArrayList<String> types = new ArrayList<>();

        if (isMarketOpen) types.add("Close");
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
    private void onNumberChanged(String input) {
        numbers.clear();
        allAmountsContainer.removeAllViews();

        if (TextUtils.isEmpty(input)) {
            hidePlayedSection();
            totalamount.setText("");
            return;
        }

        switch (game) {
            case "Crossing":
                numbers = GameData.generateCrossingNumbers(input);
                break;
            case "SP Motor":
                numbers = GameData.generateSPNumbers(input);
                break;
            case "DP Motor":
                numbers = GameData.generateDPNumbers(input);
                break;
        }
        showPlayedBets();
        calculateTotal();
    }

    private void onAmountChanged(String amt) {
        showPlayedBets();
        calculateTotal();
    }

    private void showPlayedBets() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) scrollForPlayed.getLayoutParams(); params.height = 0; params.weight = 1f;
        PlayedBetRenderer.renderFixedAmount(
                this,
                numbers,
                amount.getText().toString(),
                amountHeaderRow,
                allAmountsContainer,
                scrollForPlayed
        );
    }

    private void hidePlayedSection() {
        amountHeaderRow.setVisibility(View.GONE);
        allAmountsContainer.setVisibility(View.GONE);
        scrollForPlayed.setVisibility(View.GONE);
    }

    private void calculateTotal() {
        if (TextUtils.isEmpty(amount.getText())) {
            totalamount.setText("");
            return;
        }

        try {
            int amt = Integer.parseInt(amount.getText().toString());
            totalamount.setText("Total : "+ amt * numbers.size());
        } catch (NumberFormatException e) {
            totalamount.setText("");
        }
    }


    private void handleSubmit() {
        if (numbers.isEmpty()) {
            showAlert("Enter numbers");
            return;
        }

        if (TextUtils.isEmpty(amount.getText()) || amount.getText().toString().equals("0")) {
            amount.setError("Enter amount");
            return;
        }
        int amt = Integer.parseInt(amount.getText().toString());
        if (amt  < constant.min_total || amt  > constant.max_total) {
            amount.setError("Amount bet must be between " + constant.min_total + " and " + constant.max_total);
            return;
        }
        prepareCrossingBets();
    }

    private void prepareCrossingBets() {
        progressDialog = new ViewDialog(this);
        progressDialog.showDialog();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String mobile = prefs.getString("mobile", "");

        ArrayList<BetEngine.BetItem> bets = new ArrayList<>();
        int amt = Integer.parseInt(amount.getText().toString());

        for (String n : numbers) {
            bets.add(new BetEngine.BetItem(n, amt));
        }

        BetEngine.placeMultipleBets(
                db,
                mobile,
                market,
                game,
                selectedGameType,
                bets,
                new BetEngine.BetCallback() {
                    @Override
                    public void onSuccess(int newWallet) {
                        prefs.edit().putString("wallet", String.valueOf(newWallet)).apply();
                        progressDialog.hideDialog();
                        CommonUtils.soundPlayAndVibrate(crossing.this,
                                (Vibrator) getSystemService(VIBRATOR_SERVICE));
                        goThankYou();
                    }

                    @Override
                    public void onFailure(String error) {
                        progressDialog.hideDialog();
                        showAlert(error);
                    }
                }
        );
    }


    private TextWatcher simpleWatcher(TextChangeListener listener) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                listener.onChange(s.toString());
            }
        };
    }

    interface TextChangeListener {
        void onChange(String value);
    }

    private void showAlert(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void goThankYou() {
        startActivity(new Intent(this, thankyou.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}
