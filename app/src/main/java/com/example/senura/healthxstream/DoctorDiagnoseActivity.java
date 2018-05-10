package com.example.senura.healthxstream;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.senura.healthxstream.DoctorsAct.DoctorWaitingAreaActivity;
import com.example.senura.healthxstream.mqttConnectionPackage.JsonAccess;
import com.example.senura.healthxstream.mqttConnectionPackage.MqttConnection;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class DoctorDiagnoseActivity extends AppCompatActivity implements MqttCallback {

    public static MqttConnection mConnectionTemp=null;
    private MqttConnection mConnection=null;
    public static MqttAndroidClient clientTemp = null;
    private MqttAndroidClient client = null;

    private String clientID=null;
    private String did=null;
    private String docName=null;

    private String jsonResponse = null;
    private boolean isFirstTimeMessageBoxUpdates=true;

    //USB Monitor Vars
    public final String ACTION_USB_PERMISSION = "com.example.senura.healthxstream.USB_PERMISSION";
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;

    private String currentReadingDevice="null";


    //View
    TextView textView_MessageBox;
    Button button_Pulse=null;
    Button button_Temp=null;
    Button button_EndSession=null;
    Button button_SendMsg=null;
    EditText editText_MsgTyped=null;
    TextView textView_SensorReadingTitle=null;
    TextView textView_SensorReading=null;
    LinearLayout linearLayout_SensorReadingBox=null;

    private boolean isRetainMqttState=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        setContentView(R.layout.activity_doctor_diagnose);

        Intent intent = getIntent();
        did = intent.getStringExtra("did");
        docName = intent.getStringExtra("docName");
        clientID = intent.getStringExtra("clientID");
        Toast.makeText(DoctorDiagnoseActivity.this,"did is ="+did+" \ndocName ="+docName, Toast.LENGTH_SHORT).show();

        mConnection=mConnectionTemp;
        client=clientTemp;
        setResources();

        setClientListenToThisAct();
        initUSBSerialCon();
    }


    private void initUSBSerialCon(){
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
    }


    private void setClientListenToThisAct(){
        client.setCallback(DoctorDiagnoseActivity.this);
    }



    @Override
    public void connectionLost(Throwable cause) {
        Intent myIntent = new Intent(DoctorDiagnoseActivity.this, MainActivity.class);
        DoctorDiagnoseActivity.this.startActivity(myIntent);
        finish();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.jsonResponse = message.toString();

        Log.d("TagMessageArrived", jsonResponse);

        String reason= JsonAccess.getJsonInsideObj(jsonResponse,"reason");


        Toast.makeText(DoctorDiagnoseActivity.this,"arrived -> "+message.toString(), Toast.LENGTH_SHORT).show();

        if (reason.equals("docMsg")) {
            //sample msg = {"reason":"docMsg", "pid":"patient123", "did":"doctor123", "sensorType":"temp", "msg":"Please scan the temp"}
           docMsg_resHandler(jsonResponse);
        }else if (reason.equals("docStopped")) {
            //sample msg = {"reason":"docStopped","did":"doc1234"}
            String _did=JsonAccess.getJsonInsideObj(jsonResponse,"did");

            if(_did.equals(did)){
                goToMainMenu("Doctor Disconnected..!");
            }

        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }



    //when Other one Stopped
    private void goToMainMenu(String msg){
        //go to another act
        Toast.makeText(DoctorDiagnoseActivity.this , msg, Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(DoctorDiagnoseActivity.this, MainActivity.class);
        DoctorDiagnoseActivity.this.startActivity(myIntent);
        finish();
    }


    private void docMsg_resHandler(String res){
        //{"reason":"docMsg", "pid":"patient123", "did":"doctor123", "sensorType":"temp", "msg":"Please scan the temp"}

        String _did=JsonAccess.getJsonInsideObj(res,"did");
        String _sensorType=JsonAccess.getJsonInsideObj(res,"sensorType");
        String _msg=JsonAccess.getJsonInsideObj(res,"msg");

        Toast.makeText(DoctorDiagnoseActivity.this,"did is- "+did+" _did -"+_did, Toast.LENGTH_SHORT).show();


        if(did.equals(_did)){

            if(_sensorType.equals("null")){
                //no sensor involved

                Toast.makeText(DoctorDiagnoseActivity.this,"Inside 1st if", Toast.LENGTH_SHORT).show();

                if(isFirstTimeMessageBoxUpdates){
                    isFirstTimeMessageBoxUpdates=false;
                    textView_MessageBox.setText("");//only runs once
                    Toast.makeText(DoctorDiagnoseActivity.this,"Inside 2nd if", Toast.LENGTH_SHORT).show();

                }
                addTextToMsgBox("Doctor -> "+_msg);

            }


            else if(_sensorType.equals("temp") || _sensorType.equals("pulse")){
                showSensorReqAlert(_sensorType);
            }else{
                //payload format is wrong //could be null
            }

        }

    }


    private void setResources(){
        textView_MessageBox = (TextView) findViewById(R.id.textView_DD_Messages);

        button_EndSession= (Button) findViewById(R.id.button_DD_EndSession);
        button_Pulse= (Button) findViewById(R.id.button_DD_PulseMonitor);
        button_Temp= (Button) findViewById(R.id.button_DD_TempMonitor);
        editText_MsgTyped = (EditText) findViewById(R.id.editText_DD_MessageTyped);
        button_SendMsg = (Button) findViewById(R.id.button_DD_SendMessage);

        //For Device Readings
        textView_SensorReadingTitle=(TextView) findViewById(R.id.textView_DD_DeviceReadTitle);
        textView_SensorReading=(TextView) findViewById(R.id.textView_DD_Temp);
        linearLayout_SensorReadingBox=(LinearLayout) findViewById(R.id.linearLayout_DD_DeviceMeter);

        linearLayout_SensorReadingBox.setVisibility(View.GONE);

        button_EndSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMainMenu("Successfully ended the Session..!");
            }
        });


        button_Temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send msg
            }
        });

        button_Pulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send msg
            }
        });

        button_SendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessageToDoctor();

            }
        });

    }


    private void addTextToMsgBox(String text){
        textView_MessageBox.setText(textView_MessageBox.getText().toString()+"\n-----\n"+text);
    }


    private void sendTextMessageToDoctor(){

        String _msgTyped=editText_MsgTyped.getText().toString();

        if(did!=null && !_msgTyped.equals("")) {

            if(isFirstTimeMessageBoxUpdates){textView_MessageBox.setText("");}

            String passingPayload = "{\"reason\":\"pMsg\", \"pid\":\""+clientID+"\", \"did\":\""+did+"\", \"temp\":\"null\"," +
                    " \"pulse\":\"null\", \"msg\":\""+_msgTyped+"\"}";

            String passingTopic = "healthxtream/doctor/"+did;

            mConnection.publishMessage(passingPayload, passingTopic);
            isFirstTimeMessageBoxUpdates=false;
            addTextToMsgBox("Me -> "+_msgTyped);
            editText_MsgTyped.setText("");//clear msgBox
        }
    }


    private void sendCancelSensorReqToDoc(String sensor){

        String tempVal="null";
        String pulseVal="null";

        if(sensor.equals("temp")){
            tempVal="cancelled";
        }else{
            pulseVal="cancelled";
        }

            String passingPayload = "{\"reason\":\"pMsg\", \"pid\":\""+clientID+"\", \"did\":\""+did+"\", \"temp\":\""+tempVal+"\"," +
                    " \"pulse\":\""+pulseVal+"\", \"msg\":\"null\"}";

            String passingTopic = "healthxtream/doctor/"+did;

            mConnection.publishMessage(passingPayload, passingTopic);
    }


    private void sendSensorReadingToDoc(String sensor, String reading){

        String tempVal="null";
        String pulseVal="null";

        if(sensor.equals("temp")){
            tempVal=reading;
        }else{
            pulseVal=reading;
        }

        String passingPayload = "{\"reason\":\"pMsg\", \"pid\":\""+clientID+"\", \"did\":\""+did+"\", \"temp\":\""+tempVal+"\"," +
                " \"pulse\":\""+pulseVal+"\", \"msg\":\"null\"}";

        String passingTopic = "healthxtream/doctor/"+did;

        mConnection.publishMessage(passingPayload, passingTopic);
    }



    private void disconnectClient()
    {
        if(client!=null) {
            try {
                IMqttToken disconToken = client.disconnect();
                disconToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // we are now successfully disconnected
                        Log.d("DocD", "Client Successfully Disconnected");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        // something went wrong, but probably we are disconnected anyway
                        Log.w("DocD", "Client is not properly disconnected");
                    }
                });
            } catch (MqttException e) {
                Log.e("DocD", "Client Disconnect -error " + e.toString());
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

    }


    public void passDisconnectMessage() {

        if(did!=null) {
            String passingPayload = "{\"reason\":\"pStopped\",\"pid\":\"" + clientID + "\"}";

            String passingTopic = "healthxtream/doctor/"+did;

            mConnection.publishMessage(passingPayload, passingTopic);
        }

    }


    ////////USB Serial Controllers - Begin/////////////
    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                //data.concat("/n");
                printNow(data);
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
                                Toast.makeText(DoctorDiagnoseActivity.this,"Serial Connection Opened", Toast.LENGTH_LONG).show();
                                linearLayout_SensorReadingBox.setVisibility(View.VISIBLE);


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
                    onClickStart("temp");
                    Toast.makeText(DoctorDiagnoseActivity.this,"usb attached", Toast.LENGTH_LONG).show();

                } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                    onClickStop();
                    currentReadingDevice="null";
                    Toast.makeText(DoctorDiagnoseActivity.this,"usb detached", Toast.LENGTH_LONG).show();

                }
            }catch (Exception ex){
                Toast.makeText(DoctorDiagnoseActivity.this,"Exception occured +"+ex.getMessage(), Toast.LENGTH_LONG).show();

            }

        }

        ;
    };



    public void onClickStart(String deviceName) {

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
            Toast.makeText(DoctorDiagnoseActivity.this,"Error onclick +"+ex.getMessage(), Toast.LENGTH_LONG).show();

        }
    }



    public void onClickStop() {
        serialPort.close();
        //printNow("\nSerial Connection Closed! \n");

    }


    private void printNow(String text){
        float tempCount=0;
        try {
            tempCount=Float.parseFloat(text);
        }catch (NumberFormatException ex){
            tempCount=12.0f;
        }

        String messageTemp="";

        if(tempCount<30 && tempCount>22 && currentReadingDevice.equals("temp")){
            //sendMsgToDoc wit read count
            sendSensorReadingToDoc(currentReadingDevice,String.valueOf(tempCount));
            messageTemp=String.valueOf(tempCount)+" C'";
        }else if(tempCount<30 && tempCount>23 && currentReadingDevice.equals("pulse")){
            //sendMsgToDoc wit read count
            sendSensorReadingToDoc(currentReadingDevice,String.valueOf(tempCount));
            messageTemp=String.valueOf(tempCount)+" BPM";
        }

        final String message=messageTemp;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!message.equals(""))
                    textView_SensorReading.setText(message);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(!isRetainMqttState) {
            passDisconnectMessage();
            disconnectClient();
        }

        if(serialPort!=null)
        serialPort.close();

        unregisterReceiver(broadcastReceiver);

    }

    ////////USB Serial Controllers - End/////////////



    ////alert/////

    private void showSensorReqAlert(final String device){

        String _deviceName="Body Temperature Monitor";
        String _Msg="Doctor requested you to use ";


        if(device.equals("pulse"))
        {
            _deviceName="Pulse Sensor";
        }

        final String _deviceNameFinal=_deviceName;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                DoctorDiagnoseActivity.this);

        // set title
        alertDialogBuilder.setTitle("Doctor Requested a sensor reading");

        // set dialog message
        alertDialogBuilder
                .setMessage(_Msg+_deviceName+". Connect the sensor and press OK. If you don't want to use the sensor, press Cancel.")
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
//                        String deviceName="Body Temperature Monitor";
//                        if(device.equals("pulse")) deviceName="Pulse Sensor";
//                        Toast.makeText(DoctorDiagnoseActivity.this,"Requested the reading from "+deviceName, Toast.LENGTH_SHORT).show();

                          textView_SensorReadingTitle.setText("Reading From "+_deviceNameFinal);
                          textView_SensorReading.setText("---");
                          currentReadingDevice=device;
                          onClickStart(device);

                    }
                })
                .setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String deviceName="Body Temperature Monitor";
                        if(device.equals("pulse")) deviceName="Pulse Sensor";
                        sendCancelSensorReqToDoc(device);
                        Toast.makeText(DoctorDiagnoseActivity.this,"Cancelled using the "+deviceName, Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }





}
