package com.example.senura.healthxstream.usbSerialAnalyserPackage;
import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

/**
 * Created by senura on 4/8/18.
 */

public class SerialMonitor {

    Physicaloid mPhysicaloid; // initialising library
    Context context=null;
    //Context

    private SerialMonitor(){

    }

    public SerialMonitor(Context passedContext){

        context=passedContext;
        mPhysicaloid = new Physicaloid(context);
        Toast.makeText(context, "Inside cons serial monitor", Toast.LENGTH_LONG).show();


    }



    public void openUSBConnection(String baudtext){


        //String baudtext = spBaud.getSelectedItem().toString();
        switch (baudtext) {
            case "300 baud":
                mPhysicaloid.setBaudrate(300);
                break;
            case "1200 baud":
                mPhysicaloid.setBaudrate(1200);
                break;
            case "2400 baud":
                mPhysicaloid.setBaudrate(2400);
                break;
            case "4800 baud":
                mPhysicaloid.setBaudrate(4800);
                break;
            case "9600 baud":
                mPhysicaloid.setBaudrate(9600);
                break;
            case "19200 baud":
                mPhysicaloid.setBaudrate(19200);
                break;
            case "38400 baud":
                mPhysicaloid.setBaudrate(38400);
                break;
            case "576600 baud":
                mPhysicaloid.setBaudrate(576600);
                break;
            case "744880 baud":
                mPhysicaloid.setBaudrate(744880);
                break;
            case "115200 baud":
                mPhysicaloid.setBaudrate(115200);
                break;
            case "230400 baud":
                mPhysicaloid.setBaudrate(230400);
                break;
            case "250000 baud":
                mPhysicaloid.setBaudrate(250000);
                break;
            default:
                mPhysicaloid.setBaudrate(9600);
        }

        if(mPhysicaloid.open()) {
            //setEnabledUi(true);

            Toast.makeText(context, "Mphysicaloid opened", Toast.LENGTH_LONG).show();
//            if(cbAutoscroll.isChecked())
//            {
//                tvRead.setMovementMethod(new ScrollingMovementMethod());
//            }

            mPhysicaloid.addReadListener(new ReadLisener() {
                @Override
                public void onRead(int size) {
//                    byte[] buf = new byte[size];
//                    mPhysicaloid.read(buf, size);
//                    Log.d("SerialMonitorClass", new String(buf));
                    //Toast.makeText(context, "value = "+new String(buf), Toast.LENGTH_SHORT).show();
                    //tvAppend(tvRead, Html.fromHtml("<font color=blue>" + new String(buf) + "</font>"));

                    Toast.makeText(context, "value in read ", Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Log.d("SerialMonitorClass", "Cannot open USB Device");
            Toast.makeText(context, "Cannot open", Toast.LENGTH_LONG).show();
        }


    }


    public void closeTheUSBConnection() {//(View v)
        if(mPhysicaloid.close()) {
            mPhysicaloid.clearReadListener();
            Log.d("SerialMonitorClass", "Closed the USB connection");
            Toast.makeText(context, "closed", Toast.LENGTH_LONG).show();
        }
    }



}
