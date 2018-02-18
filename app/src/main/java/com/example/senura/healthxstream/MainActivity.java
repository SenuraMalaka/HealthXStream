package com.example.senura.healthxstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //email added delgahadeniya.dissanayake@students.plymouth.ac.uk


        Intent myIntent = new Intent(MainActivity.this, MyDoctorsActivity.class);
        myIntent.putExtra("key", "Achala Dissanayake"); //Optional parameters
        MainActivity.this.startActivity(myIntent);

    }


}
