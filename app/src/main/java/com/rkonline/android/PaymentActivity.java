package com.rkonline.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.BarcodeFormat;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PaymentActivity extends AppCompatActivity {

    private ImageView imgQr;
    private TextView txtAmount, txtTimer;
    private Button btnPaytm, btnGpay, btnRefresh;

    private FirebaseFirestore db;
    private DocumentReference txnRef;

    private String txnId;
    private String upiLink;
    private double amount = 1.00;

    private static final long QR_VALIDITY = 5 * 60 * 1000;
    private static final int UPI_REQUEST = 101;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_payment);

        initViews();
        initFirestore();
        createTransaction();
    }

    private void initViews() {
        imgQr = findViewById(R.id.imgQr);
        txtAmount = findViewById(R.id.txtAmount);
        txtTimer = findViewById(R.id.txtTimer);
        btnPaytm = findViewById(R.id.btnPaytm);
        btnGpay = findViewById(R.id.btnGpay);
        btnRefresh = findViewById(R.id.btnRefresh);

        DecimalFormat df = new DecimalFormat("0.00");
        txtAmount.setText("₹" + df.format(amount));

        btnPaytm.setOnClickListener(v -> openUpiApp());
        btnGpay.setOnClickListener(v -> openUpiApp());
        btnRefresh.setOnClickListener(v -> recreate());
    }

    private void initFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void createTransaction() {
        txnId = UUID.randomUUID().toString().replace("-", "");

        Map<String, Object> txn = new HashMap<>();
        txn.put("txnId", txnId);
        txn.put("sellerUpi", "8551071322-3@ybl");
        txn.put("sellerName", "Ketan Kumbhar");
        txn.put("amount", amount);
        txn.put("status", "CREATED");
        txn.put("createdAt", System.currentTimeMillis());
        txn.put("expiresAt", System.currentTimeMillis() + QR_VALIDITY);

        txnRef = db.collection("upi_transactions").document(txnId);

        txnRef.set(txn).addOnSuccessListener(unused -> {
            generateUpiLink();
            generateQr();
            startTimer();
        });
    }

    private void generateUpiLink() {
        upiLink = "upi://pay" +
                "?pa=" + Uri.encode("8551071322-3@ybl") +
                "&pn=" + Uri.encode("Ketan Kumbhar") +
                "&am=" + String.format("%.2f", amount) +
                "&cu=INR" +
                "&tr=" + txnId +
                "&tn=Matka_" + txnId;
    }

    private void generateQr() {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(upiLink,
                    BarcodeFormat.QR_CODE, 600, 600);
            imgQr.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, "QR error", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(QR_VALIDITY, 1000) {
            public void onTick(long ms) {
                txtTimer.setText("Time remaining: " +
                        String.format("%02d:%02d", ms / 60000, (ms / 1000) % 60));
            }
            public void onFinish() { expireTransaction(); }
        }.start();
    }

    private void openUpiApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(upiLink));
        startActivityForResult(intent, UPI_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("in onActivityResult requestCode "+requestCode);
        System.out.println("in onActivityResult resultCode "+resultCode);
        System.out.println("in onActivityResult data "+data);

        if (requestCode == UPI_REQUEST) {

            if (data == null) {
                markPending("No response from UPI app");
                return;
            }

            String response = data.getStringExtra("response");
            System.out.println("in onActivityResult response "+response);

            if (response == null) {
                markPending("Waiting for confirmation");
                return;
            }

            response = response.toLowerCase();

            if (response.contains("success")) {
                txnRef.update("status", "SUCCESS");
                Toast.makeText(this, "Payment Successful", Toast.LENGTH_LONG).show();
            }
            else if (response.contains("failure")) {
                txnRef.update("status", "FAILED");
                Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show();
            }
            else {
                markPending("Payment processing");
            }
        }
    }

    private void markPending(String msg) {
        txnRef.update("status", "PENDING");
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void expireTransaction() {
        if (countDownTimer != null) countDownTimer.cancel();
        txtTimer.setText("QR expired");
        txnRef.update("status", "EXPIRED");
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}