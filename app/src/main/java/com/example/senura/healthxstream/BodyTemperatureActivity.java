package com.example.senura.healthxstream;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.senura.healthxstream.usbSerialAnalyserPackage.SerialMonitor;

import java.util.HashMap;

public class BodyTemperatureActivity extends AppCompatActivity {

    Button buttonUSBOpen=null;
    Button buttonUSBClose=null;
    SerialMonitor serialMonitor=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_temperature);
        setButtonUSBOpen();
        setButtonUSBClose();

    }








    private void setButtonUSBOpen(){

        serialMonitor = new SerialMonitor(BodyTemperatureActivity.this);

        buttonUSBOpen = (Button) findViewById(R.id.buttonOpenUSB);

        buttonUSBOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                usbConnection(true);

            }
        });
    }



    private void setButtonUSBClose(){

        buttonUSBClose = (Button) findViewById(R.id.buttonCloseUSB);

        buttonUSBClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                usbConnection(false);
            }
        });
    }


    private void usbConnection(Boolean shouldOpen){
        if(shouldOpen)
        {
        serialMonitor.openUSBConnection("9600 baud");
        }
        else{
            if(!serialMonitor.equals(null))
            serialMonitor.closeTheUSBConnection();

        }
    }






}
