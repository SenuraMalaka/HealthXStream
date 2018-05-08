package com.example.senura.healthxstream;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class BodyTemperatureActivity extends AppCompatActivity {

    //Res
    Button buttonUSBOpen=null;
    Button buttonUSBClose=null;
    TextView textView_Temp=null;



    //USB Monitor Vars
    public final String ACTION_USB_PERMISSION = "com.example.senura.healthxstream.USB_PERMISSION";
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_temperature);
        setButtonUSBOpen();
        setButtonUSBClose();

        textView_Temp = (TextView) findViewById(R.id.textView_BT_Temp);

        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

    }



    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                printNow(new String(data));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }
    };



    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                    boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    if (granted) {
                        connection = usbManager.openDevice(device);
                        serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                        if (serialPort != null) {
                            if (serialPort.open()) { //Set Serial Connection Parameters.
                                serialPort.setBaudRate(9600);//9600
                                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                                serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                                serialPort.read(mCallback);

                                //printNow("Serial Connection Opened!\n");
                                Toast.makeText(BodyTemperatureActivity.this,"Serial Connection Opened", Toast.LENGTH_LONG).show();


                            } else {
                                Log.d("SERIAL", "PORT NOT OPEN");
                            }
                        } else {
                            Log.d("SERIAL", "PORT IS NULL");
                        }
                    } else {
                        Log.d("SERIAL", "PERM NOT GRANTED");
                    }
                } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                    onClickStart();
                    Toast.makeText(BodyTemperatureActivity.this,"usb attached", Toast.LENGTH_LONG).show();

                } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                    onClickStop();
                    Toast.makeText(BodyTemperatureActivity.this,"usb detached", Toast.LENGTH_LONG).show();


                }
            }catch (Exception ex){
                Toast.makeText(BodyTemperatureActivity.this,"Exception occured +"+ex.getMessage(), Toast.LENGTH_LONG).show();

            }

        }

        ;
    };



    public void onClickStart() {

        try {
            HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
            if (!usbDevices.isEmpty()) {
                boolean keep = true;
                for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                    device = entry.getValue();
                    int deviceVID = device.getVendorId();
                    if (deviceVID == 0x2341)//Arduino Vendor ID
                    {
                        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        usbManager.requestPermission(device, pi);
                        keep = false;
                    } else {
                        connection = null;
                        device = null;
                    }

                    if (!keep)
                        break;
                }
            }
        }catch (Exception ex){
            Toast.makeText(BodyTemperatureActivity.this,"Error onclick +"+ex.getMessage(), Toast.LENGTH_LONG).show();

        }
    }



    public void onClickStop() {
        serialPort.close();
        //printNow("\nSerial Connection Closed! \n");

    }


    private void printNow( String text){
        float tempCount=0;
        try {
             tempCount=Float.parseFloat(text);
        }catch (NumberFormatException ex){
            //cant be converted
        }

        String messageTemp="";

        if(tempCount<30 && tempCount>24){
            messageTemp=String.valueOf(tempCount);
        }

        final String message=messageTemp;

        //        Log.d("SERIALREAD", "sssssseeeeeen "+text);
        //        Log.d("SERIALREAD", "tempCount "+tempCount);
        //        Log.d("SERIALREAD", "message "+message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!message.equals(""))
                textView_Temp.setText(message);
            }
        });

    }





        private void setButtonUSBOpen(){

        buttonUSBOpen = (Button) findViewById(R.id.buttonOpenUSB);

        buttonUSBOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClickStart();

            }
        });
    }


        private void setButtonUSBClose(){

        buttonUSBClose = (Button) findViewById(R.id.buttonCloseUSB);

        buttonUSBClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClickStop();
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialPort.close();
        unregisterReceiver(broadcastReceiver);

    }





}
