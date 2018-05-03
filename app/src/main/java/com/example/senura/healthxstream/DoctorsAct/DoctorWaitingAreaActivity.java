package com.example.senura.healthxstream.DoctorsAct;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.senura.healthxstream.BodyTemperatureActivity;
import com.example.senura.healthxstream.MainActivity;
import com.example.senura.healthxstream.R;

public class DoctorWaitingAreaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_waiting_area);

        setButtons();

    }


    private void hideSearchTextArea(){
        LinearLayout lL_Search;
        lL_Search = (LinearLayout) findViewById(R.id.linearLayout_DWA_SearchingTextArea);
        lL_Search.setVisibility(View.GONE);
    }

    private void setButtons(){
        Button button_Search;

        //BodyTemp
        button_Search = (Button) findViewById(R.id.button_DWA_Search);
        button_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //sendPayload
                hideSearchTextArea();
            }
        });

    }




}
