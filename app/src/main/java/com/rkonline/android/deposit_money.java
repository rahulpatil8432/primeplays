package com.rkonline.android;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class deposit_money extends AppCompatActivity {

    EditText amountInput;
    ImageView QrCode;
    WebView webView;
    FirebaseFirestore db;

    String userMobile;

    // ⚠ UPI ID MUST BE VALID FORMAT
    String UPI_ID = "";
    String UPI_NAME = "Ketan";

    private static final int UPI_PAYMENT = 2025;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_money);

        amountInput = findViewById(R.id.amount);
        db = FirebaseFirestore.getInstance();
        db.collection("app_config").document("upiDetails").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                 UPI_ID = value.getString("UPI_ID");
            }
        });
        userMobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("mobile", null);

        findViewById(R.id.back).setOnClickListener(v -> finish());

        findViewById(R.id.phonepe).setOnClickListener(v -> startUPIPayment("phonePe"));
        findViewById(R.id.paytm).setOnClickListener(v -> startUPIPayment("paytm"));
        findViewById(R.id.gpay).setOnClickListener(v -> startUPIPayment("gpay"));
    }

    // 🔵 STEP 1: Start UPI Intent
    private void startUPIPayment(String method) {

        String amount = amountInput.getText().toString().trim();

        if (TextUtils.isEmpty(amount) || amount.equals("0")) {
            amountInput.setError("Enter valid amount");
            return;
        }
        String upiUri = "";
        switch (method){
            case "phonePe":
                upiUri = "phonepe://pay?pa="+UPI_ID+"&pn=Testing&am=1&cu=INR";
                break;
            case "paytm":
                upiUri = "paytmmp://pay?pa="+UPI_ID+"&pn=Testing&am=1&cu=INR";
                break;
            case "gpay":
                upiUri = "tez://upi/pay?pa="+UPI_ID+"&pn=Testing&am=1&cu=INR";
                break;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(upiUri));
        Intent chooser = Intent.createChooser(intent, "Pay Using UPI");
        try {
            startActivityForResult(chooser, UPI_PAYMENT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No UPI app found!", Toast.LENGTH_LONG).show();
        }


    }

    private static Uri saveImage (Bitmap bitmap, Context context){
        File imageFolder  = new File(context.getCacheDir(),"images");
        Uri uri = null;
        try{
            imageFolder.mkdirs();
            File file = new File(imageFolder,"sharedQr.jpg");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,90,stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(Objects.requireNonNull(context.getApplicationContext()),"com.rkonline.android.fileprovider",file);
        }catch (IOException ioException)
        {
            Log.d("TAG","Exception"+ ioException.getMessage());
        }
        return uri;
    }
    // 🔵 STEP 2: Handle Payment Response
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT) {

            if (resultCode == Activity.RESULT_OK || resultCode == 11) {

                if (data != null) {

                    String response = data.getStringExtra("response");
                    if (response == null) response = "discard";

                    response = response.toLowerCase();

                    if (response.contains("success")) {
                        onPaymentSuccess();
                    } else if (response.contains("failed")) {
                        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Payment Not Completed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 🔵 STEP 3: Store Deposit in Firestore + Ledger
    private void onPaymentSuccess() {

        String amount = amountInput.getText().toString().trim();

        Map<String, Object> depositEntry = new HashMap<>();
        depositEntry.put("mobile", userMobile);
        depositEntry.put("amount", amount);
        depositEntry.put("remark", "UPI Deposit Successful");
        depositEntry.put("type", "deposit");
        depositEntry.put("timestamp", System.currentTimeMillis());

        // Add transaction
        db.collection("transactions")
                .add(depositEntry)
                .addOnSuccessListener(docRef -> {

                    // Update wallet balance
                    db.collection("users").document(userMobile)
                            .update("wallet", FieldValue.increment(Integer.parseInt(amount)))
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "₹" + amount + " Added Successfully!", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Wallet update failed", Toast.LENGTH_SHORT).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show()
                );
    }
}