package com.rkonline.android;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class notice extends AppCompatActivity {

    protected latonormal text;

    ViewDialog progressDialog;
    FirebaseDatabase database = FirebaseDatabase.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_notice);
        initView();
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        apicall();
    }


    private void apicall() {

        progressDialog = new ViewDialog(notice.this);
        progressDialog.showDialog();
        DatabaseReference myRef = database.getReference("notice");
        myRef.child("message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.hideDialog();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    text.setText(Html.fromHtml(snapshot.getValue(String.class), Html.FROM_HTML_MODE_COMPACT));
                } else {
                    text.setText(Html.fromHtml(snapshot.getValue(String.class)));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.hideDialog();
                Toast.makeText(notice.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initView() {
        text = (latonormal) findViewById(R.id.text);
    }
}