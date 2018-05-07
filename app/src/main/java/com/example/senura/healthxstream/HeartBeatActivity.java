package com.example.senura.healthxstream;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

public class HeartBeatActivity extends AppCompatActivity {



    Physicaloid mPhysicaloid; // initialising library
    Button button_Open=null;
    Button button_Close=null;
    TextView textView_ReadAmount=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_beat);



        mPhysicaloid = new Physicaloid(HeartBeatActivity.this);
        loadRes();
    }

    private void loadRes(){
        button_Open = (Button) findViewById(R.id.button_HB_Scan);
        button_Close = (Button) findViewById(R.id.button_HB_Stop);
        textView_ReadAmount = (TextView) findViewById(R.id.textView_HB_BPM);

        button_Open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickOpen();
            }
        });

        button_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickClose();
            }
        });
    }



    public void onClickOpen() {

        try {
            mPhysicaloid.setBaudrate(300);

            if (mPhysicaloid.open()) {

                Toast.makeText(this, "Opened..!", Toast.LENGTH_SHORT).show();

                mPhysicaloid.addReadListener(new ReadLisener() {
                    @Override
                    public void onRead(int size) {
                        byte[] buf = new byte[size];
                        mPhysicaloid.read(buf, size);
                        printThis(new String(buf));

                    }
                });
            } else {
                Toast.makeText(this, "Cannot open", Toast.LENGTH_SHORT).show();
            }
        }
        catch(Exception ex){
            Toast.makeText(this, "exception occurred - "+ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickClose() {
        if(mPhysicaloid.close()) {
            mPhysicaloid.clearReadListener();
            Toast.makeText(this, "Closed Successfully..!", Toast.LENGTH_SHORT).show();
        }
    }

    private void printThis(String text){
        Toast.makeText(this, "settext called", Toast.LENGTH_LONG).show();
        textView_ReadAmount.setText(text+" BPM");
    }


}
