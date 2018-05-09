package com.example.senura.healthxstream.DoctorsAct;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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

import com.example.senura.healthxstream.LoginActivity;
import com.example.senura.healthxstream.R;
import com.example.senura.healthxstream.mqttConnectionPackage.JsonAccess;
import com.example.senura.healthxstream.mqttConnectionPackage.MqttConnection;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PatientDiagnoseActivity extends AppCompatActivity implements MqttCallback {


    public static MqttConnection mConnectionTemp=null;
    private MqttConnection mConnection=null;
    public static MqttAndroidClient clientTemp = null;
    private MqttAndroidClient client = null;

    //MQTT Vars
    private boolean isPublished = false;
    public String jsonResponse = null;



    //res
    Button button_ReqPulse=null;
    Button button_ReqTemp=null;
    Button button_EndSession=null;
    Button button_SendMsg=null;
    EditText editText_MsgTyped=null;
    TextView textView_messageBx=null;

    //sensorReading
    TextView textView_SensorReadingTitle=null;
    TextView textView_SensorReading=null;
    LinearLayout linearLayout_SensorReadingBox=null;


    private boolean isRetainMqttState=false;


    private String patientID=null;
    private String doctorID=null;

    private boolean isFirstTimeMessageBoxUpdates=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_diagnose);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);



        Intent intent = getIntent();
        doctorID = intent.getStringExtra("did");
        patientID = intent.getStringExtra("pid");

        mConnection=mConnectionTemp;
        client=clientTemp;

        initRes();
        setClientListenToThisAct();
    }



    private void setClientListenToThisAct(){
        client.setCallback(PatientDiagnoseActivity.this);
    }


    private void initRes(){
        button_EndSession= (Button) findViewById(R.id.button_PD_EndSession);
        button_ReqPulse= (Button) findViewById(R.id.button_PD_ReqPulseMonitor);
        button_ReqTemp= (Button) findViewById(R.id.button_PD_ReqTempMonitor);
        textView_messageBx = (TextView) findViewById(R.id.textView_PD_Messages);
        editText_MsgTyped = (EditText) findViewById(R.id.editText_PD_MessageTyped);
        button_SendMsg = (Button) findViewById(R.id.button_PD_SendMessage);

        //For Device Readings
        textView_SensorReadingTitle=(TextView) findViewById(R.id.textView_PD_DeviceReadTitle);
        textView_SensorReading=(TextView) findViewById(R.id.textView_PD_Temp);
        linearLayout_SensorReadingBox=(LinearLayout) findViewById(R.id.linearLayout_PD_DeviceMeter);

        linearLayout_SensorReadingBox.setVisibility(View.GONE);


        button_EndSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              goToLogin("Successfully ended the Session..!");
            }
        });


        button_ReqTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              showAlert("temp");
            }
        });

        button_ReqPulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert("pulse");
            }
        });

        button_SendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessageToPatient();

            }
        });

    }




    private void showAlert(final String device){

        String _deviceName="Body Temperature Monitor";
        String _Msg="Do you want patient to use ";


        if(device.equals("pulse"))
        {
            _deviceName="Pulse Sensor";
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                PatientDiagnoseActivity.this);

        // set title
        alertDialogBuilder.setTitle("Request a sensor reading");

        // set dialog message
        alertDialogBuilder
                .setMessage(_Msg+_deviceName+" ?")
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        sendDeviceReqToPatient(device);
                        String deviceName="Body Temperature Monitor";
                        if(device.equals("pulse")) deviceName="Pulse Sensor";
                        Toast.makeText(PatientDiagnoseActivity.this,"Requested the reading from "+deviceName, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Toast.makeText(PatientDiagnoseActivity.this,"Cancelled", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    private void sendDeviceReqToPatient(String device){
        if(patientID!=null) {

            makeSensorReqButtonVisible(false, device);//hide button

            String passingPayload = "{\"reason\":\"docMsg\", \"pid\":\""+patientID+"\", \"did\":\""+doctorID+"\", " +
                    "\"sensorType\":\""+device+"\", \"msg\":\"null\"}";

            String passingTopic = "healthxtream/patient/"+patientID;

            mConnection.publishMessage(passingPayload, passingTopic);

        }
    }


    private void makeSensorReqButtonVisible(Boolean state, String device){
        int visibility=0;
        int inverseVisibility=0;

        if(state){
            visibility=View.VISIBLE;
            inverseVisibility=View.INVISIBLE;
        }
        else{
            visibility=View.INVISIBLE;
            inverseVisibility=View.VISIBLE;
        }


        if(device.equals("temp")){
            button_ReqTemp.setVisibility(visibility);
            button_ReqPulse.setVisibility(inverseVisibility);
        }else if(device.equals("pulse")){
            button_ReqPulse.setVisibility(visibility);
            button_ReqTemp.setVisibility(inverseVisibility);
        }
    }


    private void sendTextMessageToPatient(){

        String _msgTyped=editText_MsgTyped.getText().toString();


        if(patientID!=null && !_msgTyped.equals("")) {

            if(isFirstTimeMessageBoxUpdates){textView_messageBx.setText("");}

            String passingPayload = "{\"reason\":\"docMsg\", \"pid\":\""+patientID+"\", \"did\":\""+doctorID+"\", " +
                    "\"sensorType\":\"null\", \"msg\":\""+_msgTyped+"\"}";

            String passingTopic = "healthxtream/patient/"+patientID;

            mConnection.publishMessage(passingPayload, passingTopic);

            addTextToMsgBox("Me -> "+_msgTyped);
            isFirstTimeMessageBoxUpdates=false;
            editText_MsgTyped.setText("");//clear msgBox

        }
    }



    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.jsonResponse = message.toString();

        Log.d("TagMessageArrived", jsonResponse);

        String reason= JsonAccess.getJsonInsideObj(jsonResponse,"reason");

        if (reason.equals("pMsg")) {
            //sample msg = {"reason":"pMsg", "pid":"patient123", "did":"doctor123", "temp":"1", "pulse":"null", "msg":"null"}
            docMsg_resHandler(jsonResponse);

        } else if (reason.equals("pStopped")) {
            //sample msg = {"reason":"pStopped","pid":"pid12345"}
            String _pid=JsonAccess.getJsonInsideObj(jsonResponse,"pid");

            if(_pid.equals(patientID)){
                goToLogin("Patient Disconnected..!");
            }
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


    //when Other one Stopped
    private void goToLogin(String msg){
        //go to another act
        Toast.makeText(PatientDiagnoseActivity.this , msg, Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(PatientDiagnoseActivity.this, LoginActivity.class);
        PatientDiagnoseActivity.this.startActivity(myIntent);
        finish();
    }


    private void docMsg_resHandler(String res){
        //{"reason":"pMsg", "pid":"patient123", "did":"doctor123", "temp":"1", "pulse":"null", "msg":"null"}

        String _pid=JsonAccess.getJsonInsideObj(res,"pid");
        String _msg=JsonAccess.getJsonInsideObj(res,"msg");
        String _pulse=JsonAccess.getJsonInsideObj(res,"pulse");
        String _temp=JsonAccess.getJsonInsideObj(res,"temp");


        if(_pid.equals(_pid)){

            if(!_msg.equals("null")){

                if(isFirstTimeMessageBoxUpdates){
                    isFirstTimeMessageBoxUpdates=false;
                    textView_messageBx.setText("");//only runs once
                }
                addTextToMsgBox("Patient -> "+_msg);
            }else if(_pulse.equals("cancelled")){
                makeSensorReqButtonVisible(true, "pulse");//show button
            }else if(_temp.equals("cancelled")){
                makeSensorReqButtonVisible(true, "temp");//show button
            }else if(!_pulse.equals("null")){
                addSensorReadingToTextBox("pulse", jsonResponse);
            }else if(!_temp.equals("null")){
                addSensorReadingToTextBox("temp", jsonResponse);
            }else{
                //payload format is wrong //could be null
            }

        }

    }


    private void addSensorReadingToTextBox(String device, String res){
        //{"reason":"pMsg", "pid":"patient123", "did":"doctor123", "temp":"1", "pulse":"null", "msg":"null"}

        String _reading=JsonAccess.getJsonInsideObj(res,device);

        String message="",title="";
        String devicename="Temperature Monitor";
        String tempSymbol="C'";


        if(device.equals("pulse")){
            devicename="Pulse Sensor";
            tempSymbol="BPM";
        }


        linearLayout_SensorReadingBox.setVisibility(View.VISIBLE);

        title="Reading from "+devicename;
        textView_SensorReadingTitle.setText(title);

        message=_reading+" "+tempSymbol;
        textView_SensorReading.setText(message);

    }

    private void addTextToMsgBox(String text){
        textView_messageBx.setText(textView_messageBx.getText().toString()+"\n-----\n"+text);
    }


    public void passDisconnectMessage() {

        if(patientID!=null) {
            String passingPayload = "{\"reason\":\"docStopped\",\"did\":\"" + doctorID + "\"}";

            String passingTopic = "healthxtream/patient/"+patientID;

            mConnection.publishMessage(passingPayload, passingTopic);
        }

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
                        Log.d("DocNILL", "Client Successfully Disconnected");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        // something went wrong, but probably we are disconnected anyway
                        Log.w("DocNILL", "Client is not properly disconnected");
                    }
                });
            } catch (MqttException e) {
                Log.e("DocNILL", "Client Disconnect -error " + e.toString());
            }
        }
    }





    @Override
    public void onStop() {
        super.onStop();

        if(!isRetainMqttState) {
            passDisconnectMessage();
            disconnectClient();
        }
    }






}
