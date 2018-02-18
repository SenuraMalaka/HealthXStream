package com.example.senura.healthxstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //email added delgahadeniya.dissanayake@students.plymouth.ac.uk


       setButtons();





    }




    private void setButtons(){
        ImageButton button_HB;
        ImageButton button_FM;
        ImageButton button_MD;
        ImageButton button_BT;



        //BodyTemp
        button_MD = (ImageButton) findViewById(R.id.imageButton_MM_BT);

        button_MD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BodyTemperatureActivity.class));
            }
        });


        //MyDocs
        button_MD = (ImageButton) findViewById(R.id.imageButton_MM_MD);

        button_MD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(MainActivity.this, MyDoctorsActivity.class);
                myIntent.putExtra("key", "Achala Dissanayake"); //Optional parameters
                MainActivity.this.startActivity(myIntent);

            }
        });




        //FamilyMems
        button_FM = (ImageButton) findViewById(R.id.imageButton_MM_FM);

        button_FM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FamilyMembersActivity.class));

            }
        });


        //hearbeat
        button_HB = (ImageButton) findViewById(R.id.imageButton_MM_HB);

        button_HB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HeartBeatActivity.class));

            }
        });
    }


}
