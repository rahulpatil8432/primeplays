package com.rkonline.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.util.ArrayList;
import java.util.Map;

public class rate extends AppCompatActivity {

    ViewDialog progressDialog;
    FirebaseFirestore db;
    RecyclerView recyclerview;
    protected TextView headerTitle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_game);
        initView();
        headerTitle.setText(getIntent().getStringExtra("header"));
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        apicall();
    }


    private void apicall() {

        progressDialog = new ViewDialog(rate.this);
        progressDialog.showDialog();

        db.collection("games").orderBy("order").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.hideDialog();
                if (task.isSuccessful()) {
                    ArrayList<String> name = new ArrayList<>();
                    ArrayList<String> rate = new ArrayList<>();


                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> userMap = document.getData();
                        name.add((String) userMap.get("name"));
                        rate.add((String) userMap.get("rate"));
                    }
                    adapter_game rc;
                    Log.e("header",headerTitle.getText().toString());
                    if(headerTitle.getText().toString().equalsIgnoreCase("Game Rate")){
                       rc = new adapter_game(rate.this,name,rate);
                        recyclerview.setLayoutManager(new LinearLayoutManager(rate.this));

                    }else{
                        rc = new adapter_game(rate.this,name,new ArrayList<>());
                        recyclerview.setLayoutManager(new GridLayoutManager(rate.this,2));

                    }
                    recyclerview.setAdapter(rc);
                    rc.notifyDataSetChanged();
                } else {
                    Toast.makeText(rate.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void initView() {
        db = FirebaseFirestore.getInstance();
        recyclerview = findViewById(R.id.gameRecyclerView);
        headerTitle = findViewById(R.id.header_title);

    }
}