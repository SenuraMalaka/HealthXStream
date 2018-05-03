package com.example.senura.healthxstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class DoctorContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_contact);



        Intent intent = getIntent();
        String did = intent.getStringExtra("did");
        String docName = intent.getStringExtra("docName");

        Toast.makeText(DoctorContactActivity.this,"did is ="+did+" \ndocName ="+docName, Toast.LENGTH_SHORT).show();

    }



}
