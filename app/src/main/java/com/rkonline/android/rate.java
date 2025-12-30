package com.rkonline.android;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rkonline.android.utils.AlertHelper;

import java.util.ArrayList;
import java.util.Map;

public class rate extends AppCompatActivity {

    ViewDialog progressDialog;
    FirebaseFirestore db;
    RecyclerView recyclerview;
    ListView listView;
    protected TextView headerTitle;
    String market,openTime,closeTime;

    boolean closeNextDay;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_game);
        initView();
        headerTitle.setText(getIntent().getStringExtra("header"));
        market = getIntent().getStringExtra("market");
        openTime = getIntent().getStringExtra("openTime");
        closeTime = getIntent().getStringExtra("closeTime");
        closeNextDay = getIntent().getBooleanExtra("closeNextDay",false);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        apicall();
    }


    private void apicall() {

        progressDialog = new ViewDialog(rate.this);
        progressDialog.showDialog();

        db.collection("games").orderBy("order").get().addOnCompleteListener(task -> {
            progressDialog.hideDialog();
            if (task.isSuccessful()) {
                ArrayList<String> name = new ArrayList<>();
                ArrayList<String> rate = new ArrayList<>();


                for (QueryDocumentSnapshot document : task.getResult()) {

                    Map<String, Object> game = document.getData();

                    String gameName = (String) game.get("name");
                    String gameRate = (String) game.get("rate");
                    Boolean isActive = (Boolean) game.get("isActive");

                    if (isActive == null || !isActive) continue;
                    if (headerTitle.getText().toString().equalsIgnoreCase("Game Rate")) {
                        if (gameRate != null && !gameRate.trim().isEmpty()) {
                            name.add(gameName + " : " + gameRate);
//                            rate.add(gameRate);
                        }
                    } else {
                        name.add(gameName);
                    }
                }
                adapter_game rc;
                Log.e("header",headerTitle.getText().toString());
                if(headerTitle.getText().toString().equalsIgnoreCase("Game Rate")){
                    listView.setVisibility(View.VISIBLE);
                    recyclerview.setVisibility(View.GONE);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_list_item_1,
                            name
                    ) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            // Get default view
                            TextView textView = (TextView) super.getView(position, convertView, parent);

                            // Center text
                            textView.setGravity(Gravity.CENTER);
                            textView.setTypeface(null, Typeface.BOLD);
                            // Add padding
                            int padding = (int) (16 * getResources().getDisplayMetrics().density);
                            textView.setPadding(padding, padding, padding, padding);

                            listView.setDivider(new ColorDrawable(Color.TRANSPARENT)); // invisible divider
                            listView.setDividerHeight((int) (8 * getResources().getDisplayMetrics().density));
                            // Add background with rounded corners
                            GradientDrawable bg = new GradientDrawable();
                            bg.setColor(Color.parseColor("#FFFFFF")); // background color
                            bg.setCornerRadius(16 * getResources().getDisplayMetrics().density); // corner radius in px
                            textView.setBackground(bg);

                            return textView;
                        }
                    };
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

//                   rc = new adapter_game(rate.this,name,rate,market, false, openTime, closeTime, closeNextDay);
//                   recyclerview.setLayoutManager(new LinearLayoutManager(rate.this));
                }else{
                    recyclerview.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);

                    boolean isMarketOpen = getIntent().getBooleanExtra("isMarketOpen",false);
                    if(isMarketOpen){
                        name.remove("Jodi");
                        name.remove("Crossing");
                        name.remove("Red Jodi");
                        name.remove("Half Sangam");
                        name.remove("Full Sangam");
                    }
                    rc = new adapter_game(rate.this,name,new ArrayList<>(), market, isMarketOpen, openTime, closeTime, closeNextDay);
                    recyclerview.setLayoutManager(new GridLayoutManager(rate.this,2));
                    recyclerview.setAdapter(rc);
                    rc.notifyDataSetChanged();
                }

            } else {
                AlertHelper.showCustomAlert(rate.this, "Something went wrong" , "Please try again", 0,0);
            }
        });
    }


    private void initView() {
        db = FirebaseFirestore.getInstance();
        recyclerview = findViewById(R.id.gameRecyclerView);
        headerTitle = findViewById(R.id.header_title);
        listView = findViewById(R.id.listView);

    }
}