package com.rkonline.android.payment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rkonline.android.MainActivity;
import com.rkonline.android.R;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaymentResultActivity extends AppCompatActivity {
    FirebaseFirestore db;
    ProgressDialog dialog;
    Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);
        db = FirebaseFirestore.getInstance();
       btnDone =findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v ->{
            Intent intent = new Intent(PaymentResultActivity.this, MainActivity.class);
            startActivity(intent);
            finishAffinity();
        });
        dialog = new ProgressDialog(this);
        dialog.setMessage("Verifying Payment...");
        dialog.setCancelable(false);
        dialog.show();
        Uri data = getIntent().getData();
        Log.d("PaymentResultActivity", "data: " + data);
        findViewById(R.id.back).setOnClickListener(v -> finish());

        if(data != null){
            String orderId = data.getQueryParameter("order_id");
            String token = data.getQueryParameter("token");
            verifyPayment(orderId, token);
        }
    }

    private void verifyPayment(String orderId, String token){

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("order_id", orderId)
                .add("user_token", token)
                .build();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("app_config").document("deposit")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String domain = document.getString("gateway_domain");
                        Request request = new Request.Builder()
                                .url(domain+"/api/check-order-status")
                                .post(body)
                                .build();
                        client.newCall(request).enqueue(new Callback() {

                            @Override
                            public void onFailure(Call call, IOException e) {

                                runOnUiThread(() -> {
                                    dialog.dismiss();
                                    Toast.makeText(PaymentResultActivity.this,
                                            "Network Error", Toast.LENGTH_LONG).show();
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                                String res = response.body().string();

                                runOnUiThread(() -> handlePaymentResponse(res));
                            }
                        });

                    }});


    }
    private void handlePaymentResponse(String res){

        dialog.dismiss();
        try {

            JSONObject json = new JSONObject(res);

            if(json.getBoolean("status")){

                JSONObject result = json.getJSONObject("result");

                String paymentStatus = result.getString("txnStatus");
                String amount = result.getString("amount");
                String mobile = result.getString("customer_mobile");
                String orderId = result.getString("orderId");
                TextView orderIdTV = findViewById(R.id.orderId);
                orderIdTV.setText(orderId);
                TextView amountTV = findViewById(R.id.amount);
                amountTV.setText("₹" + amount);

                TextView statusMessage = findViewById(R.id.statusMessage);
                statusMessage.setText(json.getString("message"));

                ImageView statusIcon = findViewById(R.id.statusIcon);
                TextView statusTitle = findViewById(R.id.statusTitle);

                if(paymentStatus.equals("SUCCESS")){
                    statusIcon.setImageResource(R.drawable.ic_success);
                    statusTitle.setText("Payment Successful");
                    Log.d("PaymentResultActivity", "amount: " + amount);
                    Log.d("PaymentResultActivity", "mobile:"+mobile);
                    onPaymentSuccess(amount,mobile,paymentStatus);
                    Toast.makeText(this, "Payment Success"+amount, Toast.LENGTH_LONG).show();

                }else{
                    onPaymentSuccess(amount,mobile,paymentStatus);

                    statusIcon.setImageResource(R.drawable.ic_failed);
                    statusTitle.setText("Payment Failed");
                    Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void onPaymentSuccess(String amount, String userMobile, String paymentStatus) {

        int depositAmount = Integer.parseInt(amount);

        long ts = System.currentTimeMillis();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

        db.runTransaction(transaction -> {

            DocumentReference userRef = db.collection("users").document(userMobile);
            DocumentSnapshot userSnap = transaction.get(userRef);

            int wallet = Objects.requireNonNull(userSnap.getLong("wallet")).intValue();

            int newWallet = wallet + depositAmount;

            Map<String, Object> depositEntry = new HashMap<>();
            depositEntry.put("mobile", userMobile);
            depositEntry.put("amount", amount);
            depositEntry.put("remark", paymentStatus.equals("SUCCESS") ?
                    "Payment Deposit Successful" : "Payment Deposit Failed");

            depositEntry.put("type", paymentStatus.equals("SUCCESS") ?
                    "CREDIT" : "DEPOSIT");

            depositEntry.put("status", paymentStatus);

            depositEntry.put("balance", paymentStatus.equals("SUCCESS") ?
                    String.valueOf(newWallet) : String.valueOf(wallet));

            depositEntry.put("date", date);
            depositEntry.put("time", time);
            depositEntry.put("timestamp", ts);

            DocumentReference txnRef = db.collection("transactions").document();
            transaction.set(txnRef, depositEntry);

            if (paymentStatus.equals("SUCCESS")) {
                transaction.update(userRef, "wallet", newWallet+"");
            }

            return null;

        }).addOnSuccessListener(unused -> {

            if (paymentStatus.equals("SUCCESS")) {
                Toast.makeText(this,
                        "₹" + amount + " Added Successfully to Wallet!",
                        Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(this,
                        "Contact Admin in case of payment deduct",
                        Toast.LENGTH_SHORT).show();

                TextView statusMessage = findViewById(R.id.statusMessage);
                statusMessage.setText("Contact Admin in case of payment deduct");
            }

        }).addOnFailureListener(e -> {

            Toast.makeText(this,
                    "Failed to update Wallet Balance. Contact Admin",
                    Toast.LENGTH_SHORT).show();

        });
    }
}