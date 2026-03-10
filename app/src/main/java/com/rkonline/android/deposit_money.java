package com.rkonline.android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.rkonline.android.payment.PaymentWebViewActivity;
import com.rkonline.android.utils.CommonUtils;

public class deposit_money extends AppCompatActivity {

    EditText amountInput;
    String userMobile;
    Button twothousand,five100,thousand,call,whatsapp;

    TextView wallet,number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_money);


        amountInput = findViewById(R.id.amount);
        userMobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("mobile", null);


        number = findViewById(R.id.mobile);
        wallet = findViewById(R.id.walletBalance);


        five100 = findViewById(R.id.five100);
        thousand = findViewById(R.id.thousand);
        twothousand = findViewById(R.id.twothousand);
        call = findViewById(R.id.call);
        whatsapp = findViewById(R.id.whatsapp);


        String walletBalance = getIntent().getStringExtra("wallet");
        number.setText(userMobile);
        wallet.setText("₹ " + walletBalance);
        call.setOnClickListener(v -> {
            CommonUtils.openTelegram(this);
        });

        whatsapp.setOnClickListener(v -> {
            CommonUtils.openWhatsApp(this);
        });

        five100.setOnClickListener(v -> {
            amountInput.setError(null);
            amountInput.setText("500");
        });

        thousand.setOnClickListener(v -> {
            amountInput.setError(null);
            amountInput.setText("1000");
        });

        twothousand.setOnClickListener(v -> {
            amountInput.setError(null);
            amountInput.setText("2000");
        });

        findViewById(R.id.back).setOnClickListener(v -> finish());
        findViewById(R.id.pay).setOnClickListener(v -> startUPIPayment());
    }

    private void startUPIPayment() {
        String amount = amountInput.getText().toString().trim();
        if (TextUtils.isEmpty(amount) || amount.equals("0")) {
            amountInput.setError("Enter valid amount");
            return;
        }
        else if(Integer.parseInt(amount)<500){
            amountInput.setError("Amount Should be Greater than 500");
            return;
        }


        Intent intent = new Intent(this, PaymentWebViewActivity.class);
        intent.putExtra("mobile", userMobile);
        intent.putExtra("amount", amount);
        startActivity(intent);
        amountInput.setText("");
    }
}