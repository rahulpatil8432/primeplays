package com.rkonline.android.payment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.rkonline.android.R;

import java.net.URLEncoder;

public class PaymentWebViewActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_webview);

        webView = findViewById(R.id.webview);
        findViewById(R.id.back).setOnClickListener(v -> finish());

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("app_config").document("deposit")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        try {
                            String domain = document.getString("gateway_domain");

                            String amount = getIntent().getStringExtra("amount");
                            String mobile = getIntent().getStringExtra("mobile");
                            String orderId = "TXN" +System.currentTimeMillis() + (int)(Math.random()*1000);
                            String postData =
                                    "mobile=" + URLEncoder.encode(mobile, "UTF-8") +
                                            "&amount=" + URLEncoder.encode(amount, "UTF-8")+
                                            "&order_id=" + URLEncoder.encode(orderId, "UTF-8");
                            webView.postUrl(
                                    domain+"/demo.php",
                                    postData.getBytes()
                            );

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }});

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if(url.startsWith("myapp://payment-success")){
                    Intent intent = new Intent(PaymentWebViewActivity.this,
                            PaymentResultActivity.class);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }

        });
    }
}