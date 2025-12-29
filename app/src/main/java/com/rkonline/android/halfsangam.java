package com.rkonline.android;

import static com.rkonline.android.utils.CommonUtils.canPlaceSangamBet;
import static com.rkonline.android.utils.CommonUtils.soundPlayAndVibrate;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.rkonline.android.utils.BetEngine;
import com.rkonline.android.utils.GameData;

import java.util.ArrayList;

public class halfsangam extends AppCompatActivity {

    protected RelativeLayout toolbar;
    protected Spinner type;
    protected latobold submit;
    protected ScrollView scrollView;
    protected Spinner first;
    protected Spinner second;
    protected latonormal firstitle;
    protected latonormal secondtitle;
    protected EditText totalamount;

    ArrayList<String> typeof = new ArrayList<>();
    ArrayList<String> ank = new ArrayList<>();
    ArrayList<String> patti = new ArrayList<>();

    String market, game, openTime, closeTime;
    boolean closeNextDay;

    SharedPreferences prefs;

    ViewDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_halfsangam);
        initView();
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        prefs = getSharedPreferences(constant.prefs, MODE_PRIVATE);

        game = getIntent().getStringExtra("game");
        market = getIntent().getStringExtra("market");
        openTime = getIntent().getStringExtra("openTime");
        closeTime = getIntent().getStringExtra("closeTime");
        closeNextDay = getIntent().getBooleanExtra("closeNextDay", false);

        ank.add("0");
        ank.add("1");
        ank.add("2");
        ank.add("3");
        ank.add("4");
        ank.add("5");
        ank.add("6");
        ank.add("7");
        ank.add("8");
        ank.add("9");

        patti.addAll(getpatti());

        if (canPlaceSangamBet(this, openTime, closeTime, "Half Sangam", closeNextDay)) {
            typeof.add("Open Ank Close Patti");
            typeof.add("Open Patti Close Ank");
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(halfsangam.this, R.layout.simple_list_item_1, typeof);
            type.setAdapter(arrayAdapter);
        } else {
            submit.setEnabled(false);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(halfsangam.this, R.layout.simple_list_item_1, typeof);
        type.setAdapter(arrayAdapter);



        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(halfsangam.this, R.layout.simple_list_item_1, ank);
        first.setAdapter(arrayAdapter2);

        ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<String>(halfsangam.this, R.layout.simple_list_item_1, patti);
        second.setAdapter(arrayAdapter3);

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(halfsangam.this, R.layout.simple_list_item_1, ank);
                    first.setAdapter(arrayAdapter2);

                    ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<String>(halfsangam.this, R.layout.simple_list_item_1, patti);
                    second.setAdapter(arrayAdapter3);

                    firstitle.setText("Ank");
                    secondtitle.setText("Patti");
                } else {
                    ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(halfsangam.this, R.layout.simple_list_item_1, ank);
                    second.setAdapter(arrayAdapter2);

                    ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<String>(halfsangam.this, R.layout.simple_list_item_1, patti);
                    first.setAdapter(arrayAdapter3);

                    firstitle.setText("Patti");
                    secondtitle.setText("Ank");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        submit.setOnClickListener(v -> {
            if (!canPlaceSangamBet(
                    halfsangam.this,
                    openTime,
                    closeTime,
                    "Half Sangam",
                    closeNextDay
            )) {
                return;
            }
            if (first.getSelectedItem().toString().contains("Line") || second.getSelectedItem().toString().contains("Line"))
            {
                Toast.makeText(halfsangam.this, "Please select A valid number", Toast.LENGTH_SHORT).show();
            }
            else if (Integer.parseInt(totalamount.getText().toString()) < Integer.parseInt(prefs.getString("wallet","0"))) {

                apicall();
            }
            else
            {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(halfsangam.this);
                builder1.setMessage("You don't have enough wallet balance to place this bet, Recharge your wallet to play");
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "Recharge",
                        (dialog, id) -> {
                            startActivity(new Intent(halfsangam.this, deposit_money.class));
                            dialog.dismiss();
                        });

                builder1.setNegativeButton(
                        "Cancel",
                        (dialog, id) -> dialog.cancel());

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
    }


    private void apicall() {

        int amount = Integer.parseInt(totalamount.getText().toString());

        if (amount < constant.min_single || amount > constant.max_single) {
            Toast.makeText(this, "Bet amount must be between 10 and 10000", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ViewDialog(halfsangam.this);
        progressDialog.showDialog();
        submit.setEnabled(false);

        String selectedFirst = first.getSelectedItem().toString();
        String selectedSecond = second.getSelectedItem().toString();
        String betNumber = selectedFirst + " - " + selectedSecond;

        BetEngine.placeBet(
                FirebaseFirestore.getInstance(),
                prefs.getString("mobile", ""),
                market,
                game,
                betNumber,
                amount,
                "Half Sangam Bet - " + market,
                null,
                new BetEngine.BetCallback() {
                    @Override
                    public void onSuccess(int newWallet) {
                        prefs.edit().putString("wallet", String.valueOf(newWallet)).apply();
                        progressDialog.hideDialog();
                        soundPlayAndVibrate(halfsangam.this,
                                (Vibrator) getSystemService(VIBRATOR_SERVICE));
                        goThankYou();
                    }

                    @Override
                    public void onFailure(String error) {
                        submit.setEnabled(true);
                        progressDialog.hideDialog();
                        Toast.makeText(halfsangam.this, error, Toast.LENGTH_SHORT).show();
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
        type = (Spinner) findViewById(R.id.type);
        submit = (latobold) findViewById(R.id.submit);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        first = (Spinner) findViewById(R.id.first);
        second = (Spinner) findViewById(R.id.second);
        firstitle = (latonormal) findViewById(R.id.firstitle);
        secondtitle = (latonormal) findViewById(R.id.secondtitle);
        totalamount = (EditText) findViewById(R.id.totalamount);
    }
}
