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

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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
    String UPI_ID = "8551071322@ybl";
    String UPI_NAME = "Ketan";

    private static final int UPI_PAYMENT = 2025;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_money);

        amountInput = findViewById(R.id.amount);
//        QrCode = findViewById(R.id.QRCode);
//        webView = findViewById(R.id.webview);

//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.getSettings().setDomStorageEnabled(true);
//        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
//        webView.setWebChromeClient(new WebChromeClient());
//        webView.setWebViewClient(new WebViewClient() {
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                return handleUrl(view, url);
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                return handleUrl(view, request.getUrl().toString());
//            }
//
//            private boolean handleUrl(WebView view, String url) {
//
//                // Convert &amp; → &
//                url = url.replace("&amp;", "&");
//                Log.d("UPI_DEBUG", "URL = " + url);
//                if (url.startsWith("upi:") ||
//                        url.startsWith("phonepe:") ||
//                        url.startsWith("tez:") ||
//                        url.startsWith("paytmmp:")) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setData(Uri.parse(url));
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.setPackage(null);
//                    try {
//                        view.getContext().startActivity(intent);
//                    } catch (Exception e) {
//                        Toast.makeText(view.getContext(), "No UPI app found", Toast.LENGTH_LONG).show();
//                    }
//                    return true; // WebView should NOT load this
//                }
//
//                return false; // Normal URLs allowed
//            }
//        });
//
//        webView.loadUrl("https://slateblue-mole-815921.hostingersite.com/");
//


//        BitmapDrawable bitmapDrawable = (BitmapDrawable)QrCode.getDrawable();
//        Bitmap bitmap = bitmapDrawable.getBitmap();

        db = FirebaseFirestore.getInstance();

        userMobile = getSharedPreferences(constant.prefs, MODE_PRIVATE)
                .getString("mobile", null);

        findViewById(R.id.back).setOnClickListener(v -> finish());

        findViewById(R.id.submit).setOnClickListener(v -> startUPIPayment());
    }

    // 🔵 STEP 1: Start UPI Intent
    private void startUPIPayment() {




        String amount = amountInput.getText().toString().trim();

        if (TextUtils.isEmpty(amount) || amount.equals("0")) {
            amountInput.setError("Enter valid amount");
            return;
        }
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("image/*");
//        Uri bmpUri = saveImage(bitmap,getApplicationContext());
//        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        shareIntent.putExtra(Intent.EXTRA_STREAM,bmpUri);
//        shareIntent.putExtra(Intent.EXTRA_SUBJECT,"New App");
//        shareIntent.setPackage("com.google.android.apps.nbu.paisa.user");
//        shareIntent.setPackage("com.phonepe.app");
//        shareIntent.setPackage("net.one97.paytm");
//        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        startActivity(Intent.createChooser(shareIntent,"Share Content"));
//

        String upiUri = "phonepe://pay?pa=test@upi&pn=Testing&am=1&cu=INR";

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