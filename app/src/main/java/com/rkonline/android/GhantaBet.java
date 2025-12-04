/*
package com.rkonline.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class GhantaBet extends AppCompatActivity {

    TextView bet_tpe;
    EditText edamount,ednumber;
    Button place_bet;
    String BET_URL = "https://rkonlinematka.in/api/bet_insert_ghanta.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghanta_bet);
        bet_tpe = (TextView)findViewById(R.id.bet_typee);
        edamount =(EditText) findViewById(R.id.bet_amount);
        ednumber =(EditText) findViewById(R.id.bet_number);
        place_bet = (Button) findViewById(R.id.place_bet);

        String btype = getIntent().getStringExtra("cat");

        bet_tpe.setText(btype);


        place_bet.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(GhantaBet.this);

                StringRequest insertRequest = new StringRequest(Request.Method.POST, BET_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Handle the response from the server
                                Toast.makeText(GhantaBet.this,response,Toast.LENGTH_LONG).show();

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Handle errors
                            }
                        }
                )
                {
                    @Override
                    protected Map<String, String> getParams() {
                        // Add the data you want to send to the server as key-value pairs in a HashMap
                        Map<String, String> params = new HashMap<>();
                        params.put("playerid", edamount.getText().toString());
                        params.put("bet_amount", edamount.getText().toString());
                        params.put("bet_number", ednumber.getText().toString());
                        return params;
                    }
                };
                queue.add(insertRequest);
            }

        });

    }

}*/
