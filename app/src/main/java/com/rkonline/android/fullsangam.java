package com.rkonline.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    String market, game;

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

        patti.addAll(getpatti());

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(fullsangam.this, R.layout.simple_list_item_1, patti);
        first.setAdapter(arrayAdapter2);

        ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<>(fullsangam.this, R.layout.simple_list_item_1, patti);
        second.setAdapter(arrayAdapter3);

        submit.setOnClickListener(v -> {
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
            Toast.makeText(this, "Bet amount must be between 10 and 10000", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ViewDialog(fullsangam.this);
        progressDialog.showDialog();
        submit.setEnabled(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String mobile = prefs.getString("mobile", null);
        int wallet = Integer.parseInt(prefs.getString("wallet", "0"));
        int newWallet = wallet - amount;

        String betNumber = firstSel + " - " + secondSel;

        long timestamp = System.currentTimeMillis();
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

        db.runBatch(batch -> {

            // 1️⃣ Played entry
            Map<String, Object> betData = new HashMap<>();
            betData.put("mobile", mobile);
            betData.put("market", market);
            betData.put("game", game);
            betData.put("bet", betNumber);
            betData.put("amount", String.valueOf(amount));
            betData.put("date", date);
            betData.put("time", time);
            betData.put("timestamp", timestamp);

            batch.set(
                    db.collection("played").document(),
                    betData
            );

            // 2️⃣ Wallet transaction
            Map<String, Object> txn = new HashMap<>();
            txn.put("mobile", mobile);
            txn.put("amount", String.valueOf(amount));
            txn.put("type", "DEBIT");
            txn.put("remark", "Full Sangam Bet - " + market);
            txn.put("timestamp", timestamp);
            txn.put("date", date);

            batch.set(
                    db.collection("transactions").document(),
                    txn
            );

            // 3️⃣ Wallet update
            batch.update(
                    db.collection("users").document(mobile),
                    "wallet", newWallet
            );

        }).addOnSuccessListener(unused -> {

            prefs.edit().putString("wallet", String.valueOf(newWallet)).apply();

            progressDialog.hideDialog();
            Toast.makeText(fullsangam.this, "Bet placed successfully 🎉", Toast.LENGTH_SHORT).show();

            Intent in = new Intent(getApplicationContext(), thankyou.class);
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);
            finish();

        }).addOnFailureListener(e -> {

            submit.setEnabled(true);
            progressDialog.hideDialog();
            Toast.makeText(fullsangam.this, "Bet failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    public ArrayList<String> getpatti() {

        ArrayList<String> number = new ArrayList<>();

        // 1
        number.add("Line of 1");
        number.add("100");
        number.add("119");
        number.add("155");
        number.add("227");
        number.add("335");
        number.add("344");
        number.add("399");
        number.add("588");
        number.add("669");
        number.add("128");
        number.add("137");
        number.add("146");
        number.add("236");
        number.add("245");
        number.add("290");
        number.add("380");
        number.add("470");
        number.add("489");
        number.add("560");
        number.add("678");
        number.add("579");

        //2
        number.add("Line of 2");
        number.add("200");
        number.add("110");
        number.add("228");
        number.add("255");
        number.add("336");
        number.add("499");
        number.add("660");
        number.add("688");
        number.add("778");
        number.add("129");
        number.add("138");
        number.add("147");
        number.add("156");
        number.add("237");
        number.add("246");
        number.add("345");
        number.add("390");
        number.add("480");
        number.add("570");
        number.add("679");
        number.add("589");

        // 3
        number.add("Line of 3");
        number.add("300");
        number.add("166");
        number.add("229");
        number.add("337");
        number.add("355");
        number.add("445");
        number.add("599");
        number.add("779");
        number.add("788");
        number.add("120");
        number.add("139");
        number.add("148");
        number.add("157");
        number.add("238");
        number.add("247");
        number.add("256");
        number.add("346");
        number.add("490");
        number.add("580");
        number.add("670");
        number.add("689");

        // 4
        number.add("Line of 4");
        number.add("400");
        number.add("112");
        number.add("220");
        number.add("266");
        number.add("338");
        number.add("446");
        number.add("455");
        number.add("699");
        number.add("770");
        number.add("130");
        number.add("149");
        number.add("158");
        number.add("167");
        number.add("239");
        number.add("248");
        number.add("257");
        number.add("347");
        number.add("356");
        number.add("590");
        number.add("680");
        number.add("789");

        // 5
        number.add("Line of 5");
        number.add("500");
        number.add("113");
        number.add("122");
        number.add("177");
        number.add("339");
        number.add("366");
        number.add("447");
        number.add("799");
        number.add("889");
        number.add("140");
        number.add("159");
        number.add("168");
        number.add("230");
        number.add("249");
        number.add("258");
        number.add("267");
        number.add("348");
        number.add("357");
        number.add("456");
        number.add("690");
        number.add("780");

        // 6
        number.add("Line of 6");
        number.add("600");
        number.add("114");
        number.add("277");
        number.add("330");
        number.add("448");
        number.add("466");
        number.add("556");
        number.add("880");
        number.add("899");
        number.add("123");
        number.add("150");
        number.add("169");
        number.add("178");
        number.add("240");
        number.add("259");
        number.add("268");
        number.add("349");
        number.add("358");
        number.add("457");
        number.add("367");
        number.add("790");

        // 7
        number.add("Line of 7");
        number.add("700");
        number.add("115");
        number.add("133");
        number.add("188");
        number.add("223");
        number.add("377");
        number.add("449");
        number.add("557");
        number.add("566");
        number.add("124");
        number.add("160");
        number.add("179");
        number.add("250");
        number.add("269");
        number.add("278");
        number.add("340");
        number.add("359");
        number.add("368");
        number.add("458");
        number.add("467");
        number.add("890");

        // 8
        number.add("Line of 8");
        number.add("800");
        number.add("116");
        number.add("224");
        number.add("233");
        number.add("288");
        number.add("440");
        number.add("477");
        number.add("558");
        number.add("990");
        number.add("125");
        number.add("134");
        number.add("170");
        number.add("189");
        number.add("260");
        number.add("279");
        number.add("350");
        number.add("369");
        number.add("378");
        number.add("459");
        number.add("567");
        number.add("468");

        // 9
        number.add("Line of 9");
        number.add("900");
        number.add("117");
        number.add("144");
        number.add("199");
        number.add("225");
        number.add("388");
        number.add("559");
        number.add("577");
        number.add("667");
        number.add("126");
        number.add("135");
        number.add("180");
        number.add("234");
        number.add("270");
        number.add("289");
        number.add("360");
        number.add("379");
        number.add("450");
        number.add("469");
        number.add("478");
        number.add("568");

        // 0
        number.add("Line of 0");
        number.add("550");
        number.add("668");
        number.add("244");
        number.add("299");
        number.add("226");
        number.add("488");
        number.add("677");
        number.add("118");
        number.add("334");
        number.add("127");
        number.add("136");
        number.add("145");
        number.add("190");
        number.add("235");
        number.add("280");
        number.add("370");
        number.add("479");
        number.add("460");
        number.add("569");
        number.add("389");
        number.add("578");


        return number;
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
