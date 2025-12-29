package com.rkonline.android;

import static com.rkonline.android.utils.CommonUtils.soundPlayAndVibrate;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.rkonline.android.utils.AlertHelper;
import com.rkonline.android.utils.BetEngine;
import com.rkonline.android.utils.CommonUtils;
import com.rkonline.android.utils.GameData;

import java.util.ArrayList;

public class fullsangam extends AppCompatActivity {

    protected RelativeLayout toolbar;
    protected latonormal firstitle;
    protected Spinner first;
    protected latonormal secondtitle;
    protected Spinner second;
    protected EditText totalamount;
    protected latobold submit;
    protected ScrollView scrollView;

    ArrayList<String> patti = new ArrayList<>();

    String market, game, openTime, closeTime;
    boolean closeNextDay;

    SharedPreferences prefs;

    ViewDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullsangam);
        initView();

        prefs = getSharedPreferences(constant.prefs, MODE_PRIVATE);
        findViewById(R.id.back).setOnClickListener(v -> finish());

        game = getIntent().getStringExtra("game");
        market = getIntent().getStringExtra("market");
        openTime = getIntent().getStringExtra("openTime");
        closeTime = getIntent().getStringExtra("closeTime");
        closeNextDay = getIntent().getBooleanExtra("closeNextDay", false);

        patti.addAll(getpatti());

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(fullsangam.this, R.layout.simple_list_item_1, patti);
        first.setAdapter(arrayAdapter2);

        ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<>(fullsangam.this, R.layout.simple_list_item_1, patti);
        second.setAdapter(arrayAdapter3);

        submit.setOnClickListener(v -> {

            if (!CommonUtils.canPlaceSangamBet(this, openTime, closeTime, "Full Sangam", closeNextDay)) {
                return;
            }
            // Validate picks
            String firstSel = first.getSelectedItem().toString();
            String secondSel = second.getSelectedItem().toString();

            if (firstSel.contains("Line") || secondSel.contains("Line")) {
                Toast.makeText(fullsangam.this, "Please select a valid number", Toast.LENGTH_SHORT).show();
                return;
            }

            String amountStr = totalamount.getText() == null ? "" : totalamount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(fullsangam.this, "Enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            int amount;
            try {
                amount = Integer.parseInt(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(fullsangam.this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            int wallet = Integer.parseInt(prefs.getString("wallet", "0"));

            // If user has enough wallet, proceed. Otherwise show Recharge prompt
            if (amount <= wallet) {
                saveFullSangamToFirestore(firstSel, secondSel, amountStr);
            } else {
                new AlertDialog.Builder(fullsangam.this)
                        .setMessage("You don't have enough wallet balance to place this bet, Recharge your wallet to play")
                        .setPositiveButton("Recharge", (dialog, id) -> {
                            Intent intent = new Intent(fullsangam.this, deposit_money.class);
                            startActivity(intent);
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                        .show();
            }
        });
    }

    private void saveFullSangamToFirestore(String firstSel, String secondSel, String amountStr) {

        int amount = Integer.parseInt(amountStr);

        if (amount < constant.min_single || amount > constant.max_single) {
            AlertHelper.showCustomAlert(this, "Info!" , "Bet amount must be between 10 and 10000", R.drawable.info_icon,0);

            return;
        }

        progressDialog = new ViewDialog(fullsangam.this);
        progressDialog.showDialog();
        submit.setEnabled(false);

        String betNumber = firstSel + " - " + secondSel;

        BetEngine.placeBet(
                FirebaseFirestore.getInstance(),
                prefs.getString("mobile", ""),
                market,
                game,
                betNumber,
                amount,
                "Full Sangam Bet - " + market,
                null,
                new BetEngine.BetCallback() {
                    @Override
                    public void onSuccess(int newWallet) {
                        prefs.edit().putString("wallet", String.valueOf(newWallet)).apply();
                        progressDialog.hideDialog();
                        soundPlayAndVibrate(fullsangam.this,
                                (Vibrator) getSystemService(VIBRATOR_SERVICE));
                        goThankYou();
                    }

                    @Override
                    public void onFailure(String error) {
                        submit.setEnabled(true);
                        progressDialog.hideDialog();
                        AlertHelper.showCustomAlert(fullsangam.this, "Sorry!" , "Something went wrong", 0,0);
                    }
                }
        );
    }

    private void goThankYou() {
        startActivity(new Intent(this, thankyou.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
    public ArrayList<String> getpatti() {
        return GameData.getPattiWithLines();
    }

    private void initView() {
        firstitle = (latonormal) findViewById(R.id.firstitle);
        first = (Spinner) findViewById(R.id.first);
        secondtitle = (latonormal) findViewById(R.id.secondtitle);
        second = (Spinner) findViewById(R.id.second);
        totalamount = (EditText) findViewById(R.id.totalamount);
        submit = (latobold) findViewById(R.id.submit);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
    }
}
