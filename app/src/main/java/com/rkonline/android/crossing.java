package com.rkonline.android;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.rkonline.android.utils.AlertHelper;
import com.rkonline.android.utils.BetEngine;
import com.rkonline.android.utils.CommonUtils;
import com.rkonline.android.utils.PlayedBetRenderer;

import java.util.ArrayList;
public class crossing extends AppCompatActivity {

    private EditText number, amount;
    private TextView totalamount;
    private Button submit;

    private LinearLayout allAmountsContainer, amountHeaderRow;
    private ScrollView scrollForPlayed;

    private SharedPreferences prefs;

    private final ArrayList<String> numbers = new ArrayList<>();
    private String market, game;
    private ViewDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crossing);
        initView();

        findViewById(R.id.back).setOnClickListener(v -> finish());

        prefs = getSharedPreferences(constant.prefs, MODE_PRIVATE);
        game = getIntent().getStringExtra("game");
        market = getIntent().getStringExtra("market");

        submit.setOnClickListener(v -> handleSubmit());
    }


    private void initView() {
        number = findViewById(R.id.number);
        amount = findViewById(R.id.amount);
        totalamount = findViewById(R.id.totalamount);
        submit = findViewById(R.id.submit);

        amountHeaderRow = findViewById(R.id.amountHeaderRow);
        allAmountsContainer = findViewById(R.id.allAmountsContainer);
        scrollForPlayed = findViewById(R.id.scrollForPlayed);

        hidePlayedSection();

        number.addTextChangedListener(simpleWatcher(this::onNumberChanged));
        amount.addTextChangedListener(simpleWatcher(this::onAmountChanged));
    }

    private void onNumberChanged(String input) {
        numbers.clear();
        allAmountsContainer.removeAllViews();

        if (TextUtils.isEmpty(input)) {
            hidePlayedSection();
            totalamount.setText("");
            return;
        }

        generateCrossingNumbers(input);
        showPlayedBets();
        calculateTotal();
    }

    private void onAmountChanged(String amt) {
        showPlayedBets();
        calculateTotal();
    }


    private void generateCrossingNumbers(String input) {
        ArrayList<Character> unique = new ArrayList<>();
        for (char c : input.toCharArray()) {
            if (!unique.contains(c)) {
                unique.add(c);
            }
        }

        for (char a : unique) {
            for (char b : unique) {
                numbers.add("" + a + b);
            }
        }
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
        int amt = Integer.parseInt(amount.getText().toString());
        if (amt * numbers.size() < constant.min_total || amt * numbers.size() > constant.max_total) {
            AlertHelper.showCustomAlert(this, "Info!",
                    "Total bet must be between " + constant.min_total + " and " + constant.max_total,
                    R.drawable.info_icon, 0);
        }
        if (TextUtils.isEmpty(amount.getText()) || amount.getText().toString().equals("0")) {
            amount.setError("Enter amount");
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
                null,
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
